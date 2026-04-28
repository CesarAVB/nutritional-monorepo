package br.com.sistema.dtos;

import lombok.Data;

@Data
public class ConfiguracaoInfraestruturaRequest {
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
}
