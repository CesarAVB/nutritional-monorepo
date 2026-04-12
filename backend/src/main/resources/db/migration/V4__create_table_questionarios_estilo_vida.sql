-- ====================================
-- V4: Criar Tabela de Questionários de Estilo de Vida
-- ====================================

CREATE TABLE tbl_questionarios_estilo_vida (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consulta_id BIGINT NOT NULL,

    -- Rotina
    objetivo VARCHAR(500),
    frequencia_treino VARCHAR(255),
    tempo_treino VARCHAR(255),

    -- Saúde
    cirurgias TEXT,
    doencas TEXT,
    historico_familiar TEXT,
    medicamentos TEXT,
    suplementos TEXT,
    uso_anabolizantes VARCHAR(255),
    ciclo_anabolizantes VARCHAR(255),
    duracao_anabolizantes VARCHAR(255),

    -- Hábitos
    fuma BOOLEAN DEFAULT FALSE,
    frequencia_alcool VARCHAR(255),
    funcionamento_intestino VARCHAR(255),
    qualidade_sono VARCHAR(255),
    ingestao_agua_diaria DOUBLE,

    -- Preferências
    alimentos_nao_gosta TEXT,
    frutas_preferidas VARCHAR(500),
    numero_refeicoes_desejadas INT,
    horario_maior_fome VARCHAR(255),

    -- Clínico
    pressao_arterial VARCHAR(50),
    intolerancias TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_questionario_consulta
        FOREIGN KEY (consulta_id)
        REFERENCES tbl_consultas(id)
        ON DELETE CASCADE,

    INDEX idx_consulta_id (consulta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Questionários - Conhece a Consulta (relacionamento unidirecional)';