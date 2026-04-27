import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConfiguracaoAgendamentoService } from '../../../services/configuracao-agendamento';
import { ToastService } from '../../../services/toast';

interface DiaSemana {
  sigla: string;
  label: string;
}

@Component({
  selector: 'app-configuracoes-agendamento',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './configuracoes-agendamento.html',
  styleUrl: './configuracoes-agendamento.scss',
})
export class ConfiguracoesAgendamentoComponent implements OnInit {
  private fb = inject(FormBuilder);
  private service = inject(ConfiguracaoAgendamentoService);
  private toastService = inject(ToastService);

  isSaving = signal(false);
  isCarregando = signal(true);
  isTestando = signal(false);
  mostrarApiKey = signal(false);
  abaAtiva = signal<string>('agenda');
  resultadoTeste = signal<{ conectado: boolean; mensagem: string } | null>(null);

  paciente_nome = '';
  nutricionista_nome = '';
  data_consulta = '';
  hora_consulta = '';
  tipo_consulta = '';

  form!: FormGroup;

  diasSemana: DiaSemana[] = [
    { sigla: 'SEG', label: 'Segunda' },
    { sigla: 'TER', label: 'Terça' },
    { sigla: 'QUA', label: 'Quarta' },
    { sigla: 'QUI', label: 'Quinta' },
    { sigla: 'SEX', label: 'Sexta' },
    { sigla: 'SAB', label: 'Sábado' },
    { sigla: 'DOM', label: 'Domingo' },
  ];

  ngOnInit(): void {
    this.form = this.fb.group({
      duracaoPadraoMinutos: [60, [Validators.required, Validators.min(15)]],
      intervaloEntreConsultasMinutos: [0, [Validators.required, Validators.min(0)]],
      diasAtendimento: ['SEG,TER,QUA,QUI,SEX', Validators.required],
      horarioInicio: ['08:00:00', Validators.required],
      horarioFim: ['18:00:00', Validators.required],
      fusoHorario: ['America/Sao_Paulo', Validators.required],
      janelaNotifInicio: ['08:00:00', Validators.required],
      janelaNotifFim: ['20:00:00', Validators.required],
      emailNotificacao: [''],
      notifAoCriar: [true],
      notifAoCancelar: [true],
      alertaNaoConfirmacao: [true],
      lembreteImediatoAtivo: [true],
      lembrete72hAtivo: [true],
      lembrete24hAtivo: [true],
      lembrete2hAtivo: [true],
      evolutionUrl: [''],
      evolutionInstancia: [''],
      evolutionApiKey: [''],
      templateConfirmacao: [''],
      templateLembrete72h: [''],
      templateLembrete24h: [''],
      templateLembrete2h: [''],
      templateConsultaConfirmada: [''],
      templateConsultaCancelada: [''],
    });

    this.service.buscar().subscribe({
      next: (config) => {
        this.form.patchValue(config);
        this.isCarregando.set(false);
      },
      error: () => {
        this.isCarregando.set(false);
      },
    });
  }

  mudarAba(aba: string): void {
    this.abaAtiva.set(aba);
    this.resultadoTeste.set(null);
  }

  isDiaAtivo(sigla: string): boolean {
    const diasAtendimento = this.form.get('diasAtendimento')?.value || '';
    return diasAtendimento.split(',').includes(sigla);
  }

  toggleDia(sigla: string): void {
    const diasAtendimento = this.form.get('diasAtendimento')?.value || '';
    const dias = diasAtendimento.split(',').filter((d: string) => d);
    const index = dias.indexOf(sigla);

    if (index > -1) {
      dias.splice(index, 1);
    } else {
      dias.push(sigla);
    }

    this.form.patchValue({ diasAtendimento: dias.join(',') });
  }

  toggleApiKey(): void {
    this.mostrarApiKey.set(!this.mostrarApiKey());
  }

  testarWhatsapp(): void {
    this.isTestando.set(true);
    this.resultadoTeste.set(null);

    this.service.testarWhatsapp().subscribe({
      next: (res) => {
        this.resultadoTeste.set({
          conectado: res.conectado,
          mensagem: res.mensagem,
        });
        this.isTestando.set(false);
      },
      error: () => {
        this.resultadoTeste.set({
          conectado: false,
          mensagem: 'Erro ao testar conexão com WhatsApp.',
        });
        this.isTestando.set(false);
      },
    });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastService.error('Preencha todos os campos obrigatórios.');
      return;
    }

    this.isSaving.set(true);
    this.service.salvar(this.form.getRawValue()).subscribe({
      next: (config) => {
        this.form.patchValue({ evolutionApiKey: config.evolutionApiKey });
        this.toastService.success('Configurações salvas com sucesso!');
        this.isSaving.set(false);
      },
      error: (err) => {
        this.toastService.error(err?.error?.message || 'Erro ao salvar configurações.');
        this.isSaving.set(false);
      },
    });
  }
}
