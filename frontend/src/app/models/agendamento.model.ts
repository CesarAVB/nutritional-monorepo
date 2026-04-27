export type TipoAgendamento = 'PRIMEIRA_CONSULTA' | 'RETORNO' | 'AVALIACAO';
export type StatusAgendamento = 'AGUARDANDO_CONFIRMACAO' | 'CONFIRMADO' | 'REALIZADO' | 'CANCELADO' | 'FALTA';

export interface AgendamentoResponseDto {
  id: number;
  pacienteId: number;
  nomePaciente: string;
  telefoneWhatsapp: string;
  dataHoraInicio: string;
  dataHoraFim: string;
  duracaoMinutos: number;
  tipo: TipoAgendamento;
  status: StatusAgendamento;
  observacoes?: string;
  criadoEm: string;
  notificacaoEnviada: boolean;
}

export interface AgendamentoDiaDto {
  data: string;
  agendamentos: AgendamentoResponseDto[];
}

export interface AgendamentoSemanaResponseDto {
  dataInicio: string;
  dataFim: string;
  dias: AgendamentoDiaDto[];
}

export interface ContadorHojeDto {
  quantidade: number;
}

export interface AgendamentoRequestDto {
  pacienteId: number;
  dataHoraInicio: string;
  duracaoMinutos: number;
  tipo: TipoAgendamento;
  observacoes?: string;
}
