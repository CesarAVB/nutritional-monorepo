package br.com.sistema.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
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
    private final ObjectMapper objectMapper;
    private final RestTemplate iaRestTemplate;

    public DietaIAService(
            PacienteRepository pacienteRepository,
            ConsultaRepository consultaRepository,
            QuestionarioEstiloVidaRepository questionarioRepository,
            AvaliacaoFisicaRepository avaliacaoFisicaRepository,
            TacoAlimentoRepository tacoAlimentoRepository,
            ConfiguracaoIAService configuracaoIAService,
            ObjectMapper objectMapper,
            @Qualifier("iaRestTemplate") RestTemplate iaRestTemplate) {
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
        this.questionarioRepository = questionarioRepository;
        this.avaliacaoFisicaRepository = avaliacaoFisicaRepository;
        this.tacoAlimentoRepository = tacoAlimentoRepository;
        this.configuracaoIAService = configuracaoIAService;
        this.objectMapper = objectMapper;
        this.iaRestTemplate = iaRestTemplate;
    }

    @Transactional(readOnly = true)
    public DietaRequest gerarDieta(Long pacienteId, GerarDietaIARequest request) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado: " + pacienteId));

        var ultimaConsulta = consultaRepository.findFirstByPacienteIdOrderByDataConsultaDesc(pacienteId)
                .orElseThrow(() -> new BusinessException(
                        "Paciente não possui consultas registradas. Cadastre uma consulta antes de gerar a dieta com IA."));

        QuestionarioEstiloVida questionario = questionarioRepository
                .findByConsultaId(ultimaConsulta.getId())
                .orElse(null);

        AvaliacaoFisica avaliacao = avaliacaoFisicaRepository
                .findByConsultaId(ultimaConsulta.getId())
                .orElse(null);

        ConfiguracaoIA config = configuracaoIAService.buscarParaUso();

        List<TacoAlimento> alimentosTaco = selecionarAlimentosTaco(questionario);

        String promptSistema = configuracaoIAService.resolverPromptSistema(config);
        String promptUsuario = montarPromptUsuario(paciente, questionario, avaliacao, request, alimentosTaco);

        log.debug("Chamando API de IA para paciente {} | {} alimentos TACO | {} kcal",
                pacienteId, alimentosTaco.size(), request.getKcalTotal());

        return chamarIAApi(config, promptSistema, promptUsuario);
    }

    private List<TacoAlimento> selecionarAlimentosTaco(QuestionarioEstiloVida questionario) {
        List<TacoAlimento> todos = tacoAlimentoRepository.findByGrupoIn(GRUPOS_TACO_PADRAO);

        List<String> termos = extrairTermosExclusao(questionario);

        List<TacoAlimento> filtrados = todos.stream()
                .filter(a -> termos.stream().noneMatch(
                        t -> a.getDescricao().toLowerCase().contains(t.toLowerCase())))
                .limit(LIMITE_ALIMENTOS_CONTEXTO)
                .collect(Collectors.toList());

        log.debug("Alimentos TACO selecionados: {} (de {} totais, {} excluídos por restrições)",
                filtrados.size(), todos.size(), todos.size() - filtrados.size());

        return filtrados;
    }

    private List<String> extrairTermosExclusao(QuestionarioEstiloVida q) {
        if (q == null) return List.of();
        String combinado = "";
        if (q.getAlimentosNaoGosta() != null) combinado += q.getAlimentosNaoGosta() + ",";
        if (q.getIntolerancias() != null) combinado += q.getIntolerancias();
        return Arrays.stream(combinado.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String montarPromptUsuario(
            Paciente paciente,
            QuestionarioEstiloVida q,
            AvaliacaoFisica a,
            GerarDietaIARequest req,
            List<TacoAlimento> alimentos) {

        StringBuilder sb = new StringBuilder();

        sb.append("=== DADOS DO PACIENTE ===\n");
        sb.append("Nome: ").append(paciente.getNomeCompleto()).append("\n");
        sb.append("Sexo: ").append(paciente.getSexo()).append("\n");
        if (paciente.getDataNascimento() != null) {
            int idade = Period.between(paciente.getDataNascimento(), LocalDate.now()).getYears();
            sb.append("Idade: ").append(idade).append(" anos\n");
        }

        if (a != null) {
            sb.append("Peso: ").append(formatarDouble(a.getPesoAtual())).append(" kg\n");
            sb.append("Altura: ").append(formatarDouble(a.getAltura())).append(" cm\n");
            sb.append("IMC: ").append(formatarDouble(a.getImc())).append("\n");
            sb.append("Massa magra: ").append(formatarDouble(a.getMassaMagra())).append(" kg\n");
            sb.append("% Gordura: ").append(formatarDouble(a.getPercentualGordura())).append("%\n");
        }

        sb.append("\n=== METAS NUTRICIONAIS ===\n");
        sb.append("Calorias totais: ").append(req.getKcalTotal()).append(" kcal\n");
        sb.append("Proteínas: ").append(req.getProteinasG()).append(" g\n");
        sb.append("Carboidratos: ").append(req.getCarboidratosG()).append(" g\n");
        sb.append("Gorduras: ").append(req.getGordurasG()).append(" g\n");

        if (q != null) {
            sb.append("\n=== PREFERÊNCIAS E RESTRIÇÕES ===\n");
            sb.append("Número de refeições desejadas: ")
              .append(q.getNumeroRefeicoesDesejadas() != null ? q.getNumeroRefeicoesDesejadas() : "5").append("\n");
            if (isPreenchido(q.getAlimentosNaoGosta()))
                sb.append("Alimentos a EVITAR: ").append(q.getAlimentosNaoGosta()).append("\n");
            if (isPreenchido(q.getIntolerancias()))
                sb.append("Intolerâncias: ").append(q.getIntolerancias()).append("\n");
            if (isPreenchido(q.getFrutasPreferidas()))
                sb.append("Frutas preferidas: ").append(q.getFrutasPreferidas()).append("\n");
            if (isPreenchido(q.getHorarioMaiorFome()))
                sb.append("Horário de maior fome: ").append(q.getHorarioMaiorFome()).append("\n");

            sb.append("\n=== SAÚDE E MEDICAMENTOS ===\n");
            if (isPreenchido(q.getDoencas()))
                sb.append("Doenças: ").append(q.getDoencas()).append("\n");
            if (isPreenchido(q.getMedicamentos()))
                sb.append("Medicamentos em uso: ").append(q.getMedicamentos()).append("\n");
            if (isPreenchido(q.getPressaoArterial()))
                sb.append("Pressão arterial: ").append(q.getPressaoArterial()).append("\n");
            if (isPreenchido(q.getCirurgias()))
                sb.append("Cirurgias: ").append(q.getCirurgias()).append("\n");
            if (isPreenchido(q.getHistoricoFamiliar()))
                sb.append("Histórico familiar: ").append(q.getHistoricoFamiliar()).append("\n");
            if (Boolean.TRUE.equals(q.getFuma()))
                sb.append("Tabagista: Sim\n");
            if (isPreenchido(q.getFrequenciaAlcool()))
                sb.append("Consumo de álcool: ").append(q.getFrequenciaAlcool()).append("\n");
            if (isPreenchido(q.getQualidadeSono()))
                sb.append("Qualidade do sono: ").append(q.getQualidadeSono()).append("\n");

            sb.append("\n=== ESTILO DE VIDA ===\n");
            if (isPreenchido(q.getObjetivo()))
                sb.append("Objetivo: ").append(q.getObjetivo()).append("\n");
            if (isPreenchido(q.getFrequenciaTreino()))
                sb.append("Frequência de treino: ").append(q.getFrequenciaTreino()).append("\n");
            if (isPreenchido(q.getTempoTreino()))
                sb.append("Duração do treino: ").append(q.getTempoTreino()).append("\n");
        }

        sb.append("\n=== ALIMENTOS DISPONÍVEIS (Tabela TACO) ===\n");
        sb.append("Use EXCLUSIVAMENTE os alimentos abaixo. Valores por 100g:\n");
        for (TacoAlimento t : alimentos) {
            sb.append(String.format("- %s | %.0f kcal | P:%.1fg C:%.1fg G:%.1fg\n",
                    t.getDescricao(),
                    valorOuZero(t.getEnergiaKcal()),
                    valorOuZero(t.getProteinaG()),
                    valorOuZero(t.getCarboidratosG()),
                    valorOuZero(t.getLipideosG())));
        }

        int numRefeicoes = (q != null && q.getNumeroRefeicoesDesejadas() != null)
                ? q.getNumeroRefeicoesDesejadas() : 5;

        sb.append("\n=== INSTRUÇÕES ===\n");
        sb.append("1. Crie exatamente ").append(numRefeicoes).append(" refeições.\n");
        sb.append("2. A soma total de calorias de todas as refeições deve ser próxima de ")
          .append(req.getKcalTotal()).append(" kcal.\n");
        sb.append("3. Respeite as metas de proteínas (").append(req.getProteinasG())
          .append("g), carboidratos (").append(req.getCarboidratosG())
          .append("g) e gorduras (").append(req.getGordurasG()).append("g).\n");
        sb.append("4. Use APENAS alimentos da lista TACO acima. Nunca invente alimentos.\n");
        sb.append("5. No campo 'observacoes', inclua orientações específicas sobre medicamentos, ");
        sb.append("doenças e cuidados de saúde relevantes para este paciente.\n");
        sb.append("6. Forneça quantidades realistas em gramas ou unidades comuns (colher, xícara, etc.).\n");
        sb.append("7. Distribua as calorias de forma equilibrada entre as refeições.\n");

        return sb.toString();
    }

    private DietaRequest chamarIAApi(ConfiguracaoIA config, String promptSistema, String promptUsuario) {
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

            return parsearResposta(jsonContent);

        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new BusinessException("API key inválida. Verifique a chave configurada em Configurações > IA.");
        } catch (HttpClientErrorException.TooManyRequests ex) {
            throw new BusinessException("Limite de requisições da IA atingido. Aguarde alguns instantes e tente novamente.");
        } catch (HttpClientErrorException ex) {
            log.error("Erro HTTP ao chamar IA: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new BusinessException("Erro ao chamar a IA: " + ex.getStatusCode() + ". Verifique as configurações.");
        } catch (ResourceAccessException ex) {
            throw new BusinessException("Tempo de resposta da IA excedido. Tente novamente em instantes.");
        }
    }

    private DietaRequest parsearResposta(String json) {
        try {
            DietaRequest dieta = objectMapper.readValue(json, DietaRequest.class);
            if (dieta.getRefeicoes() == null || dieta.getRefeicoes().isEmpty()) {
                throw new BusinessException("A IA não gerou nenhuma refeição. Verifique o prompt do sistema e tente novamente.");
            }
            return dieta;
        } catch (JsonProcessingException ex) {
            log.error("Falha ao parsear resposta da IA: {}", json);
            throw new BusinessException("A IA retornou um formato de resposta inválido. Tente novamente.");
        }
    }

    private boolean isPreenchido(String valor) {
        return valor != null && !valor.isBlank();
    }

    private String formatarDouble(Double valor) {
        if (valor == null) return "N/I";
        return String.format("%.1f", valor);
    }

    private double valorOuZero(BigDecimal valor) {
        return valor != null ? valor.doubleValue() : 0.0;
    }
}
