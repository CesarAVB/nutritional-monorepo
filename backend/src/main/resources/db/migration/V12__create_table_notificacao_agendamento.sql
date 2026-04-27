-- ====================================
-- V12: Criar tabela de notificações de agendamento
-- ====================================

CREATE TABLE tbl_notificacao_agendamento (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  agendamento_id BIGINT NOT NULL,
  tipo ENUM('IMEDIATO','LEMBRETE_72H','LEMBRETE_24H','LEMBRETE_2H','ALERTA_NUTRICIONISTA') NOT NULL,
  canal ENUM('WHATSAPP','EMAIL') NOT NULL,
  status ENUM('PENDENTE','ENVIADO','FALHOU') NOT NULL DEFAULT 'PENDENTE',
  enviado_em DATETIME,
  mensagem TEXT,
  erro TEXT,
  FOREIGN KEY (agendamento_id) REFERENCES tbl_agendamentos(id),
  INDEX idx_notif_agendamento (agendamento_id),
  INDEX idx_notif_tipo_status (tipo, status)
);
