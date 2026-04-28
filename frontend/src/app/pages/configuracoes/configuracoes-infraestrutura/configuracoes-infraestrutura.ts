import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConfiguracaoInfraestruturaService } from '../../../services/configuracao-infraestrutura';
import { ToastService } from '../../../services/toast';

@Component({
  selector: 'app-configuracoes-infraestrutura',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './configuracoes-infraestrutura.html',
  styleUrl: './configuracoes-infraestrutura.scss',
})
export class ConfiguracoesInfraestruturaComponent implements OnInit {
  private fb = inject(FormBuilder);
  private service = inject(ConfiguracaoInfraestruturaService);
  private toastService = inject(ToastService);

  isSaving = signal(false);
  isCarregando = signal(true);
  isTestandoEmail = signal(false);
  isTestandoMinio = signal(false);
  isTestandoRabbitmq = signal(false);

  mostrarEmailPassword = signal(false);
  mostrarRabbitmqPassword = signal(false);
  mostrarMinioSecretKey = signal(false);

  abaAtiva = signal<string>('email');
  resultadoTeste = signal<{ sucesso: boolean; mensagem: string } | null>(null);

  form!: FormGroup;

  ngOnInit(): void {
    this.form = this.fb.group({
      emailHost: [''],
      emailPort: [587, Validators.min(1)],
      emailUsername: [''],
      emailPassword: [''],
      emailHabilitado: [false],

      rabbitmqHost: [''],
      rabbitmqPort: [5672, Validators.min(1)],
      rabbitmqUsername: [''],
      rabbitmqPassword: [''],
      rabbitmqVhost: ['/'],
      rabbitmqQueue: [''],
      rabbitmqHabilitado: [false],

      minioEndpoint: [''],
      minioAccessKey: [''],
      minioSecretKey: [''],
      minioBucketName: [''],
      minioRegion: ['us-east-1'],
      minioHabilitado: [false],
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

  toggleEmailPassword(): void {
    this.mostrarEmailPassword.set(!this.mostrarEmailPassword());
  }

  toggleRabbitmqPassword(): void {
    this.mostrarRabbitmqPassword.set(!this.mostrarRabbitmqPassword());
  }

  toggleMinioSecretKey(): void {
    this.mostrarMinioSecretKey.set(!this.mostrarMinioSecretKey());
  }

  testarEmail(): void {
    this.isTestandoEmail.set(true);
    this.resultadoTeste.set(null);

    this.service.testarEmail().subscribe({
      next: (res) => {
        this.resultadoTeste.set({
          sucesso: res.sucesso,
          mensagem: res.mensagem,
        });
        this.isTestandoEmail.set(false);
      },
      error: () => {
        this.resultadoTeste.set({
          sucesso: false,
          mensagem: 'Erro ao testar conexão com servidor de Email.',
        });
        this.isTestandoEmail.set(false);
      },
    });
  }

  testarMinio(): void {
    this.isTestandoMinio.set(true);
    this.resultadoTeste.set(null);

    this.service.testarMinio().subscribe({
      next: (res) => {
        this.resultadoTeste.set({
          sucesso: res.sucesso,
          mensagem: res.mensagem,
        });
        this.isTestandoMinio.set(false);
      },
      error: () => {
        this.resultadoTeste.set({
          sucesso: false,
          mensagem: 'Erro ao testar conexão com MinIO/S3.',
        });
        this.isTestandoMinio.set(false);
      },
    });
  }

  testarRabbitmq(): void {
    this.isTestandoRabbitmq.set(true);
    this.resultadoTeste.set(null);

    this.service.testarRabbitmq().subscribe({
      next: (res) => {
        this.resultadoTeste.set({
          sucesso: res.sucesso,
          mensagem: res.mensagem,
        });
        this.isTestandoRabbitmq.set(false);
      },
      error: () => {
        this.resultadoTeste.set({
          sucesso: false,
          mensagem: 'Erro ao testar conexão com RabbitMQ.',
        });
        this.isTestandoRabbitmq.set(false);
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
        this.form.patchValue({
          emailPassword: config.emailPassword,
          rabbitmqPassword: config.rabbitmqPassword,
          minioSecretKey: config.minioSecretKey,
        });
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
