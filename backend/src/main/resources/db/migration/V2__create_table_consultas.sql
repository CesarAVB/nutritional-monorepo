-- ====================================
-- V2: Criar Tabela de Consultas
-- ====================================
-- Relacionamento unidirecional: Consulta → Paciente (ManyToOne)

CREATE TABLE tbl_consultas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    data_consulta DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_consulta_paciente 
        FOREIGN KEY (paciente_id) 
        REFERENCES tbl_pacientes(id) 
        ON DELETE CASCADE,
    
    INDEX idx_paciente_id (paciente_id),
    INDEX idx_data_consulta (data_consulta),
    INDEX idx_paciente_data (paciente_id, data_consulta DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Consultas - Conhece o Paciente (relacionamento unidirecional)';