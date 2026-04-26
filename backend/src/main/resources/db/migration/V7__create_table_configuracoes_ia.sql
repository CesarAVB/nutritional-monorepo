CREATE TABLE tbl_configuracoes_ia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provedor VARCHAR(30) NOT NULL DEFAULT 'OPENAI',
    api_key TEXT NOT NULL,
    modelo VARCHAR(100) NOT NULL DEFAULT 'gpt-4o',
    base_url VARCHAR(500),
    prompt_sistema TEXT,
    temperatura_modelo DECIMAL(3,2) NOT NULL DEFAULT 0.70,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO tbl_configuracoes_ia (provedor, api_key, modelo, base_url, temperatura_modelo)
VALUES ('OPENAI', 'CONFIGURAR', 'gpt-4o', 'https://api.openai.com/v1', 0.70);
