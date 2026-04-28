import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConfiguracaoIAService } from '../../services/configuracao-ia';
import { ToastService } from '../../services/toast';
import { ProvedorIA } from '../../models/configuracao-ia.model';

const DEFAULTS: Record<ProvedorIA, { modelo: string; baseUrl: string }> = {
  OPENAI: { modelo: 'gpt-4o', baseUrl: 'https://api.openai.com/v1' },
  OPENROUTER: { modelo: 'anthropic/claude-3.5-sonnet', baseUrl: 'https://openrouter.ai/api/v1' },
};

@Component({
  selector: 'app-configuracoes-ia',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './configuracoes-ia.html',
  styleUrls: ['./configuracoes-ia.scss'],
})
export class ConfiguracoesIAComponent implements OnInit {
  private fb = inject(FormBuilder);
  private service = inject(ConfiguracaoIAService);
  private toastService = inject(ToastService);

  isSaving = signal(false);
  isTestando = signal(false);
  isCarregando = signal(true);
  mostrarApiKey = signal(false);
  resultadoTeste = signal<{ sucesso: boolean; mensagem: string } | null>(null);

  form!: FormGroup;

  ngOnInit(): void {
    this.form = this.fb.group({
      provedor: ['OPENAI', Validators.required],
      apiKey: ['', Validators.required],
      modelo: ['gpt-4o', Validators.required],
      baseUrl: ['https://api.openai.com/v1'],
      temperaturaModelo: [0.7],
      promptSistema: [''],
      precoInputPorMilhao: [null],
      precoOutputPorMilhao: [null],
    });

    this.service.buscar().subscribe({
      next: (config) => {
        this.form.patchValue({
          provedor: config.provedor,
          apiKey: config.apiKey,
          modelo: config.modelo,
          baseUrl: config.baseUrl,
          temperaturaModelo: config.temperaturaModelo,
          promptSistema: config.promptSistema ?? '',
          precoInputPorMilhao: config.precoInputPorMilhao ?? null,
          precoOutputPorMilhao: config.precoOutputPorMilhao ?? null,
        });
        this.isCarregando.set(false);
      },
      error: () => {
        this.isCarregando.set(false);
      },
    });
  }

  get temperaturaAtual(): number {
    return this.form.get('temperaturaModelo')?.value ?? 0.7;
  }

  onProvedorChange(): void {
    const provedor = this.form.get('provedor')?.value as ProvedorIA;
    const defaults = DEFAULTS[provedor];
    if (defaults) {
      this.form.patchValue({ modelo: defaults.modelo, baseUrl: defaults.baseUrl });
    }
    this.resultadoTeste.set(null);
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
        this.form.patchValue({ apiKey: config.apiKey });
        this.toastService.success('Configurações salvas com sucesso!');
        this.isSaving.set(false);
      },
      error: (err) => {
        this.toastService.error(err?.error?.message || 'Erro ao salvar configurações.');
        this.isSaving.set(false);
      },
    });
  }

  testarConexao(): void {
    this.isTestando.set(true);
    this.resultadoTeste.set(null);

    this.service.testarConexao().subscribe({
      next: (res) => {
        this.resultadoTeste.set({
          sucesso: res.sucesso,
          mensagem: res.sucesso
            ? `Conexão OK — modelo: ${res.modelo}`
            : (res.erro ?? 'Falha na conexão'),
        });
        this.isTestando.set(false);
      },
      error: () => {
        this.resultadoTeste.set({ sucesso: false, mensagem: 'Erro ao testar conexão.' });
        this.isTestando.set(false);
      },
    });
  }

  toggleApiKey(): void {
    this.mostrarApiKey.set(!this.mostrarApiKey());
  }
}
