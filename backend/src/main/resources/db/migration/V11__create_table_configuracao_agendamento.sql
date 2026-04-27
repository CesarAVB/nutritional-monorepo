-- ====================================
-- V11: Criar tabela de configuração de agendamento
-- ====================================

CREATE TABLE tbl_configuracao_agendamento (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  duracao_padrao_minutos INT NOT NULL DEFAULT 50,
  intervalo_entre_consultas_minutos INT NOT NULL DEFAULT 10,
  dias_atendimento VARCHAR(50) NOT NULL DEFAULT 'SEG,TER,QUA,QUI,SEX',
  horario_inicio TIME NOT NULL DEFAULT '08:00:00',
  horario_fim TIME NOT NULL DEFAULT '18:00:00',
  fuso_horario VARCHAR(50) NOT NULL DEFAULT 'America/Sao_Paulo',
  janela_notif_inicio TIME NOT NULL DEFAULT '08:00:00',
  janela_notif_fim TIME NOT NULL DEFAULT '22:00:00',
  email_notificacao VARCHAR(200),
  notif_ao_criar BOOLEAN NOT NULL DEFAULT TRUE,
  notif_ao_cancelar BOOLEAN NOT NULL DEFAULT TRUE,
  alerta_nao_confirmacao BOOLEAN NOT NULL DEFAULT TRUE,
  lembrete_imediato_ativo BOOLEAN NOT NULL DEFAULT TRUE,
  lembrete_72h_ativo BOOLEAN NOT NULL DEFAULT TRUE,
  lembrete_24h_ativo BOOLEAN NOT NULL DEFAULT TRUE,
  lembrete_2h_ativo BOOLEAN NOT NULL DEFAULT TRUE,
  evolution_url VARCHAR(500),
  evolution_instancia VARCHAR(200),
  evolution_api_key TEXT,
  template_confirmacao TEXT,
  template_lembrete_72h TEXT,
  template_lembrete_24h TEXT,
  template_lembrete_2h TEXT,
  template_consulta_confirmada TEXT,
  template_consulta_cancelada TEXT
);

INSERT INTO tbl_configuracao_agendamento (duracao_padrao_minutos, intervalo_entre_consultas_minutos) VALUES (50, 10);
