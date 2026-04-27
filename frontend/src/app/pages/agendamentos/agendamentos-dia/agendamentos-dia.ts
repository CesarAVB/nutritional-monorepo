import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AgendamentoService } from '../../../services/agendamento';
import { AgendamentoResponseDto, TipoAgendamento, StatusAgendamento } from '../../../models/agendamento.model';
import { ToastService } from '../../../services/toast';

@Component({
  selector: 'app-agendamentos-dia',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './agendamentos-dia.html',
  styleUrl: './agendamentos-dia.css'
})
export class AgendamentosDiaComponent implements OnInit {
  agendamentos: AgendamentoResponseDto[] = [];
  isLoading = false;
  dataSelecionada = '';    // YYYY-MM-DD

  // Labels amigáveis para tipo e status
  readonly tipoLabel: Record<TipoAgendamento, string> = {
    PRIMEIRA_CONSULTA: 'Primeira Consulta',
    RETORNO: 'Retorno',
    AVALIACAO: 'Avaliação'
  };

  readonly statusLabel: Record<StatusAgendamento, string> = {
    AGUARDANDO_CONFIRMACAO: 'Aguardando',
    CONFIRMADO: 'Confirmado',
    REALIZADO: 'Realizado',
    CANCELADO: 'Cancelado',
    FALTA: 'Falta'
  };

  readonly statusClass: Record<StatusAgendamento, string> = {
    AGUARDANDO_CONFIRMACAO: 'status-warning',
    CONFIRMADO: 'status-success',
    REALIZADO: 'status-info',
    CANCELADO: 'status-danger',
    FALTA: 'status-muted'
  };

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private agendamentoService: AgendamentoService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    // Lê ?data=YYYY-MM-DD dos queryParams; se ausente usa hoje
    this.route.queryParamMap.subscribe(params => {
      const data = params.get('data') || new Date().toISOString().split('T')[0];
      this.dataSelecionada = data;
      this.carregar();
    });
  }

  carregar(): void {
    this.isLoading = true;
    this.agendamentoService.listarPorDia(this.dataSelecionada).subscribe({
      next: (lista) => { this.agendamentos = lista; this.isLoading = false; },
      error: () => { this.toastService.error('Erro ao carregar agendamentos'); this.isLoading = false; }
    });
  }

  formatarDataExibicao(): string {
    if (!this.dataSelecionada) return '';
    const [ano, mes, dia] = this.dataSelecionada.split('-');
    return `${dia}/${mes}/${ano}`;
  }

  formatarHorario(iso: string): string {
    return iso.substring(11, 16);
  }

  novoAgendamento(): void {
    this.router.navigate(['/agendamentos/novo'], { queryParams: { data: this.dataSelecionada } });
  }

  editar(id: number): void {
    this.router.navigate(['/agendamentos', id, 'editar']);
  }

  marcarRealizado(ag: AgendamentoResponseDto): void {
    this.agendamentoService.atualizarStatus(ag.id, 'REALIZADO').subscribe({
      next: () => { this.toastService.success('Marcado como realizado'); this.carregar(); },
      error: () => this.toastService.error('Erro ao atualizar status')
    });
  }

  marcarFalta(ag: AgendamentoResponseDto): void {
    this.agendamentoService.atualizarStatus(ag.id, 'FALTA').subscribe({
      next: () => { this.toastService.success('Falta registrada'); this.carregar(); },
      error: () => this.toastService.error('Erro ao atualizar status')
    });
  }

  cancelarAgendamento(ag: AgendamentoResponseDto): void {
    if (!confirm(`Cancelar consulta de ${ag.nomePaciente}?`)) return;
    this.agendamentoService.cancelar(ag.id).subscribe({
      next: () => { this.toastService.success('Agendamento cancelado'); this.carregar(); },
      error: () => this.toastService.error('Erro ao cancelar agendamento')
    });
  }

  voltarDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  irParaDiaAnterior(): void {
    const d = new Date(this.dataSelecionada + 'T12:00:00');
    d.setDate(d.getDate() - 1);
    const nova = d.toISOString().split('T')[0];
    this.router.navigate(['/agendamentos'], { queryParams: { data: nova } });
  }

  irParaProximoDia(): void {
    const d = new Date(this.dataSelecionada + 'T12:00:00');
    d.setDate(d.getDate() + 1);
    const nova = d.toISOString().split('T')[0];
    this.router.navigate(['/agendamentos'], { queryParams: { data: nova } });
  }
}
