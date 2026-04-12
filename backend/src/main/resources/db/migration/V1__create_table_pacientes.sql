-- ====================================
-- V1: Criar Tabela de Pacientes
-- ====================================
-- Entidade independente - não conhece consultas

CREATE TABLE tbl_pacientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_completo VARCHAR(255) NOT NULL,
    cpf CHAR(11) NOT NULL UNIQUE,
    data_nascimento DATE NOT NULL,
    telefone_whatsapp VARCHAR(15),
    sexo VARCHAR(10) NOT NULL DEFAULT 'MASCULINO',
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_cpf (cpf),
    INDEX idx_nome (nome_completo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Cadastro de pacientes - Entidade independente';