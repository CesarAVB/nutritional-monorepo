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

  protected formatarData(dataStr: string): string {
    const [, mes, dia] = dataStr.split('-');
    return `${dia}/${mes}`;
  }

  protected ehHoje(dataStr: string): boolean {
    return dataStr === new Date().toISOString().split('T')[0];
  }

  protected formatarHorario(isoStr: string): string {
    return isoStr.substring(11, 16);
  }

  protected corStatus(status: StatusAgendamento): string {
    const cores: Record<StatusAgendamento, string> = {
      AGUARDANDO_CONFIRMACAO: 'chip-warning',
      CONFIRMADO: 'chip-success',
      REALIZADO: 'chip-info',
      CANCELADO: 'chip-danger',
      FALTA: 'chip-muted'
    };
    return cores[status] ?? 'chip-muted';
  }
}
