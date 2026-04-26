// src/app/pages/pacientes/paciente-details/paciente-details.ts
import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { PacienteService } from '../../../services/paciente';
import { ConsultaService } from '../../../services/consulta';
import { DietaService } from '../../../services/dieta';
import { PacienteDTO, ConsultaResumoDTO } from '../../../models/paciente.model';
import { DietaResumoResponse } from '../../../models/dieta.model';
import { ToastService } from '../../../services/toast';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-paciente-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './paciente-details.html',
  styleUrls: ['./paciente-details.scss']
})
export class PacienteDetailsComponent implements OnInit {
  private toastService = inject(ToastService);
  private http = inject(HttpClient);
  private dietaService = inject(DietaService);
  paciente = signal<PacienteDTO | null>(null);
  consultas = signal<ConsultaResumoDTO[]>([]);
  dietas = signal<DietaResumoResponse[]>([]);
  isLoading = signal(false);
  error = signal('');
  mostrarModalExclusao = signal(false);
  mostrarModalExclusaoDieta = signal(false);
  dietaParaExcluir = signal<number | null>(null);
  gerandoComparativo = signal(false);
  gerandoPdfDieta = signal<number | null>(null);
  expandirProntuario = signal(false);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private pacienteService: PacienteService,
    private consultaService: ConsultaService
  ) {}

  // ===========================================
  // # ngOnInit - Inicializa o componente carregando dados do paciente e consultas
  // ===========================================
  ngOnInit(): void {
    const id = Number(this.route.snapshot.params['id']);
    if (id) {
      this.carregarPaciente(id);
      this.carregarConsultas(id);
      this.carregarDietas(id);
    }
  }

  // ===========================================
  // # carregarPaciente - Carrega os dados do paciente pelo ID
  // ===========================================
  carregarPaciente(id: number): void {
    this.isLoading.set(true);
    
    this.pacienteService.buscarPorId(id).subscribe({
      next: (paciente) => {
        this.paciente.set(paciente);
        this.isLoading.set(false);
      },
      error: (erro) => {
        console.error('Erro ao carregar paciente:', erro);
        this.toastService.error('Erro ao carregar dados do paciente');
        this.isLoading.set(false);
        this.router.navigate(['/pacientes']);
      }
    });
  }

  // ===========================================
  // # carregarConsultas - Carrega as consultas associadas ao paciente
  // ===========================================
  carregarConsultas(pacienteId: number): void {
    this.consultaService.listarPorPaciente(pacienteId).subscribe({
      next: (consultas) => {
        console.log('Consultas carregadas:', consultas);
        this.consultas.set(consultas);
      },
      error: (erro) => {
        console.error('Erro ao carregar consultas:', erro);
        this.toastService.error('Erro ao carregar histórico de consultas');
      }
    });
  }

  // ===========================================
  // # carregarDietas - Carrega as dietas do paciente
  // ===========================================
  carregarDietas(pacienteId: number): void {
    this.dietaService.listarPorPaciente(pacienteId).subscribe({
      next: (dietas) => this.dietas.set(dietas),
      error: () => {},
    });
  }

  // ===========================================
  // # novaDieta - Navega para criação de nova dieta
  // ===========================================
  novaDieta(): void {
    const id = this.paciente()?.id;
    if (id) this.router.navigate(['/pacientes', id, 'dietas', 'nova']);
  }

  // ===========================================
  // # editarDieta - Navega para edição de dieta
  // ===========================================
  editarDieta(dietaId: number): void {
    const id = this.paciente()?.id;
    if (id) this.router.navigate(['/pacientes', id, 'dietas', dietaId, 'editar']);
  }

  // ===========================================
  // # confirmarExclusaoDieta - Abre modal de exclusão de dieta
  // ===========================================
  confirmarExclusaoDieta(dietaId: number): void {
    this.dietaParaExcluir.set(dietaId);
    this.mostrarModalExclusaoDieta.set(true);
  }

  cancelarExclusaoDieta(): void {
    this.mostrarModalExclusaoDieta.set(false);
    this.dietaParaExcluir.set(null);
  }

  excluirDieta(): void {
    const dietaId = this.dietaParaExcluir();
    if (!dietaId) return;
    this.dietaService.deletar(dietaId).subscribe({
      next: () => {
        this.toastService.success('Dieta excluída com sucesso!');
        this.mostrarModalExclusaoDieta.set(false);
        this.dietaParaExcluir.set(null);
        const id = this.paciente()?.id;
        if (id) this.carregarDietas(id);
      },
      error: () => this.toastService.error('Erro ao excluir dieta.'),
    });
  }

  // ===========================================
  // # baixarPdfDieta - Baixa PDF de uma dieta
  // ===========================================
  baixarPdfDieta(dietaId: number): void {
    this.gerandoPdfDieta.set(dietaId);
    this.dietaService.baixarPdf(dietaId).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `dieta-${dietaId}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
        this.gerandoPdfDieta.set(null);
      },
      error: () => {
        this.toastService.error('Erro ao gerar PDF da dieta.');
        this.gerandoPdfDieta.set(null);
      },
    });
  }

  // ===========================================
  // # formatarDataSimples - Formata data ISO para dd/MM/yyyy
  // ===========================================
  formatarDataSimples(dataISO: string): string {
    if (!dataISO) return '-';
    const [ano, mes, dia] = dataISO.split('-');
    return `${dia}/${mes}/${ano}`;
  }

  // ===========================================
  // # gerarComparativo - Realiza POST para gerar relatório comparativo do paciente
  // ===========================================
  gerarComparativo(): void {
    const pacienteId = this.paciente()?.id;
    if (!pacienteId) return;

    this.gerandoComparativo.set(true);
    this.http
      .post(`${environment.apiUrl}/api/v1/relatorio/comparativo/${pacienteId}`, {}, { responseType: 'blob' })
      .subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `comparativo-paciente-${pacienteId}.pdf`;
          a.click();
          URL.revokeObjectURL(url);
          this.toastService.success('Relatório comparativo gerado com sucesso!');
          this.gerandoComparativo.set(false);
        },
        error: (erro) => {
          console.error('Erro ao gerar comparativo:', erro);
          this.toastService.error('Erro ao gerar relatório comparativo. Tente novamente.');
          this.gerandoComparativo.set(false);
        }
      });
  }

  // ===========================================
  // # novaConsulta - Navega para a página de criação de nova consulta
  // ===========================================
  novaConsulta(): void {
    const pacienteId = this.paciente()?.id;
    if (pacienteId) {
      this.router.navigate(['/pacientes', pacienteId, 'consulta']);
    }
  }

  // ===========================================
  // # editarPaciente - Navega para a página de edição do paciente
  // ===========================================
  editarPaciente(): void {
    console.log('Método editarPaciente chamado');
    const pacienteAtual = this.paciente();
    console.log('Paciente atual:', pacienteAtual);
    
    const pacienteId = pacienteAtual?.id;
    console.log('ID do paciente:', pacienteId);
    
    if (pacienteId) {
      const rota = ['/pacientes', pacienteId, 'editar'];
      console.log('Navegando para:', rota);
      this.router.navigate(rota);
    } else {
      console.error('ID do paciente não encontrado');
      this.toastService.error('Erro: não foi possível identificar o paciente para edição.');
    }
  }

  // ===========================================
  // # verConsulta - Navega para os detalhes da consulta selecionada
  // ===========================================
  verConsulta(consultaId: number): void {
    this.router.navigate(['/consultas', consultaId]);
  }

  // ===========================================
  // # getConsultaPrefix - Gera prefixo tipo SKU para consulta
  // ===========================================
  getConsultaPrefix(id: number): string {
    return `CNS-${id.toString().padStart(4, '0')}`;
  }

  // ===========================================
  // # abrirWhatsApp - Abre o WhatsApp com o número de telefone fornecido
  // ===========================================
  abrirWhatsApp(telefone: string): void {
    const numero = telefone.replace(/\D/g, '');
    window.open(`https://wa.me/55${numero}`, '_blank');
  }

  // ===========================================
  // # confirmarExclusao - Exibe o modal de confirmação para exclusão do paciente
  // ===========================================
  confirmarExclusao(): void {
    this.mostrarModalExclusao.set(true);
  }

  // ===========================================
  // # cancelarExclusao - Fecha o modal de confirmação de exclusão
  // ===========================================
  cancelarExclusao(): void {
    this.mostrarModalExclusao.set(false);
  }

  // ===========================================
  // # excluirPaciente - Exclui o paciente do sistema
  // ===========================================
  excluirPaciente(): void {
    const pacienteId = this.paciente()?.id;
    if (!pacienteId) return;

    this.isLoading.set(true);
    
    this.pacienteService.deletar(pacienteId).subscribe({
      next: () => {
        this.toastService.success('Paciente excluído com sucesso!');
        this.router.navigate(['/pacientes']);
      },
      error: (erro) => {
        console.error('Erro ao excluir paciente:', erro);
        this.toastService.error('Erro ao excluir paciente. Tente novamente.');
        this.isLoading.set(false);
        this.mostrarModalExclusao.set(false);
      }
    });
  }

  // ===========================================
  // # voltar - Navega de volta para a lista de pacientes
  // ===========================================
  voltar(): void {
    this.router.navigate(['/pacientes']);
  }

  // ===========================================
  // # formatarData - Formata a data ISO para o formato brasileiro
  // ===========================================
  formatarData(dataISO?: string): string {
    if (!dataISO) return '-';
    try {
      const data = new Date(dataISO);
      if (isNaN(data.getTime())) return '-';
      return data.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    } catch {
      return '-';
    }
  }

  // ===========================================
  // # calcularIdade - Calcula a idade do paciente baseada na data de nascimento
  // ===========================================
  calcularIdade(dataNascimento: string): number {
    const hoje = new Date();
    const nascimento = new Date(dataNascimento);
    let idade = hoje.getFullYear() - nascimento.getFullYear();
    const mes = hoje.getMonth() - nascimento.getMonth();
    
    if (mes < 0 || (mes === 0 && hoje.getDate() < nascimento.getDate())) {
      idade--;
    }
    
    return idade;
  }

  // ===========================================
  // # extrairDia - Extrai o dia da data ISO
  // ===========================================
  extrairDia(dataISO: string): string {
    if (!dataISO) return '-';
    try {
      const data = new Date(dataISO);
      if (isNaN(data.getTime())) return '-';
      return data.getDate().toString().padStart(2, '0');
    } catch {
      return '-';
    }
  }

  // ===========================================
  // # extrairMes - Extrai o mês da data ISO em formato abreviado
  // ===========================================
  extrairMes(dataISO: string): string {
    if (!dataISO) return '-';
    try {
      const data = new Date(dataISO);
      if (isNaN(data.getTime())) return '-';
      const meses = ['JAN', 'FEV', 'MAR', 'ABR', 'MAI', 'JUN', 
                     'JUL', 'AGO', 'SET', 'OUT', 'NOV', 'DEZ'];
      return meses[data.getMonth()];
    } catch {
      return '-';
    }
  }
}