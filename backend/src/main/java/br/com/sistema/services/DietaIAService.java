package br.com.sistema.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.sistema.dtos.DietaRequest;
import br.com.sistema.dtos.ia.GerarDietaIARequest;
import br.com.sistema.dtos.ia.OpenAIRequest;
import br.com.sistema.dtos.ia.OpenAIRequest.Message;
import br.com.sistema.dtos.ia.OpenAIResponse;
import br.com.sistema.exceptions.BusinessException;
import br.com.sistema.exceptions.ResourceNotFoundException;
import br.com.sistema.models.AvaliacaoFisica;
import br.com.sistema.models.ConfiguracaoIA;
import br.com.sistema.models.Paciente;
import br.com.sistema.models.QuestionarioEstiloVida;
import br.com.sistema.models.TacoAlimento;
import br.com.sistema.repositories.AvaliacaoFisicaRepository;
import br.com.sistema.repositories.ConsultaRepository;
import br.com.sistema.repositories.PacienteRepository;
import br.com.sistema.repositories.QuestionarioEstiloVidaRepository;
import br.com.sistema.repositories.TacoAlimentoRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Orquestra a geracao de dietas individualizadas via IA generativa (OpenAI/OpenRouter).
 * Consolida dados do paciente (avaliacao fisica, questionario de estilo de vida) e
 * uma selecao do banco TACO para construir prompts ricos e gerar dietas completas
 * com refeicoes, macroNutrientes e orientacoes personalizadas.
 */
@Service
@Slf4j
public class DietaIAService {

    private static final List<String> GRUPOS_TACO_PADRAO = List.of(
            "CEREAIS_E_DERIVADOS",
            "LEGUMINOSAS_E_DERIVADOS",
            "FRUTAS_E_DERIVADOS",
            "HORTALICAS_E_DERIVADOS",
            "CARNES_E_DERIVADOS",
            "AVES_E_DERIVADOS",
            "PESCADOS_E_FRUTOS_DO_MAR",
            "OVOS_E_DERIVADOS",
            "LEITE_E_DERIVADOS",
            "OLEOS_E_GORDURAS"
    );

    private static final int LIMITE_ALIMENTOS_CONTEXTO = 150;

    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;
    private final QuestionarioEstiloVidaRepository questionarioRepository;
    private final AvaliacaoFisicaRepository avaliacaoFisicaRepository;
    private final TacoAlimentoRepository tacoAlimentoRepository;
    private final ConfiguracaoIAService configuracaoIAService;
    private final UsoIALogService usoIALogService;
    private final ObjectMapper objectMapper;
    private final RestTemplate iaRestTemplate;

    public DietaIAService(
            PacienteRepository pacienteRepository,
            ConsultaRepository consultaRepository,
            QuestionarioEstiloVidaRepository questionarioRepository,
            AvaliacaoFisicaRepository avaliacaoFisicaRepository,
            TacoAlimentoRepository tacoAlimentoRepository,
            ConfiguracaoIAService configuracaoIAService,
            UsoIALogService usoIALogService,
            ObjectMapper objectMapper,
            @Qualifier("iaRestTemplate") RestTemplate iaRestTemplate) {
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
        this.questionarioRepository = questionarioRepository;
        this.avaliacaoFisicaRepository = avaliacaoFisicaRepository;
        this.tacoAlimentoRepository = tacoAlimentoRepository;
        this.configuracaoIAService = configuracaoIAService;
        this.usoIALogService = usoIALogService;
        this.objectMapper = objectMapper;
        this.iaRestTemplate = iaRestTemplate;
    }

    /**
     * Gera uma dieta completa via IA utilizando dados consolidados do paciente.
     * Agrega perfil antropometrico, preferencias alimentares e inventario TACO
     * para construir prompts que maximizam a precisao nutricional da geracao.
     *
     * @param pacienteId ID do paciente
     * @param request parametros de geracao (kcal, macros, numero de refeicoes)
     * @return dieta com refeicoes, opcoes e suplementos
     */
    @Transactional(readOnly = true)
    public DietaRequest gerarDieta(Long pacienteId, GerarDietaIARequest request) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente n�o encontrado: " + pacienteId));

        var ultimaConsulta = consultaRepository.findFirstByPacienteIdOrderByDataConsultaDesc(pacienteId)
                .orElseThrow(() -> new BusinessException(
                        "Paciente n�o possui consultas registradas. Cadastre uma consulta antes de gerar a dieta com IA."));

        QuestionarioEstiloVida questionario = questionarioRepository
                .findByConsultaId(ultimaConsulta.getId())
                .orElse(null);

        AvaliacaoFisica avaliacao = avaliacaoFisicaRepository
                .findByConsultaId(ultimaConsulta.getId())
                .orElse(null);

        ConfiguracaoIA config = configuracaoIAService.buscarParaUso();

        List<TacoAlimento> alimentosTaco = selecionarAlimentosTaco(questionario);

        String promptSistema = configuracaoIAService.resolverPromptSistema(config);
        String promptUsuario = construirPromptUsuario(paciente, avaliacao, questionario, alimentosTaco, request);

        return chamarIAApi(config, promptSistema, promptUsuario, pacienteId);
    }

    /**
     * Seleciona alimentos do banco TACO considerando preferencias e restricoes
     * alimentares do questionario. Inclui grupos basicos mais opcionais ativados
     * pelo paciente para manter variedade na geracao.
     *
     * @param questionario questionario de estilo de vida do paciente
     * @return lista de alimentos TACO ordenados por grupo
     */
    private List<TacoAlimento> selecionarAlimentosTaco(QuestionarioEstiloVida questionario) {
        List<String> grupos = new java.util.ArrayList<>(GRUPOS_TACO_PADRAO);

        if (questionario != null && isPreenchido(questionario.getFrutasPreferidas())) {
            if (!grupos.contains("FRUTAS_E_DERIVADOS")) {
                grupos.add("FRUTAS_E_DERIVADOS");
            }
        }

        return tacoAlimentoRepository
                .findByGrupoIn(grupos)
                .stream()
                .limit(LIMITE_ALIMENTOS_CONTEXTO)
                .collect(Collectors.toList());
    }

    /**
     * Constroi o prompt de usuario com dados completos do paciente, parametros
     * nutricionais e inventario de alimentos TACO.
     * Inclui perfil antropometrico, preferencias, restricoes e metas cal�ricas.
     *
     * @param paciente paciente com dados basicos
     * @param avaliacao avaliacao fisica com medidas corporais
     * @param questionario questionario com preferencias e estilo de vida
     * @param alimentos lista de alimentos TACO disponiveis
     * @param request parametros de geracao da dieta
     * @return prompt formatado para envio a IA
     */
    private String construirPromptUsuario(Paciente paciente, AvaliacaoFisica avaliacao,
                                          QuestionarioEstiloVida questionario,
                                          List<TacoAlimento> alimentos, GerarDietaIARequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Gere uma dieta personalizada no seguinte formato JSON.");
        sb.append("\n\n=== DADOS DO PACIENTE ===\n");
        sb.append("Nome: ").append(paciente.getNomeCompleto()).append("\n");

        if (avaliacao != null) {
            LocalDate dt = paciente.getDataNascimento();
            int idade = (dt != null) ? Period.between(dt, LocalDate.now()).getYears() : 0;
            sb.append("Idade: ").append(idade).append(" anos\n");
            sb.append("Sexo: ").append(paciente.getSexo()).append("\n");

            sb.append("Peso: ").append(formatarDouble(avaliacao.getPesoAtual())).append(" kg\n");
            sb.append("Altura: ").append(formatarDouble(avaliacao.getAltura())).append(" cm\n");

            // IMC usado para calibrar a densidade calorica do cardapio
            if (avaliacao.getPesoAtual() != null && avaliacao.getAltura() != null
                    && avaliacao.getAltura() > 0) {
                double imc = avaliacao.getPesoAtual()
                        / Math.pow(avaliacao.getAltura() / 100, 2);
                sb.append("IMC: ").append(String.format("%.1f", imc)).append(" kg/m�\n");
            }

            sb.append("Gordura Corporal: ").append(formatarDouble(avaliacao.getPercentualGordura())).append("%\n");
            sb.append("Massa Muscular: ").append(formatarDouble(avaliacao.getMassaMagra())).append(" kg\n");
        }

        sb.append("\n=== QUESTION�RIO DE ESTILO DE VIDA ===\n");
        if (questionario != null) {
            if (isPreenchido(questionario.getObjetivo())) {
                sb.append("Objetivo: ").append(questionario.getObjetivo()).append("\n");
            }
            if (isPreenchido(questionario.getFrequenciaTreino())) {
                sb.append("Frequ�ncia de treino: ").append(questionario.getFrequenciaTreino()).append("\n");
            }
            if (isPreenchido(questionario.getTempoTreino())) {
                sb.append("Dura��o do treino: ").append(questionario.getTempoTreino()).append("\n");
            }
            if (isPreenchido(questionario.getAlimentosNaoGosta())) {
                sb.append("Alimentos que n�o gosta: ").append(questionario.getAlimentosNaoGosta()).append("\n");
            }
            if (isPreenchido(questionario.getFrutasPreferidas())) {
                sb.append("Frutas preferidas: ").append(questionario.getFrutasPreferidas()).append("\n");
            }
            if (questionario.getNumeroRefeicoesDesejadas() != null) {
                sb.append("Refeicoes desejadas: ").append(questionario.getNumeroRefeicoesDesejadas()).append("\n");
            }
            if (isPreenchido(questionario.getIntolerancias())) {
                sb.append("Intolerancias/Alergias: ").append(questionario.getIntolerancias()).append("\n");
            }
            if (isPreenchido(questionario.getDoencas())) {
                sb.append("Doen�as/Condicoes: ").append(questionario.getDoencas()).append("\n");
            }
            if (isPreenchido(questionario.getMedicamentos())) {
                sb.append("Medicamentos em uso: ").append(questionario.getMedicamentos()).append("\n");
            }
        }

        sb.append("\n=== INVENT�RIO TACO (use SOMENTE estes alimentos) ===\n");
        sb.append("Quantidade maxima de alimentos no contexto: ").append(LIMITE_ALIMENTOS_CONTEXTO).append("\n\n");

        for (TacoAlimento t : alimentos) {
            sb.append("- ").append(t.getDescricao());
            sb.append(" | Proteina: ").append(formatarDouble(valorOuZero(t.getProteinaG())));
            sb.append("g | Carbo: ").append(formatarDouble(valorOuZero(t.getCarboidratosG())));
            sb.append("g | Gordura: ").append(formatarDouble(valorOuZero(t.getLipideosG())));
            sb.append(" | Calorias: ").append(formatarDouble(
                    valorOuZero(t.getCarboidratosG()) * 4 +
                    valorOuZero(t.getProteinaG()) * 4 +
                    valorOuZero(t.getLipideosG()) * 9));
            sb.append(" kcal por 100g\n");
        }

        int numRefeicoes = (questionario != null && questionario.getNumeroRefeicoesDesejadas() != null)
                ? questionario.getNumeroRefeicoesDesejadas() : 5;

        sb.append("\n=== INSTRU��ES ===\n");
        sb.append("1. Crie exatamente ").append(numRefeicoes).append(" refei��es.\n");
        sb.append("2. A soma total de calorias de todas as refei��es deve ser pr�xima de ")
          .append(request.getKcalTotal()).append(" kcal.\n");
        sb.append("3. Respeite as metas de prote�nas (").append(request.getProteinasG())
          .append("g), carboidratos (").append(request.getCarboidratosG())
          .append("g) e gorduras (").append(request.getGordurasG()).append("g).\n");
        sb.append("4. Use APENAS alimentos da lista TACO acima. Nunca invente alimentos.\n");
        sb.append("5. No campo 'observacoes', inclua orienta��es espec�ficas sobre medicamentos, ");
        sb.append("doen�as e cuidados de sa�de relevantes para este paciente.\n");
        sb.append("6. Forne�a quantidades realistas em gramas ou unidades comuns (colher, x�cara, etc.).\n");
        sb.append("7. Distribua as calorias de forma equilibrada entre as refei��es.\n");

        return sb.toString();
    }

    /**
     * Envia a requisicao para a API de IA configurada (OpenAI ou OpenRouter).
     * Trata erros de autenticacao, rate limiting e timeouts com mensagens
     * de negocio amigaveis ao usuario.
     *
     * @param config configuracao de IA (modelo, key, endpoint)
     * @param promptSistema instrucoes de comportamento do assistente
     * @param promptUsuario dados do paciente e parametros nutricionais
     * @return dieta parseada da resposta JSON da IA
     */
    private DietaRequest chamarIAApi(ConfiguracaoIA config, String promptSistema, String promptUsuario, Long pacienteId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(config.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (config.getProvedor() != null && "OPENROUTER".equals(config.getProvedor().name())) {
            headers.set("HTTP-Referer", "https://app.nutriandrereis.com.br");
            headers.set("X-Title", "NutriAndreReis");
        }

        OpenAIRequest payload = new OpenAIRequest(
                config.getModelo(),
                List.of(
                        new Message("system", promptSistema),
                        new Message("user", promptUsuario)
                ),
                config.getTemperaturaModelo(),
                Map.of("type", "json_object")
        );

        HttpEntity<OpenAIRequest> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<OpenAIResponse> response = iaRestTemplate.exchange(
                    config.getBaseUrl() + "/chat/completions",
                    HttpMethod.POST,
                    entity,
                    OpenAIResponse.class
            );

            if (response.getBody() == null || response.getBody().getChoices() == null
                    || response.getBody().getChoices().isEmpty()) {
                throw new BusinessException("A IA retornou uma resposta vazia. Tente novamente.");
            }

            String jsonContent = response.getBody().getChoices().get(0).getMessage().getContent();
            log.debug("Resposta bruta da IA recebida ({} caracteres)", jsonContent.length());

            usoIALogService.registrar(config, response.getBody(), pacienteId, true);

            return parsearResposta(jsonContent);

        } catch (HttpClientErrorException.Unauthorized ex) {
            usoIALogService.registrar(config, null, pacienteId, false);
            throw new BusinessException("API key inv�lida. Verifique a chave configurada em Configura��es > IA.");
        } catch (HttpClientErrorException.TooManyRequests ex) {
            usoIALogService.registrar(config, null, pacienteId, false);
            throw new BusinessException("Limite de requisi��es da IA atingido. Aguarde alguns instantes e tente novamente.");
        } catch (HttpClientErrorException ex) {
            log.error("Erro HTTP ao chamar IA: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            usoIALogService.registrar(config, null, pacienteId, false);
            throw new BusinessException("Erro ao chamar a IA: " + ex.getStatusCode() + ". Verifique as configura��es.");
        } catch (ResourceAccessException ex) {
            usoIALogService.registrar(config, null, pacienteId, false);
            throw new BusinessException("Tempo de resposta da IA excedido. Tente novamente em instantes.");
        }
    }

    /**
     * Parseia a resposta JSON da IA convertendo para DietaRequest.
     * Valida que ao menos uma refeicao foi gerada antes de retornar.
     *
     * @param json string JSON retornada pela API
     * @return dieta processada
     */
    private DietaRequest parsearResposta(String json) {
        try {
            DietaRequest dieta = objectMapper.readValue(json, DietaRequest.class);
            if (dieta.getRefeicoes() == null || dieta.getRefeicoes().isEmpty()) {
                throw new BusinessException("A IA n�o gerou nenhuma refei��o. Verifique o prompt do sistema e tente novamente.");
            }
            return dieta;
        } catch (JsonProcessingException ex) {
            log.error("Falha ao parsear resposta da IA: {}", json);
            throw new BusinessException("A IA retornou um formato de resposta inv�lido. Tente novamente.");
        }
    }

    /**
     * Verifica se uma string esta preenchida (nao nula e nao vazia apos trim).
     *
     * @param valor string a verificar
     * @return true se preenchida
     */
    private boolean isPreenchido(String valor) {
        return valor != null && !valor.isBlank();
    }

    /**
     * Formata valor Double para exibicao com uma casa decimal.
     * Retorna "N/I" para valores nulos.
     *
     * @param valor valor a formatar
     * @return string formatada
     */
    private String formatarDouble(Double valor) {
        if (valor == null) return "N/I";
        return String.format("%.1f", valor);
    }

    /**
     * Converte BigDecimal para double, retornando zero se nulo.
     *
     * @param valor BigDecimal
     * @return valor numerico ou zero
     */
    private double valorOuZero(BigDecimal valor) {
        return valor != null ? valor.doubleValue() : 0.0;
    }
}
