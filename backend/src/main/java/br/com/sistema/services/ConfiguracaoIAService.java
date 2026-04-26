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

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfiguracaoIAService {

    private static final String PROMPT_SISTEMA_DEFAULT = """
            Você é um nutricionista especialista. Responda APENAS com JSON válido, sem markdown, sem blocos de código, sem texto adicional.
            Estrutura obrigatória do JSON:
            {
              "titulo": "string",
              "objetivo": "string",
              "observacoes": "string",
              "refeicoes": [
                {
                  "tipo": "ENUM_VALOR",
                  "ordemExibicao": 0,
                  "opcoes": [
                    {
                      "numeroOpcao": 1,
                      "alimentos": [
                        { "nome": "string", "quantidade": 0.0, "unidade": "string", "ordem": 0 }
                      ]
                    }
                  ]
                }
              ],
              "suplementos": []
            }
            Tipos válidos para refeição: AO_ACORDAR, DESJEJUM, ALMOCO, LANCHE_DA_TARDE, JANTAR, CEIA.
            Use SOMENTE alimentos da lista TACO fornecida. Nunca inclua alimentos fora dessa lista.
            A soma total de calorias de todas as refeições deve ser próxima do valor de Kcal Total definido.
            """;

    private final ConfiguracaoIARepository repository;

    @Transactional(readOnly = true)
    public ConfiguracaoIAResponse buscar() {
        ConfiguracaoIA config = repository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new BusinessException("Nenhuma configuração de IA encontrada."));
        return toResponse(config);
    }

    @Transactional
    public ConfiguracaoIAResponse salvar(ConfiguracaoIARequest request) {
        ConfiguracaoIA config = repository.findFirstByOrderByIdAsc()
                .orElse(new ConfiguracaoIA());

        config.setProvedor(request.getProvedor());
        config.setModelo(request.getModelo());
        config.setBaseUrl(request.getBaseUrl());
        config.setPromptSistema(request.getPromptSistema());
        config.setTemperaturaModelo(request.getTemperaturaModelo());

        // Só atualiza a apiKey se não for placeholder (evitar sobrescrever com valor mascarado)
        if (!request.getApiKey().contains("...")) {
            config.setApiKey(request.getApiKey());
        }

        return toResponse(repository.save(config));
    }

    @Transactional(readOnly = true)
    public ConfiguracaoIA buscarParaUso() {
        ConfiguracaoIA config = repository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new BusinessException("Nenhuma configuração de IA encontrada. Configure em Configurações > IA."));

        if ("CONFIGURAR".equals(config.getApiKey())) {
            throw new BusinessException("API key não configurada. Acesse Configurações e informe sua chave de API.");
        }

        return config;
    }

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
            log.warn("Teste de conexão com IA falhou: {}", ex.getMessage());
            resultado.put("sucesso", false);
            resultado.put("erro", "Falha na conexão: " + ex.getMessage());
        }
        return resultado;
    }

    public String resolverPromptSistema(ConfiguracaoIA config) {
        String prompt = config.getPromptSistema();
        if (prompt == null || prompt.isBlank()) {
            return PROMPT_SISTEMA_DEFAULT;
        }
        return prompt;
    }

    private ConfiguracaoIAResponse toResponse(ConfiguracaoIA config) {
        return new ConfiguracaoIAResponse(
                config.getId(),
                config.getProvedor(),
                mascarar(config.getApiKey()),
                config.getModelo(),
                config.getBaseUrl(),
                config.getPromptSistema() != null ? config.getPromptSistema() : PROMPT_SISTEMA_DEFAULT,
                config.getTemperaturaModelo()
        );
    }

    private String mascarar(String apiKey) {
        if (apiKey == null || apiKey.length() <= 4 || "CONFIGURAR".equals(apiKey)) {
            return apiKey;
        }
        return apiKey.substring(0, Math.min(6, apiKey.length() - 4)) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
