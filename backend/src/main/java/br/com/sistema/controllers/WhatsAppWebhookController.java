package br.com.sistema.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.models.ConfiguracaoAgendamento;
import br.com.sistema.services.AgendamentoService;
import br.com.sistema.services.ConfiguracaoAgendamentoService;
import br.com.sistema.services.EvolutionApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Endpoint público para receber eventos webhook da Evolution API.
 * Processa mensagens de confirmação e cancelamento de agendamentos via WhatsApp.
 *
 * <p>Nota operacional: Endpoint sem autenticação para permitir chamadas da Evolution API.</p>
 */
@RestController
@RequestMapping("/webhook/whatsapp")
@RequiredArgsConstructor
@Slf4j
public class WhatsAppWebhookController {

    private final AgendamentoService agendamentoService;
    private final EvolutionApiService evolutionApiService;
    private final ConfiguracaoAgendamentoService configuracaoService;

    /**
     * Recebe eventos do webhook da Evolution API.
     * Processa apenas messages.upsert com fromMe=false.
     * Responde com mensagem de feedback ao paciente.
     *
     * @param payload evento webhook no formato Evolution API
     * @return resposta HTTP 200 OK sempre, independente do processamento
     */
    @SuppressWarnings("unchecked")
    @PostMapping
    public ResponseEntity<Void> receberEvento(@RequestBody Map<String, Object> payload) {
        try {
            String event = (String) payload.get("event");
            if (!"messages.upsert".equals(event)) {
                return ResponseEntity.ok().build();
            }

            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data == null) return ResponseEntity.ok().build();

            Map<String, Object> key = (Map<String, Object>) data.get("key");
            if (key == null) return ResponseEntity.ok().build();

            Boolean fromMe = (Boolean) key.get("fromMe");
            if (Boolean.TRUE.equals(fromMe)) return ResponseEntity.ok().build();

            // Extrai número: "5511999999999@s.whatsapp.net" → "5511999999999"
            String remoteJid = (String) key.get("remoteJid");
            if (remoteJid == null) return ResponseEntity.ok().build();
            String numero = remoteJid.replace("@s.whatsapp.net", "").replace("@g.us", "");

            // Extrai texto da mensagem
            Map<String, Object> message = (Map<String, Object>) data.get("message");
            if (message == null) return ResponseEntity.ok().build();
            String texto = (String) message.get("conversation");
            if (texto == null) texto = (String) message.getOrDefault("extendedTextMessage", null);
            if (texto == null) return ResponseEntity.ok().build();

            texto = texto.trim().toUpperCase();

            ConfiguracaoAgendamento config = configuracaoService.buscarParaUso();
            String feedback;

            if ("CONFIRMAR".equals(texto)) {
                agendamentoService.confirmarPorWhatsapp(numero);
                String tpl = config.getTemplateConsultaConfirmada();
                feedback = (tpl != null && !tpl.isBlank()) ? tpl : "Sua consulta foi confirmada! Até lá.";
            } else if ("CANCELAR".equals(texto)) {
                agendamentoService.cancelarPorWhatsapp(numero);
                String tpl = config.getTemplateConsultaCancelada();
                feedback = (tpl != null && !tpl.isBlank()) ? tpl : "Sua consulta foi cancelada. Para reagendar, entre em contato.";
            } else {
                return ResponseEntity.ok().build();
            }

            // Envia feedback ao paciente
            if (config.getEvolutionUrl() != null && config.getEvolutionApiKey() != null) {
                evolutionApiService.enviarMensagem(
                    config.getEvolutionUrl(), config.getEvolutionInstancia(),
                    config.getEvolutionApiKey(), numero, feedback);
            }

        } catch (Exception ex) {
            log.warn("Erro ao processar webhook WhatsApp: {}", ex.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
