import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AgendamentoService } from '../../../services/agendamento';
import { AgendamentoRequestDto, AgendamentoResponseDto, TipoAgendamento, StatusAgendamento } from '../../../models/agendamento.model';
import { PacienteService } from '../../../services/paciente';
import { PacienteDTO } from '../../../models/paciente.model';
import { ToastService } from '../../../services/toast';

@Component({
  selector: 'app-agendamento-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './agendamento-form.html',
  styleUrl: './agendamento-form.css'
})
export class AgendamentoFormComponent implements OnInit {
  form: FormGroup;
  isEditMode = false;
  agendamentoId?: number;
  isLoading = false;
  isSaving = false;
  slots: string[] = [];
  loadingSlots = false;
  pacientesBusca: PacienteDTO[] = [];
  pacienteSelecionado: PacienteDTO | null = null;
  showDropdown = false;
  dataPreenchida = ''; // YYYY-MM-DD pré-vinda da queryParam
  private buscaDebounce: ReturnType<typeof setTimeout> | null = null;

  readonly tipos: TipoAgendamento[] = ['PRIMEIRA_CONSULTA', 'RETORNO', 'AVALIACAO'];
  readonly tipoLabel: Record<TipoAgendamento, string> = {
    PRIMEIRA_CONSULTA: 'Primeira Consulta',
    RETORNO: 'Retorno',
    AVALIACAO: 'Avaliação'
  };
  readonly statusOptions: StatusAgendamento[] = ['AGUARDANDO_CONFIRMACAO','CONFIRMADO','REALIZADO','CANCELADO','FALTA'];
  readonly statusLabel: Record<StatusAgendamento, string> = {
    AGUARDANDO_CONFIRMACAO: 'Aguardando Confirmação',
    CONFIRMADO: 'Confirmado',
    REALIZADO: 'Realizado',
    CANCELADO: 'Cancelado',
    FALTA: 'Falta'
  };

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private agendamentoService: AgendamentoService,
    private pacienteService: PacienteService,
    private toastService: ToastService
  ) {
    this.form = this.fb.group({
      buscaPaciente: [''],
      pacienteId: [null, Validators.required],
      data: ['', Validators.required],
      horario: ['', Validators.required],
      duracaoMinutos: [50, [Validators.required, Validators.min(10)]],
      tipo: ['RETORNO', Validators.required],
      observacoes: [''],
      status: ['AGUARDANDO_CONFIRMACAO']
    });
  }

  ngOnInit(): void {
    // Verificar se é edição
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      this.agendamentoId = Number(idParam);
      this.carregarParaEdicao(this.agendamentoId);
    } else {
      // Pré-preencher data se vier via queryParam
      const data = this.route.snapshot.queryParamMap.get('data');
      if (data) {
        this.dataPreenchida = data;
        this.form.patchValue({ data }, { emitEvent: false });
        this.carregarSlots(data);
      }
    }

    // Recarregar slots ao mudar a data
    this.form.get('data')!.valueChanges.subscribe(data => {
      if (data && data.length === 10) {
        this.carregarSlots(data);
        this.form.patchValue({ horario: '' }, { emitEvent: false });
      }
    });
  }

  carregarParaEdicao(id: number): void {
    this.isLoading = true;
    this.agendamentoService.buscarPorId(id).subscribe({
      next: (ag) => {
        this.pacienteSelecionado = { id: ag.pacienteId, nomeCompleto: ag.nomePaciente, telefoneWhatsapp: ag.telefoneWhatsapp } as any;
        const data = ag.dataHoraInicio.substring(0, 10);
        const horario = ag.dataHoraInicio.substring(11, 16);
        this.form.patchValue({
          buscaPaciente: ag.nomePaciente,
          pacienteId: ag.pacienteId,
          data,
          horario,
          duracaoMinutos: ag.duracaoMinutos,
          tipo: ag.tipo,
          observacoes: ag.observacoes || '',
          status: ag.status
        }, { emitEvent: false });
        this.carregarSlots(data);
        this.isLoading = false;
      },
      error: () => {
        this.toastService.error('Agendamento não encontrado');
        this.router.navigate(['/agendamentos']);
      }
    });
  }

  buscarPacientes(event: Event): void {
    const termo = (event.target as HTMLInputElement).value;
    if (termo.length < 2) { this.pacientesBusca = []; this.showDropdown = false; return; }
    if (this.buscaDebounce) clearTimeout(this.buscaDebounce);
    this.buscaDebounce = setTimeout(() => {
      this.pacienteService.buscarPorNome(termo).subscribe({
        next: (lista) => { this.pacientesBusca = lista.slice(0, 8); this.showDropdown = true; },
        error: () => { this.pacientesBusca = []; }
      });
    }, 300);
  }

  selecionarPaciente(p: PacienteDTO): void {
    this.pacienteSelecionado = p;
    this.form.patchValue({ pacienteId: p.id, buscaPaciente: p.nomeCompleto }, { emitEvent: false });
    this.showDropdown = false;
    this.pacientesBusca = [];
  }

  carregarSlots(data: string): void {
    this.loadingSlots = true;
    this.slots = [];
    this.agendamentoService.calcularSlots(data).subscribe({
      next: (s) => { this.slots = s; this.loadingSlots = false; },
      error: () => { this.loadingSlots = false; }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.toastService.warning('Preencha todos os campos obrigatórios');
      Object.values(this.form.controls).forEach(c => c.markAsTouched());
      return;
    }
    this.isSaving = true;
    const v = this.form.value;
    const horario = v.horario.includes(':') ? v.horario.substring(0, 5) : v.horario;
    const dataHoraInicio = `${v.data}T${horario}:00`;

    const dto: AgendamentoRequestDto = {
      pacienteId: v.pacienteId,
      dataHoraInicio,
      duracaoMinutos: v.duracaoMinutos,
      tipo: v.tipo,
      observacoes: v.observacoes || undefined
    };

    const op = this.isEditMode
      ? this.agendamentoService.atualizar(this.agendamentoId!, dto)
      : this.agendamentoService.criar(dto);

    op.subscribe({
      next: (ag) => {
        this.toastService.success(this.isEditMode ? 'Agendamento atualizado!' : 'Agendamento criado!');
        if (!this.isEditMode && ag.notificacaoEnviada === false) {
          setTimeout(() => {
            this.toastService.warning('Notificação WhatsApp não enviada. Configure a integração em Configurações de Agendamento.');
          }, 500);
        }
        const data = ag.dataHoraInicio.substring(0, 10);
        this.router.navigate(['/agendamentos'], { queryParams: { data } });
      },
      error: (err) => {
        const msg = err?.error?.message || 'Erro ao salvar agendamento';
        this.toastService.error(msg);
        this.isSaving = false;
      }
    });
  }

  cancelar(): void {
    const data = this.form.value.data || this.dataPreenchida;
    this.router.navigate(['/agendamentos'], { queryParams: data ? { data } : {} });
  }

  getCampoErro(campo: string): string | null {
    const ctrl = this.form.get(campo);
    if (ctrl?.touched && ctrl?.errors) {
      if (ctrl.errors['required']) return 'Campo obrigatório';
      if (ctrl.errors['min']) return 'Mínimo de 10 minutos';
    }
    return null;
  }
}
