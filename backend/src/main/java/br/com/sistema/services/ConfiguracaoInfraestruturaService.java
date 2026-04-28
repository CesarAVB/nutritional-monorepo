package br.com.sistema.services;

import br.com.sistema.dtos.ConfiguracaoInfraestruturaRequest;
import br.com.sistema.dtos.ConfiguracaoInfraestruturaResponse;
import br.com.sistema.models.ConfiguracaoInfraestrutura;
import br.com.sistema.repositories.ConfiguracaoInfraestruturaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gerencia configurações de infraestrutura (Email, RabbitMQ, MinIO/S3).
 * Aplica configurações dinamicamente aos serviços dependentes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfiguracaoInfraestruturaService {

    private final ConfiguracaoInfraestruturaRepository repository;
    private final EmailService emailService;
    private final AuditProducerService auditProducerService;
    private final S3Service s3Service;

    /**
     * Aplica configurações de infraestrutura do banco ao inicializar a aplicação.
     */
    @PostConstruct
    public void aplicarConfiguracoes() {
        try {
            ConfiguracaoInfraestrutura config = repository.findFirstBy()
                .orElseGet(() -> repository.save(new ConfiguracaoInfraestrutura()));
            emailService.reconfigurar(config);
            auditProducerService.reconfigurar(config);
            s3Service.reconfigurar(config);
            log.info("Configurações de infraestrutura aplicadas na inicialização.");
        } catch (Exception e) {
            log.warn("Não foi possível aplicar configurações de infraestrutura: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ConfiguracaoInfraestruturaResponse buscar() {
        ConfiguracaoInfraestrutura config = repository.findFirstBy()
            .orElseGet(() -> repository.save(new ConfiguracaoInfraestrutura()));
        return toResponse(config);
    }

    @Transactional
    public ConfiguracaoInfraestruturaResponse salvar(ConfiguracaoInfraestruturaRequest request) {
        ConfiguracaoInfraestrutura config = repository.findFirstBy()
            .orElse(new ConfiguracaoInfraestrutura());

        config.setEmailHost(request.getEmailHost());
        config.setEmailPort(request.getEmailPort());
        config.setEmailUsername(request.getEmailUsername());
        config.setEmailHabilitado(request.getEmailHabilitado());
        config.setRabbitmqHost(request.getRabbitmqHost());
        config.setRabbitmqPort(request.getRabbitmqPort());
        config.setRabbitmqUsername(request.getRabbitmqUsername());
        config.setRabbitmqVhost(request.getRabbitmqVhost());
        config.setRabbitmqQueue(request.getRabbitmqQueue());
        config.setRabbitmqHabilitado(request.getRabbitmqHabilitado());
        config.setMinioEndpoint(request.getMinioEndpoint());
        config.setMinioAccessKey(request.getMinioAccessKey());
        config.setMinioBucketName(request.getMinioBucketName());
        config.setMinioRegion(request.getMinioRegion());
        config.setMinioHabilitado(request.getMinioHabilitado());

        // Preserve senhas se não foram alteradas (placeholder "***")
        if (request.getEmailPassword() != null && !request.getEmailPassword().equals("***")) {
            config.setEmailPassword(request.getEmailPassword());
        }
        if (request.getRabbitmqPassword() != null && !request.getRabbitmqPassword().equals("***")) {
            config.setRabbitmqPassword(request.getRabbitmqPassword());
        }
        if (request.getMinioSecretKey() != null && !request.getMinioSecretKey().equals("***")) {
            config.setMinioSecretKey(request.getMinioSecretKey());
        }

        config = repository.save(config);
        emailService.reconfigurar(config);
        auditProducerService.reconfigurar(config);
        s3Service.reconfigurar(config);
        return toResponse(config);
    }

    private ConfiguracaoInfraestruturaResponse toResponse(ConfiguracaoInfraestrutura config) {
        return ConfiguracaoInfraestruturaResponse.builder()
            .id(config.getId())
            .emailHost(config.getEmailHost())
            .emailPort(config.getEmailPort())
            .emailUsername(config.getEmailUsername())
            .emailPassword(mascarar(config.getEmailPassword()))
            .emailHabilitado(config.getEmailHabilitado())
            .rabbitmqHost(config.getRabbitmqHost())
            .rabbitmqPort(config.getRabbitmqPort())
            .rabbitmqUsername(config.getRabbitmqUsername())
            .rabbitmqPassword(mascarar(config.getRabbitmqPassword()))
            .rabbitmqVhost(config.getRabbitmqVhost())
            .rabbitmqQueue(config.getRabbitmqQueue())
            .rabbitmqHabilitado(config.getRabbitmqHabilitado())
            .minioEndpoint(config.getMinioEndpoint())
            .minioAccessKey(config.getMinioAccessKey())
            .minioSecretKey(mascarar(config.getMinioSecretKey()))
            .minioBucketName(config.getMinioBucketName())
            .minioRegion(config.getMinioRegion())
            .minioHabilitado(config.getMinioHabilitado())
            .updatedAt(config.getUpdatedAt())
            .build();
    }

    private String mascarar(String valor) {
        if (valor == null || valor.isBlank()) return valor;
        return "***";
    }
}
