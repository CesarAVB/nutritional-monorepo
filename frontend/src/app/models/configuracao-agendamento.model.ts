export interface ConfiguracaoAgendamentoDto {
  duracaoPadraoMinutos: number;
  intervaloEntreConsultasMinutos: number;
  diasAtendimento: string;
  horarioInicio: string;
  horarioFim: string;
  fusoHorario: string;
  janelaNotifInicio: string;
  janelaNotifFim: string;
  emailNotificacao: string | null;
  notifAoCriar: boolean;
  notifAoCancelar: boolean;
  alertaNaoConfirmacao: boolean;
  lembreteImediatoAtivo: boolean;
  lembrete72hAtivo: boolean;
  lembrete24hAtivo: boolean;
  lembrete2hAtivo: boolean;
  evolutionUrl: string | null;
  evolutionInstancia: string | null;
  evolutionApiKey: string | null;
  templateConfirmacao: string | null;
  templateLembrete72h: string | null;
  templateLembrete24h: string | null;
  templateLembrete2h: string | null;
  templateConsultaConfirmada: string | null;
  templateConsultaCancelada: string | null;
}

export interface TesteWhatsappResponseDto {
  conectado: boolean;
  mensagem: string;
}
