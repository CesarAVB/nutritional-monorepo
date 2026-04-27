import { Component, OnInit, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DashboardService, DashboardStatsDTO, PacienteDTO } from '../../services/dashboard';
import { ToastService } from '../../services/toast';
import { AgendamentoService } from '../../services/agendamento';
import { AgendamentoSemanaResponseDto } from '../../models/agendamento.model';
import { WeekCalendarComponent } from '../../shared/week-calendar/week-calendar';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faUsers, faCalendarDay, faChartBar, faClock, faUserPlus, faCalendarPlus, faList, faCalendarAlt, faChevronRight, faPhone, faPlus, faSyncAlt, faHome } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, FontAwesomeModule, WeekCalendarComponent],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent implements OnInit {

  protected readonly faUsers = faUsers;
  protected readonly faCalendarDay = faCalendarDay;
  protected readonly faChartBar = faChartBar;
  protected readonly faClock = faClock;
  protected readonly faUserPlus = faUserPlus;
  protected readonly faCalendarPlus = faCalendarPlus;
  protected readonly faList = faList;
  protected readonly faCalendarAlt = faCalendarAlt;
  protected readonly faChevronRight = faChevronRight;
  protected readonly faPhone = faPhone;
  protected readonly faPlus = faPlus;
  protected readonly faSyncAlt = faSyncAlt;
  protected readonly faHome = faHome;
  
  private readonly dashboardService = inject(DashboardService);
  private readonly agendamentoService = inject(AgendamentoService);
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);

  protected readonly stats = signal<DashboardStatsDTO | null>(null);
  protected readonly semanaAgendamentos = signal<AgendamentoSemanaResponseDto | null>(null);
  protected readonly pacientesRecentes = signal<PacienteDTO[]>([]);

  protected readonly loadingStats = signal(true);
  protected readonly loadingCalendario = signal(false);
  protected readonly loadingPacientes = signal(false);
  protected readonly dataReferenciaCalendario = signal<string>(new Date().toISOString().split('T')[0]);

  protected readonly isLoading = computed(() =>
    this.loadingStats() || this.loadingCalendario() || this.loadingPacientes()
  );

  protected readonly greeting = computed(() => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Bom dia';
    if (hour < 18) return 'Boa tarde';
    return 'Boa noite';
  });

  // ===========================================
  // # ngOnInit - Inicializa o componente
  // ===========================================
  ngOnInit(): void {
    this.carregarDados();
  }

  // ===========================================
  // # carregarDados - Carrega dados do dashboard
  // ===========================================
  carregarDados(): void {
    this.loadingStats.set(true);
    this.dashboardService.obterEstatisticas().subscribe({
      next: (stats) => {
        this.stats.set(stats);
        this.loadingStats.set(false);
      },
      error: (err) => {
        this.loadingStats.set(false);
        if (err.status === 400) {
          this.toastService.info(err.error.message || 'Sem dados para exibir');
        } else {
          this.toastService.error('Erro ao carregar estatísticas');
        }
        this.stats.set(null);
      }
    });

    this.carregarCalendario();

    this.loadingPacientes.set(true);
    this.dashboardService.pacientesRecentes(5).subscribe({
      next: (pacientes) => {
        this.pacientesRecentes.set(pacientes);
        this.loadingPacientes.set(false);
      },
      error: (err) => {
        this.loadingPacientes.set(false);
        if (err.status === 400) {
          this.toastService.info(err.error.message || 'Nenhum paciente encontrado');
        } else {
          this.toastService.error('Erro ao carregar pacientes');
        }
        this.pacientesRecentes.set([]);
      }
    });
  }

  // ===========================================
  // # carregarCalendario - Carrega dados do calendário semanal
  // ===========================================
  carregarCalendario(): void {
    this.loadingCalendario.set(true);
    this.agendamentoService.listarSemana(this.dataReferenciaCalendario()).subscribe({
      next: (semana) => {
        this.semanaAgendamentos.set(semana);
        this.loadingCalendario.set(false);
      },
      error: (err) => {
        this.loadingCalendario.set(false);
        if (err.status === 400) {
          this.toastService.info(err.error.message || 'Nenhum agendamento encontrado');
        } else {
          this.toastService.error('Erro ao carregar calendário');
        }
        this.semanaAgendamentos.set(null);
      }
    });
  }

  // ===========================================
  // # navegarSemanaAnterior - Navega para semana anterior
  // ===========================================
  protected navegarSemanaAnterior(): void {
    const dataAtual = new Date(this.dataReferenciaCalendario());
    dataAtual.setDate(dataAtual.getDate() - 7);
    this.dataReferenciaCalendario.set(dataAtual.toISOString().split('T')[0]);
    this.carregarCalendario();
  }

  // ===========================================
  // # navegarProximaSemana - Navega para próxima semana
  // ===========================================
  protected navegarProximaSemana(): void {
    const dataAtual = new Date(this.dataReferenciaCalendario());
    dataAtual.setDate(dataAtual.getDate() + 7);
    this.dataReferenciaCalendario.set(dataAtual.toISOString().split('T')[0]);
    this.carregarCalendario();
  }

  // ===========================================
  // # voltarHoje - Volta para a semana atual
  // ===========================================
  protected voltarHoje(): void {
    this.dataReferenciaCalendario.set(new Date().toISOString().split('T')[0]);
    this.carregarCalendario();
  }

  // ===========================================
  // # irParaDia - Navega para página de agendamentos do dia
  // ===========================================
  protected irParaDia(data: string): void {
    this.router.navigate(['/agendamentos'], { queryParams: { data } });
  }

  // ===========================================
  // # recarregar - Recarrega os dados
  // ===========================================
  protected recarregar(): void {
    this.carregarDados();
  }

  // ===========================================
  // # verTodosPacientes - Navega para lista de pacientes
  // ===========================================
  protected verTodosPacientes(): void {
    this.router.navigate(['/pacientes']);
  }

  // ===========================================
  // # verDetalhesPaciente - Navega para detalhes do paciente
  // ===========================================
  protected verDetalhesPaciente(id: number): void {
    this.router.navigate(['/pacientes', id.toString()]);
  }

  // ===========================================
  // # novoPaciente - Navega para criação de novo paciente
  // ===========================================
  protected novoPaciente(): void {
    this.router.navigate(['/pacientes/novo']);
  }

  // ===========================================
  // # novaConsulta - Navega para criação de nova consulta
  // ===========================================
  protected novaConsulta(): void {
    this.router.navigate(['/consultas/novo']);
  }

  // ===========================================
  // # verConsultas - Navega para lista de consultas
  // ===========================================
  protected verConsultas(): void {
    this.router.navigate(['/consultas']);
  }

  // ===========================================
  // # getIniciais - Obtém iniciais do nome
  // ===========================================
  protected getIniciais(nomeCompleto: string): string {
    return nomeCompleto
      .split(' ')
      .map(nome => nome.charAt(0).toUpperCase())
      .slice(0, 2)
      .join('');
  }

  // ===========================================
  // # formatarData - Formata data para exibição
  // ===========================================
  protected formatarData(data: string | null | undefined): string {
    if (!data) return 'Sem registro';
    
    const dataObj = new Date(data);
    return dataObj.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  // ===========================================
  // # ligarPaciente - Abre WhatsApp do paciente
  // ===========================================
  protected ligarPaciente(telefone: string | null | undefined, event: Event): void {
    event.stopPropagation();
    if (!telefone) {
      this.toastService.warning('Paciente sem telefone cadastrado');
      return;
    }
    
    const telefoneFormatado = telefone.replace(/\D/g, '');
    window.open(`https://wa.me/55${telefoneFormatado}`, '_blank');
  }
}