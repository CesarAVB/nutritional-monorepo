-- ====================================
-- V10: Criar tabela de agendamentos
-- ====================================

CREATE TABLE tbl_agendamentos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  paciente_id BIGINT NOT NULL,
  data_hora_inicio DATETIME NOT NULL,
  data_hora_fim DATETIME NOT NULL,
  duracao_minutos INT NOT NULL,
  tipo ENUM('PRIMEIRA_CONSULTA','RETORNO','AVALIACAO') NOT NULL,
  status ENUM('AGUARDANDO_CONFIRMACAO','CONFIRMADO','REALIZADO','CANCELADO','FALTA') NOT NULL DEFAULT 'AGUARDANDO_CONFIRMACAO',
  observacoes TEXT,
  criado_em DATETIME NOT NULL,
  atualizado_em DATETIME,
  FOREIGN KEY (paciente_id) REFERENCES tbl_pacientes(id),
  INDEX idx_agendamento_data (data_hora_inicio),
  INDEX idx_agendamento_paciente (paciente_id)
);
