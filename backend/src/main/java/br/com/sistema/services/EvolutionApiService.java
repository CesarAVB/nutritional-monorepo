package br.com.sistema.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Integração com Evolution API para envio de mensagens via WhatsApp.
 * Encapsula chamadas HTTP ao endpoint sendText da Evolution API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EvolutionApiService {

    private final RestTemplate restTemplate;

    /**
     * Envia mensagem de texto via WhatsApp usando Evolution API.
     * Endpoint: POST {url}/message/sendText/{instancia}
     * Header: apikey: {apiKey}
     * Body: { "number": "55XXXXXXXXXXX", "text": "mensagem" }
     *
     * @param url URL base da Evolution API
     * @param instancia identificador da instância Evolution
     * @param apiKey chave de autenticação da Evolution API
     * @param numero telefone WhatsApp do destinatário com código do país (ex: 5511999999999)
     * @param mensagem conteúdo da mensagem a ser enviada
     * @return true se enviado com sucesso, false em caso de falha
     */
    public boolean enviarMensagem(String url, String instancia, String apiKey, String numero, String mensagem) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("number", numero);
            body.put("text", mensagem);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            String endpoint = url.stripTrailing() + "/message/sendText/" + instancia;
            restTemplate.postForObject(endpoint, entity, Object.class);
            return true;
        } catch (Exception ex) {
            log.warn("Falha ao enviar WhatsApp para {}: {}", numero, ex.getMessage());
            return false;
        }
    }
}
