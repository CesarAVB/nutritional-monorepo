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
  protected readonly HORAS = Array.from({ length: 15 }, (_, i) => i + 7); // 7h às 21h

  protected formatarPeriodo(): string {
    if (!this.semana) return '';
    const [, mesI, diaI] = this.semana.dataInicio.split('-');
    const [, mesF, diaF] = this.semana.dataFim.split('-');
    
    const meses: Record<string, string> = {
      '01': 'Jan', '02': 'Fev', '03': 'Mar', '04': 'Abr', '05': 'Mai', '06': 'Jun',
      '07': 'Jul', '08': 'Ago', '09': 'Set', '10': 'Out', '11': 'Nov', '12': 'Dez'
    };

    return mesI === mesF
      ? `${diaI} – ${diaF} de ${meses[mesF]}`
      : `${diaI} de ${meses[mesI]} – ${diaF} de ${meses[mesF]}`;
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

  protected getEventStyle(ag: any) {
    const dataHora = new Date(ag.dataHoraInicio);
    const horas = dataHora.getHours();
    const minutos = dataHora.getMinutes();
    
    // Cada hora tem 44px de altura para ser mais compacto
    const hourHeight = 44;
    const top = ((horas - 7) * hourHeight) + (minutos * hourHeight / 60);
    const height = (ag.duracaoMinutos || 50) * hourHeight / 60;
    
    return {
      'top': `${top}px`,
      'height': `${height}px`
    };
  }

  protected getCurrentTimePosition(): string | null {
    const agora = new Date();
    const horas = agora.getHours();
    const minutos = agora.getMinutes();
    
    if (horas < 7 || horas >= 22) return null;
    
    const hourHeight = 44;
    const top = ((horas - 7) * hourHeight) + (minutos * hourHeight / 60);
    return `${top}px`;
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
