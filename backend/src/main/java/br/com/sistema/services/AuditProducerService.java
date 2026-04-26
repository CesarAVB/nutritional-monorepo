package br.com.sistema.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import br.com.sistema.models.AuditEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Produz eventos de auditoria para o RabbitMQ.
 * Serializa AuditEventMessage para JSON e envia para a fila configurada via audit.queue.name.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditProducerService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${audit.queue.name:audit_events_queue}")
    private String auditQueueName;

    /**
     * Configura o ObjectMapper com JavaTimeModule para serializar LocalDateTime corretamente.
     */
    @PostConstruct
    public void setupObjectMapper() {
        if (!objectMapper.getRegisteredModuleIds().contains(JavaTimeModule.class.getName())) {
            objectMapper.registerModule(new JavaTimeModule());
            log.info("JavaTimeModule registrado no ObjectMapper do AuditProducerService.");
        }
    }

    /**
     * Envia um evento de auditoria para a fila do RabbitMQ.
     * Falhas de serializacao ou envio sao registradas e nao lancam excecao.
     *
     * @param eventMessage Evento de auditoria a ser enviado
     */
    public void sendAuditEvent(AuditEventMessage eventMessage) {
        try {
            String jsonEvent = objectMapper.writeValueAsString(eventMessage);
            rabbitTemplate.convertAndSend(auditQueueName, jsonEvent);
            log.info("Evento de auditoria enviado para a fila '{}': {}", auditQueueName, eventMessage.getEventType());
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar evento de auditoria para JSON: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro ao enviar evento de auditoria para o RabbitMQ: {}", e.getMessage(), e);
        }
    }
}
