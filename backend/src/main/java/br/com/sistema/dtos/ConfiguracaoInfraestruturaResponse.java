package br.com.sistema.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracaoInfraestruturaResponse {
    private Long id;

    private String emailHost;
    private Integer emailPort;
    private String emailUsername;
    private String emailPassword;
    private Boolean emailHabilitado;

    private String rabbitmqHost;
    private Integer rabbitmqPort;
    private String rabbitmqUsername;
    private String rabbitmqPassword;
    private String rabbitmqVhost;
    private String rabbitmqQueue;
    private Boolean rabbitmqHabilitado;

    private String minioEndpoint;
    private String minioAccessKey;
    private String minioSecretKey;
    private String minioBucketName;
    private String minioRegion;
    private Boolean minioHabilitado;

    private LocalDateTime updatedAt;
}
