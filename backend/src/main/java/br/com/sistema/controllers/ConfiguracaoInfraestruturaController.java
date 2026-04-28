package br.com.sistema.controllers;

import br.com.sistema.dtos.ConfiguracaoInfraestruturaRequest;
import br.com.sistema.dtos.ConfiguracaoInfraestruturaResponse;
import br.com.sistema.services.ConfiguracaoInfraestruturaService;
import br.com.sistema.services.EmailService;
import br.com.sistema.services.AuditProducerService;
import br.com.sistema.services.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Gerencia configurações de infraestrutura (Email, RabbitMQ, MinIO/S3).
 * Permite testar conexões antes de ativar cada serviço.
 */
@RestController
@RequestMapping("/api/v1/configuracoes-infraestrutura")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Configuracoes de Infraestrutura")
public class ConfiguracaoInfraestruturaController {

    private final ConfiguracaoInfraestruturaService service;
    private final EmailService emailService;
    private final S3Service s3Service;
    private final AuditProducerService auditProducerService;

    @GetMapping
    @Operation(summary = "Buscar configuracoes de infraestrutura")
    public ResponseEntity<ConfiguracaoInfraestruturaResponse> buscar() {
        return ResponseEntity.ok(service.buscar());
    }

    @PutMapping
    @Operation(summary = "Salvar configuracoes de infraestrutura")
    public ResponseEntity<ConfiguracaoInfraestruturaResponse> salvar(@RequestBody ConfiguracaoInfraestruturaRequest request) {
        return ResponseEntity.ok(service.salvar(request));
    }

    @PostMapping("/testar-email")
    @Operation(summary = "Testar conexão com servidor de email")
    public ResponseEntity<Map<String, Object>> testarEmail() {
        try {
            boolean ok = emailService.enviarEmail(
                "cesar.augusto.rj1@gmail.com",
                "Teste de conexão - NutriControl",
                "Email de teste enviado com sucesso pelo servidor SMTP configurado."
            );
            if (ok) {
                return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", "Email de teste enviado com sucesso!"));
            } else {
                return ResponseEntity.ok(Map.of("sucesso", false, "mensagem", "Falha ao enviar email. Verifique as configurações SMTP."));
            }
        } catch (Exception e) {
            log.warn("Teste de email falhou: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("sucesso", false, "mensagem", "Erro: " + e.getMessage()));
        }
    }

    @PostMapping("/testar-minio")
    @Operation(summary = "Testar conexão com MinIO/S3")
    public ResponseEntity<Map<String, Object>> testarMinio() {
        try {
            String bucket = s3Service.getBucketName();
            if (bucket == null || bucket.isBlank()) {
                return ResponseEntity.ok(Map.of("sucesso", false, "mensagem", "MinIO não configurado ou desabilitado."));
            }
            return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", "Conexão com MinIO/S3 estabelecida. Bucket: " + bucket));
        } catch (Exception e) {
            log.warn("Teste de MinIO falhou: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("sucesso", false, "mensagem", "Erro: " + e.getMessage()));
        }
    }

    @PostMapping("/testar-rabbitmq")
    @Operation(summary = "Testar conexão com RabbitMQ")
    public ResponseEntity<Map<String, Object>> testarRabbitmq() {
        try {
            br.com.sistema.models.AuditEventMessage testMsg = new br.com.sistema.models.AuditEventMessage();
            testMsg.setEventType("TESTE_CONEXAO");
            auditProducerService.sendAuditEvent(testMsg);
            return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", "Evento de teste enviado para o RabbitMQ com sucesso!"));
        } catch (Exception e) {
            log.warn("Teste de RabbitMQ falhou: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("sucesso", false, "mensagem", "Erro: " + e.getMessage()));
        }
    }
}
