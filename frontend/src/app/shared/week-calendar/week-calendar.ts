import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AgendamentoSemanaResponseDto, StatusAgendamento } from '../../models/agendamento.model';

@Component({
  selector: 'app-week-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './week-calendar.html',
  styleUrl: './week-calendar.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WeekCalendarComponent {
  @Input() semana: AgendamentoSemanaResponseDto | null = null;
  @Input() carregando = false;
  @Output() semanaAnterior = new EventEmitter<void>();
  @Output() proximaSemana = new EventEmitter<void>();
  @Output() hoje = new EventEmitter<void>();
  @Output() diaSelecionado = new EventEmitter<string>();

  protected readonly DIAS_SEMANA = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'];
  protected readonly hoje_str = new Date().toISOString().split('T')[0];

  protected formatarPeriodo(): string {
    if (!this.semana) return '';
    const [, mesI, diaI] = this.semana.dataInicio.split('-');
    const [, mesF, diaF] = this.semana.dataFim.split('-');
    return mesI === mesF
      ? `${diaI} – ${diaF}/${mesF}`
      : `${diaI}/${mesI} – ${diaF}/${mesF}`;
  }

  protected numeroDia(dataStr: string): string {
    return dataStr.split('-')[2].replace(/^0/, '');
  }

  protected primeiroNome(nomeCompleto: string): string {
    return nomeCompleto.split(' ')[0];
  }

  protected ehHoje(dataStr: string): boolean {
    return dataStr === this.hoje_str;
  }

  protected formatarHorario(isoStr: string): string {
    return isoStr.substring(11, 16);
  }

  protected corStatus(status: StatusAgendamento): string {
    const cores: Record<StatusAgendamento, string> = {
      AGUARDANDO_CONFIRMACAO: 'warning',
      CONFIRMADO: 'success',
      REALIZADO: 'info',
      CANCELADO: 'danger',
      FALTA: 'muted'
    };
    return cores[status] ?? 'muted';
  }
}
