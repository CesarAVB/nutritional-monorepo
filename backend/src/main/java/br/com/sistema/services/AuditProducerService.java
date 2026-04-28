package br.com.sistema.services;

import br.com.sistema.models.AuditEventMessage;
import br.com.sistema.models.ConfiguracaoInfraestrutura;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Produz eventos de auditoria para o RabbitMQ.
 * Configuração dinâmica via banco de dados.
 */
@Service
@Slf4j
public class AuditProducerService {

    private final ObjectMapper objectMapper;
    private RabbitTemplate rabbitTemplate;
    private String auditQueueName = "audit_events_queue";
    private CachingConnectionFactory connectionFactory;

    public AuditProducerService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        if (!objectMapper.getRegisteredModuleIds().contains(JavaTimeModule.class.getName())) {
            objectMapper.registerModule(new JavaTimeModule());
        }
    }

    /**
     * Reconfigura o serviço de auditoria com base nas configurações de infraestrutura.
     * Se desabilitado, limpa a configuração existente.
     *
     * @param config configuração de infraestrutura do banco
     */
    public void reconfigurar(ConfiguracaoInfraestrutura config) {
        if (connectionFactory != null) {
            try { connectionFactory.destroy(); } catch (Exception ignored) {}
        }
        if (!Boolean.TRUE.equals(config.getRabbitmqHabilitado())) {
            this.rabbitTemplate = null;
            this.connectionFactory = null;
            log.info("AuditProducerService: RabbitMQ desabilitado.");
            return;
        }
        CachingConnectionFactory cf = new CachingConnectionFactory();
        cf.setHost(config.getRabbitmqHost());
        cf.setPort(config.getRabbitmqPort() != null ? config.getRabbitmqPort() : 5672);
        cf.setUsername(config.getRabbitmqUsername());
        cf.setPassword(config.getRabbitmqPassword());
        if (config.getRabbitmqVhost() != null && !config.getRabbitmqVhost().isBlank()) {
            cf.setVirtualHost(config.getRabbitmqVhost());
        }
        this.connectionFactory = cf;
        this.rabbitTemplate = new RabbitTemplate(cf);
        this.auditQueueName = config.getRabbitmqQueue() != null ? config.getRabbitmqQueue() : "audit_events_queue";
        log.info("AuditProducerService reconfigurado - host: {}", config.getRabbitmqHost());
    }

    /**
     * Envia um evento de auditoria para a fila do RabbitMQ.
     * Falhas de serializacao ou envio sao registradas e nao lancam excecao.
     *
     * @param eventMessage Evento de auditoria a ser enviado
     */
    public void sendAuditEvent(AuditEventMessage eventMessage) {
        if (rabbitTemplate == null) {
            log.warn("AuditProducerService: RabbitMQ não configurado. Evento ignorado: {}",
                eventMessage != null ? eventMessage.getEventType() : "null");
            return;
        }
        try {
            String jsonEvent = objectMapper.writeValueAsString(eventMessage);
            rabbitTemplate.convertAndSend(auditQueueName, jsonEvent);
            log.info("Evento de auditoria enviado para '{}': {}", auditQueueName, eventMessage.getEventType());
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar evento de auditoria: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro ao enviar evento para RabbitMQ: {}", e.getMessage(), e);
        }
    }
}
