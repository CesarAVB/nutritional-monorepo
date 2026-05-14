-- ====================================
-- V15: Permitir pacientes incompletos criados pelo agendamento
-- ====================================

ALTER TABLE tbl_pacientes
    MODIFY cpf CHAR(11) NULL,
    MODIFY data_nascimento DATE NULL,
    MODIFY sexo VARCHAR(10) NULL,
    ADD COLUMN cadastro_incompleto BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN origem_agendamento BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_pacientes_cadastro_incompleto ON tbl_pacientes (cadastro_incompleto);
