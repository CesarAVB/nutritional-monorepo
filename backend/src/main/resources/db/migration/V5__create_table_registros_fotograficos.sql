-- ====================================
-- V5: Criar Tabela de Registros Fotográficos
-- ====================================
-- Relacionamento unidirecional: RegistroFotografico → Consulta (OneToOne)

CREATE TABLE tbl_registros_fotograficos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consulta_id BIGINT NOT NULL,
    
    foto_anterior VARCHAR(500) COMMENT 'URL/caminho da foto frontal',
    foto_posterior VARCHAR(500) COMMENT 'URL/caminho da foto das costas',
    foto_lateral_esquerda VARCHAR(500),
    foto_lateral_direita VARCHAR(500),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_registro_consulta 
        FOREIGN KEY (consulta_id) 
        REFERENCES tbl_consultas(id) 
        ON DELETE CASCADE,
    
    INDEX idx_consulta_id (consulta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Fotos - Conhece a Consulta (relacionamento unidirecional)';