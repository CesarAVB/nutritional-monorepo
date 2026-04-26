CREATE TABLE tbl_uso_ia_log (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_hora      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    provedor       VARCHAR(30) NOT NULL,
    modelo         VARCHAR(100) NOT NULL,
    tokens_entrada INT NOT NULL DEFAULT 0,
    tokens_saida   INT NOT NULL DEFAULT 0,
    custo_usd      DECIMAL(12,6) NOT NULL DEFAULT 0,
    sucesso        BOOLEAN NOT NULL DEFAULT TRUE,
    paciente_id    BIGINT,
    CONSTRAINT fk_uso_ia_paciente FOREIGN KEY (paciente_id)
        REFERENCES tbl_pacientes(id) ON DELETE SET NULL
);
