-- ====================================
-- V6: Criar Tabelas de Dietas
-- ====================================

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS tbl_alimentos_refeicao;
DROP TABLE IF EXISTS tbl_refeicao_opcoes;
DROP TABLE IF EXISTS tbl_refeicoes;
DROP TABLE IF EXISTS tbl_suplementos_dieta;
DROP TABLE IF EXISTS tbl_dietas;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE tbl_dietas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,

    titulo VARCHAR(200),
    data_criacao DATE NOT NULL,
    objetivo VARCHAR(500),
    kcal_total INT,
    proteinas_g DECIMAL(8,2),
    carboidratos_g DECIMAL(8,2),
    gorduras_g DECIMAL(8,2),
    ingestao_agua_litros DECIMAL(4,2),
    observacoes TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_dieta_paciente
        FOREIGN KEY (paciente_id)
        REFERENCES tbl_pacientes(id)
        ON DELETE CASCADE,

    INDEX idx_dieta_paciente_id (paciente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tbl_refeicoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dieta_id BIGINT NOT NULL,

    tipo VARCHAR(30) NOT NULL,
    ordem_exibicao INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_refeicao_dieta
        FOREIGN KEY (dieta_id)
        REFERENCES tbl_dietas(id)
        ON DELETE CASCADE,

    INDEX idx_refeicao_dieta_id (dieta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tbl_refeicao_opcoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    refeicao_id BIGINT NOT NULL,

    numero_opcao INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_opcao_refeicao
        FOREIGN KEY (refeicao_id)
        REFERENCES tbl_refeicoes(id)
        ON DELETE CASCADE,

    INDEX idx_opcao_refeicao_id (refeicao_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tbl_alimentos_refeicao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    opcao_id BIGINT NOT NULL,

    nome VARCHAR(300) NOT NULL,
    quantidade DECIMAL(8,2),
    unidade VARCHAR(50),
    ordem INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_alimento_opcao
        FOREIGN KEY (opcao_id)
        REFERENCES tbl_refeicao_opcoes(id)
        ON DELETE CASCADE,

    INDEX idx_alimento_opcao_id (opcao_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tbl_suplementos_dieta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dieta_id BIGINT NOT NULL,

    nome VARCHAR(200) NOT NULL,
    dosagem VARCHAR(100),
    timing VARCHAR(200),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_suplemento_dieta
        FOREIGN KEY (dieta_id)
        REFERENCES tbl_dietas(id)
        ON DELETE CASCADE,

    INDEX idx_suplemento_dieta_id (dieta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
