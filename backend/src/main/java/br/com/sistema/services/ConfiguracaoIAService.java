package br.com.sistema.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import br.com.sistema.dtos.ConfiguracaoIARequest;
import br.com.sistema.dtos.ConfiguracaoIAResponse;
import br.com.sistema.exceptions.BusinessException;
import br.com.sistema.models.ConfiguracaoIA;
import br.com.sistema.repositories.ConfiguracaoIARepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Gerencia configuracoes de integracao com provedores de IA (OpenAI, OpenRouter).
 * Fornece busca, persistencia, validacao de conexao e resolucao de prompt de sistema.
 * A API key eh mascarada na resposta para protecao de dados sensiveis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfiguracaoIAService {

    private final ConfiguracaoIARepository repository;

    /**
     * Busca a configuracao de IA ativa no sistema.
     * Lanca excecao se nenhuma configuracao estiver cadastrada.
     *
     * @return Configuracao atual com API key mascarada
     */
    @Transactional(readOnly = true)
    public ConfiguracaoIAResponse buscar() {
        ConfiguracaoIA config = repository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new BusinessException("Nenhuma configuracao de IA encontrada."));
        return toResponse(config);
    }

    /**
     * Salva ou atualiza a configuracao de IA.
     * Se a API key enviada contiver placeholder (...), mantem a key existente.
     *
     * @param request Dados da configuracao
     * @return Configuracao salva com API key mascarada
     */
    @Transactional
    public ConfiguracaoIAResponse salvar(ConfiguracaoIARequest request) {
        ConfiguracaoIA config = repository.findFirstByOrderByIdAsc()
                .orElse(new ConfiguracaoIA());

        config.setProvedor(request.getProvedor());
        config.setModelo(request.getModelo());
        config.setBaseUrl(request.getBaseUrl());
        config.setPromptSistema(request.getPromptSistema());
        config.setTemperaturaModelo(request.getTemperaturaModelo());
        config.setPrecoInputPorMilhao(request.getPrecoInputPorMilhao());
        config.setPrecoOutputPorMilhao(request.getPrecoOutputPorMilhao());

        if (!request.getApiKey().contains("...")) {
            config.setApiKey(request.getApiKey());
        }

        return toResponse(repository.save(config));
    }

    /**
     * Busca configuracao validada para uso interno pelos servicos de geracao de dietas.
     * Lanca BusinessException se a API key nao estiver configurada.
     *
     * @return Configuracao de IA pronta para uso
     */
    @Transactional(readOnly = true)
    public ConfiguracaoIA buscarParaUso() {
        ConfiguracaoIA config = repository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new BusinessException("Nenhuma configuracao de IA encontrada. Configure em Configuracoes > IA."));

        if ("CONFIGURAR".equals(config.getApiKey())) {
            throw new BusinessException("API key nao configurada. Acesse Configuracoes e informe sua chave de API.");
        }

        return config;
    }

    /**
     * Valida a conexao com o provedor de IA enviando uma requisicao de teste.
     * Retorna status de sucesso ou mensagem de erro amigavel ao usuario.
     * Adiciona headers especificos para provedores OpenRouter.
     *
     * @return Mapa com resultado do teste (sucesso, modelo, provedor ou erro)
     */
    public Map<String, Object> testarConexao() {
        Map<String, Object> resultado = new HashMap<>();
        try {
            ConfiguracaoIA config = buscarParaUso();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(config.getApiKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (config.getProvedor() != null && config.getProvedor().name().equals("OPENROUTER")) {
                headers.set("HTTP-Referer", "https://app.nutriandrereis.com.br");
                headers.set("X-Title", "NutriAndreReis");
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", config.getModelo());
            payload.put("messages", List.of(Map.of("role", "user", "content", "Responda apenas: OK")));
            payload.put("max_tokens", 5);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForObject(config.getBaseUrl() + "/chat/completions", entity, Map.class);

            resultado.put("sucesso", true);
            resultado.put("modelo", config.getModelo());
            resultado.put("provedor", config.getProvedor());

        } catch (BusinessException ex) {
            resultado.put("sucesso", false);
            resultado.put("erro", ex.getMessage());
        } catch (Exception ex) {
            log.warn("Teste de conexao com IA falhou: {}", ex.getMessage());
            resultado.put("sucesso", false);
            resultado.put("erro", "Falha na conexao: " + ex.getMessage());
        }
        return resultado;
    }

    /**
     * Retorna o prompt de sistema cadastrado no banco.
     * Lanca excecao se nao houver prompt configurado.
     */
    public String resolverPromptSistema(ConfiguracaoIA config) {
        String prompt = config.getPromptSistema();
        if (prompt == null || prompt.isBlank()) {
            throw new BusinessException("Prompt de sistema nao configurado. Acesse Configuracoes > IA e defina o prompt.");
        }
        return prompt;
    }

    /**
     * Converte entidade ConfiguracaoIA para resposta com API key mascarada.
     * Protege credenciais em ambientes de exibicao.
     */
    private ConfiguracaoIAResponse toResponse(ConfiguracaoIA config) {
        return new ConfiguracaoIAResponse(
                config.getId(),
                config.getProvedor(),
                mascarar(config.getApiKey()),
                config.getModelo(),
                config.getBaseUrl(),
                config.getPromptSistema(),
                config.getTemperaturaModelo(),
                config.getPrecoInputPorMilhao(),
                config.getPrecoOutputPorMilhao()
        );
    }

    /**
     * Mascara API key mostrando apenas os 6 primeiros e 4 ultimos caracteres.
     * Retorna null ou placeholder inalterados para configuracoes pendentes.
     */
    private String mascarar(String apiKey) {
        if (apiKey == null || apiKey.length() <= 4 || "CONFIGURAR".equals(apiKey)) {
            return apiKey;
        }
        return apiKey.substring(0, Math.min(6, apiKey.length() - 4)) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
