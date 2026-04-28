package br.com.sistema.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import br.com.sistema.dtos.ConfiguracaoAgendamentoRequestDto;
import br.com.sistema.dtos.ConfiguracaoAgendamentoResponseDto;
import br.com.sistema.exceptions.BusinessException;
import br.com.sistema.models.ConfiguracaoAgendamento;
import br.com.sistema.repositories.ConfiguracaoAgendamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Gerencia configuracoes de agendamento de consultas.
 * Fornece busca, persistencia de parametros de funcionamento (dias, horarios, lembretes)
 * e validacao de integracao com Evolution API para envio de notificacoes via WhatsApp.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfiguracaoAgendamentoService {

    private final ConfiguracaoAgendamentoRepository repository;
    private final RestTemplate restTemplate;

    /**
     * Busca a configuracao de agendamento ativa no sistema.
     * A API key da Evolution eh mascarada para protecao de dados sensiveis.
     *
     * @return Configuracao atual com evolutionApiKey mascarada
     */
    @Transactional(readOnly = true)
    public ConfiguracaoAgendamentoResponseDto buscar() {
        ConfiguracaoAgendamento config = carregarSingleton();
        return toResponse(config);
    }

    /**
     * Salva ou atualiza a configuracao de agendamento.
     * Se a API key enviada contiver placeholder (...), mantem a key existente.
     *
     * @param request Dados da configuracao
     * @return Configuracao salva com API key mascarada
     */
    @Transactional
    public ConfiguracaoAgendamentoResponseDto salvar(ConfiguracaoAgendamentoRequestDto request) {
        ConfiguracaoAgendamento config = carregarSingleton();

        config.setDuracaoPadraoMinutos(request.getDuracaoPadraoMinutos());
        config.setIntervaloEntreConsultasMinutos(request.getIntervaloEntreConsultasMinutos());
        config.setDiasAtendimento(request.getDiasAtendimento());
        config.setHorarioInicio(request.getHorarioInicio());
        config.setHorarioFim(request.getHorarioFim());
        config.setFusoHorario(request.getFusoHorario());
        config.setJanelaNotifInicio(request.getJanelaNotifInicio());
        config.setJanelaNotifFim(request.getJanelaNotifFim());
        config.setEmailNotificacao(request.getEmailNotificacao());
        config.setNotifAoCriar(request.getNotifAoCriar());
        config.setNotifAoCancelar(request.getNotifAoCancelar());
        config.setAlertaNaoConfirmacao(request.getAlertaNaoConfirmacao());
        config.setLembreteImediatoAtivo(request.getLembreteImediatoAtivo());
        config.setLembrete72hAtivo(request.getLembrete72hAtivo());
        config.setLembrete24hAtivo(request.getLembrete24hAtivo());
        config.setLembrete2hAtivo(request.getLembrete2hAtivo());
        config.setEvolutionUrl(request.getEvolutionUrl());
        config.setEvolutionInstancia(request.getEvolutionInstancia());

        // Preservar chave AES-encrypted se vier mascarada
        if (request.getEvolutionApiKey() != null && !request.getEvolutionApiKey().contains("...")) {
            config.setEvolutionApiKey(request.getEvolutionApiKey());
        }

        config.setTemplateConfirmacao(request.getTemplateConfirmacao());
        config.setTemplateLembrete72h(request.getTemplateLembrete72h());
        config.setTemplateLembrete24h(request.getTemplateLembrete24h());
        config.setTemplateLembrete2h(request.getTemplateLembrete2h());
        config.setTemplateConsultaConfirmada(request.getTemplateConsultaConfirmada());
        config.setTemplateConsultaCancelada(request.getTemplateConsultaCancelada());

        return toResponse(repository.save(config));
    }

    /**
     * Busca configuracao validada para uso interno pelos servicos de notificacao.
     * Retorna a entidade com chave real (nao mascarada).
     *
     * @return Configuracao de agendamento pronta para uso
     */
    @Transactional(readOnly = true)
    public ConfiguracaoAgendamento buscarParaUso() {
        return carregarSingleton();
    }

    /**
     * Valida a conexao com a instancia Evolution API configurada.
     * Chama GET {evolutionUrl}/instance/fetchInstances com header apikey.
     * Retorna status de sucesso ou mensagem de erro amigavel ao usuario.
     *
     * @return Mapa com resultado do teste (sucesso, instancia, status ou erro)
     */
    public Map<String, Object> testarConexaoWhatsapp() {
        Map<String, Object> resultado = new HashMap<>();
        try {
            ConfiguracaoAgendamento config = carregarSingleton();

            if (config.getEvolutionUrl() == null || config.getEvolutionUrl().isBlank()) {
                resultado.put("conectado", false);
                resultado.put("mensagem", "URL da Evolution API não configurada");
                return resultado;
            }
            if (config.getEvolutionApiKey() == null || config.getEvolutionApiKey().isBlank()) {
                resultado.put("conectado", false);
                resultado.put("mensagem", "API Key da Evolution não configurada");
                return resultado;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", config.getEvolutionApiKey());
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = config.getEvolutionUrl().stripTrailing() + "/instance/fetchInstances";
            restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);

            resultado.put("conectado", true);
            resultado.put("mensagem", "Conexão com Evolution API estabelecida com sucesso");

        } catch (Exception ex) {
            log.warn("Teste de conexao com Evolution API falhou: {}", ex.getMessage());
            resultado.put("conectado", false);
            resultado.put("mensagem", "Falha na conexão: " + ex.getMessage());
        }
        return resultado;
    }

    /**
     * Carrega configuracao singleton (id=1) do banco.
     * Lanca BusinessException se nao existir.
     */
    private ConfiguracaoAgendamento carregarSingleton() {
        return repository.findById(1L)
                .orElseThrow(() -> new BusinessException("Configuracao de agendamento nao encontrada"));
    }

    /**
     * Converte entidade ConfiguracaoAgendamento para resposta com API key mascarada.
     * Protege credenciais em ambientes de exibicao.
     */
    private ConfiguracaoAgendamentoResponseDto toResponse(ConfiguracaoAgendamento config) {
        ConfiguracaoAgendamentoResponseDto dto = new ConfiguracaoAgendamentoResponseDto();
        dto.setId(config.getId());
        dto.setDuracaoPadraoMinutos(config.getDuracaoPadraoMinutos());
        dto.setIntervaloEntreConsultasMinutos(config.getIntervaloEntreConsultasMinutos());
        dto.setDiasAtendimento(config.getDiasAtendimento());
        dto.setHorarioInicio(config.getHorarioInicio());
        dto.setHorarioFim(config.getHorarioFim());
        dto.setFusoHorario(config.getFusoHorario());
        dto.setJanelaNotifInicio(config.getJanelaNotifInicio());
        dto.setJanelaNotifFim(config.getJanelaNotifFim());
        dto.setEmailNotificacao(config.getEmailNotificacao());
        dto.setNotifAoCriar(config.getNotifAoCriar());
        dto.setNotifAoCancelar(config.getNotifAoCancelar());
        dto.setAlertaNaoConfirmacao(config.getAlertaNaoConfirmacao());
        dto.setLembreteImediatoAtivo(config.getLembreteImediatoAtivo());
        dto.setLembrete72hAtivo(config.getLembrete72hAtivo());
        dto.setLembrete24hAtivo(config.getLembrete24hAtivo());
        dto.setLembrete2hAtivo(config.getLembrete2hAtivo());
        dto.setEvolutionUrl(config.getEvolutionUrl());
        dto.setEvolutionInstancia(config.getEvolutionInstancia());
        dto.setEvolutionApiKey(mascarar(config.getEvolutionApiKey()));
        dto.setTemplateConfirmacao(config.getTemplateConfirmacao());
        dto.setTemplateLembrete72h(config.getTemplateLembrete72h());
        dto.setTemplateLembrete24h(config.getTemplateLembrete24h());
        dto.setTemplateLembrete2h(config.getTemplateLembrete2h());
        dto.setTemplateConsultaConfirmada(config.getTemplateConsultaConfirmada());
        dto.setTemplateConsultaCancelada(config.getTemplateConsultaCancelada());
        return dto;
    }

    /**
     * Mascara API key mostrando apenas os 6 primeiros e 4 ultimos caracteres.
     * Retorna null ou chaves curtas inalteradas.
     */
    private String mascarar(String apiKey) {
        if (apiKey == null || apiKey.length() <= 4) {
            return apiKey;
        }
        return apiKey.substring(0, Math.min(6, apiKey.length() - 4)) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
