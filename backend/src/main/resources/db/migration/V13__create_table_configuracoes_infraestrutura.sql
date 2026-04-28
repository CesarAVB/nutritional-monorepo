CREATE TABLE tbl_configuracoes_infraestrutura (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  -- Email
  email_host VARCHAR(200),
  email_port INT DEFAULT 587,
  email_username VARCHAR(200),
  email_password TEXT,
  email_habilitado BOOLEAN NOT NULL DEFAULT FALSE,
  -- RabbitMQ
  rabbitmq_host VARCHAR(200),
  rabbitmq_port INT DEFAULT 5672,
  rabbitmq_username VARCHAR(200),
  rabbitmq_password TEXT,
  rabbitmq_vhost VARCHAR(100) DEFAULT '/',
  rabbitmq_queue VARCHAR(200) DEFAULT 'audit_events_queue',
  rabbitmq_habilitado BOOLEAN NOT NULL DEFAULT FALSE,
  -- MinIO/S3
  minio_endpoint VARCHAR(500),
  minio_access_key VARCHAR(200),
  minio_secret_key TEXT,
  minio_bucket_name VARCHAR(200),
  minio_region VARCHAR(100) DEFAULT 'us-east-1',
  minio_habilitado BOOLEAN NOT NULL DEFAULT FALSE,
  -- Controle
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO tbl_configuracoes_infraestrutura (
  email_habilitado, rabbitmq_habilitado, minio_habilitado
) VALUES (FALSE, FALSE, FALSE);
