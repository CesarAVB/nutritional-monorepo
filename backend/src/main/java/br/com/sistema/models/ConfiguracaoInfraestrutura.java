package br.com.sistema.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_configuracoes_infraestrutura")
public class ConfiguracaoInfraestrutura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_host")
    private String emailHost;
    @Column(name = "email_port")
    private Integer emailPort;
    @Column(name = "email_username")
    private String emailUsername;
    @Column(name = "email_password", columnDefinition = "TEXT")
    private String emailPassword;
    @Column(name = "email_habilitado", nullable = false)
    private Boolean emailHabilitado = false;

    @Column(name = "rabbitmq_host")
    private String rabbitmqHost;
    @Column(name = "rabbitmq_port")
    private Integer rabbitmqPort;
    @Column(name = "rabbitmq_username")
    private String rabbitmqUsername;
    @Column(name = "rabbitmq_password", columnDefinition = "TEXT")
    private String rabbitmqPassword;
    @Column(name = "rabbitmq_vhost")
    private String rabbitmqVhost;
    @Column(name = "rabbitmq_queue")
    private String rabbitmqQueue;
    @Column(name = "rabbitmq_habilitado", nullable = false)
    private Boolean rabbitmqHabilitado = false;

    @Column(name = "minio_endpoint")
    private String minioEndpoint;
    @Column(name = "minio_access_key")
    private String minioAccessKey;
    @Column(name = "minio_secret_key", columnDefinition = "TEXT")
    private String minioSecretKey;
    @Column(name = "minio_bucket_name")
    private String minioBucketName;
    @Column(name = "minio_region")
    private String minioRegion;
    @Column(name = "minio_habilitado", nullable = false)
    private Boolean minioHabilitado = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
