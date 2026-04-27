import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DietaService } from '../../../services/dieta';
import { ToastService } from '../../../services/toast';
import {
  DietaContextoPacienteDTO,
  DietaRequest,
  DietaResponse,
  GerarDietaIARequest,
  TIPOS_REFEICAO,
  TipoRefeicao,
} from '../../../models/dieta.model';

@Component({
  selector: 'app-dieta-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dieta-form.html',
  styleUrls: ['./dieta-form.scss'],
})
export class DietaFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private dietaService = inject(DietaService);
  private toastService = inject(ToastService);

  tiposRefeicao = TIPOS_REFEICAO;
  pacienteId = signal(0);
  dietaId = signal<number | null>(null);
  isEdicao = signal(false);
  isLoading = signal(false);
  isSaving = signal(false);
  isGerandoPdf = signal(false);
  isGerandoIA = signal(false);
  contexto = signal<DietaContextoPacienteDTO | null>(null);
  refeicoesFechadas = signal<Set<number>>(new Set());

  form!: FormGroup;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.params['id']);
    const dietaIdParam = this.route.snapshot.params['dietaId'];
    this.pacienteId.set(id);

    this.form = this.fb.group({
      titulo: [''],
      objetivo: [''],
      kcalTotal: [null],
      proteinasG: [null],
      carboidratosG: [null],
      gordurasG: [null],
      ingestaoAguaLitros: [null],
      observacoes: [''],
      refeicoes: this.fb.array([]),
      suplementos: this.fb.array([]),
    });

    // Inicializa todas como fechadas
    this.refeicoes.valueChanges.subscribe(() => {
      const novasFechadas = new Set<number>();
      for (let i = 0; i < this.refeicoes.length; i++) {
        novasFechadas.add(i);
      }
      this.refeicoesFechadas.set(novasFechadas);
    });

    this.dietaService.buscarContextoPaciente(id).subscribe({
      next: (ctx) => this.contexto.set(ctx),
      error: () => {},
    });

    if (dietaIdParam) {
      this.dietaId.set(Number(dietaIdParam));
      this.isEdicao.set(true);
      this.carregarDieta(Number(dietaIdParam));
    }
  }

  // ===================================================
  // # Getters do FormArray
  // ===================================================
  get refeicoes(): FormArray {
    return this.form.get('refeicoes') as FormArray;
  }

  get suplementos(): FormArray {
    return this.form.get('suplementos') as FormArray;
  }

  opcoesDe(refeicaoIndex: number): FormArray {
    return this.refeicoes.at(refeicaoIndex).get('opcoes') as FormArray;
  }

  alimentosDe(refeicaoIndex: number, opcaoIndex: number): FormArray {
    return this.opcoesDe(refeicaoIndex).at(opcaoIndex).get('alimentos') as FormArray;
  }

  // ===================================================
  // # Refeições
  // ===================================================
  get limiteRefeicoes(): number {
    return this.contexto()?.numeroRefeicoesDesejadas ?? 99;
  }

  adicionarRefeicao(): void {
    if (this.refeicoes.length >= this.limiteRefeicoes) return;
    this.refeicoes.push(this.criarRefeicaoGroup());
  }

  removerRefeicao(i: number): void {
    this.refeicoes.removeAt(i);
  }

  private criarRefeicaoGroup(tipo: TipoRefeicao = 'DESJEJUM'): FormGroup {
    const g = this.fb.group({
      tipo: [tipo, Validators.required],
      ordemExibicao: [0],
      opcoes: this.fb.array([this.criarOpcaoGroup(0)]),
    });
    return g;
  }

  toggleRefeicao(i: number): void {
    const atual = new Set(this.refeicoesFechadas());
    if (atual.has(i)) {
      atual.delete(i);
    } else {
      atual.add(i);
    }
    this.refeicoesFechadas.set(atual);
  }

  isRefeicaoFechada(i: number): boolean {
    return this.refeicoesFechadas().has(i);
  }

  labelTipo(tipo: TipoRefeicao): string {
    return TIPOS_REFEICAO.find((t) => t.value === tipo)?.label ?? tipo;
  }

  // ===================================================
  // # Opções
  // ===================================================
  adicionarOpcao(refeicaoIndex: number): void {
    const opcoes = this.opcoesDe(refeicaoIndex);
    opcoes.push(this.criarOpcaoGroup(opcoes.length));
  }

  removerOpcao(refeicaoIndex: number, opcaoIndex: number): void {
    this.opcoesDe(refeicaoIndex).removeAt(opcaoIndex);
  }

  private criarOpcaoGroup(numero: number): FormGroup {
    return this.fb.group({
      numeroOpcao: [numero],
      alimentos: this.fb.array([this.criarAlimentoGroup()]),
    });
  }

  // ===================================================
  // # Alimentos
  // ===================================================
  adicionarAlimento(refeicaoIndex: number, opcaoIndex: number): void {
    this.alimentosDe(refeicaoIndex, opcaoIndex).push(this.criarAlimentoGroup());
  }

  removerAlimento(refeicaoIndex: number, opcaoIndex: number, alimentoIndex: number): void {
    this.alimentosDe(refeicaoIndex, opcaoIndex).removeAt(alimentoIndex);
  }

  private criarAlimentoGroup(): FormGroup {
    return this.fb.group({
      nome: ['', Validators.required],
      quantidade: [null],
      unidade: [''],
      calorias: [null],
      ordem: [0],
    });
  }

  // ===================================================
  // # Suplementos
  // ===================================================
  adicionarSuplemento(): void {
    this.suplementos.push(this.fb.group({
      nome: ['', Validators.required],
      dosagem: [''],
      timing: [''],
    }));
  }

  removerSuplemento(i: number): void {
    this.suplementos.removeAt(i);
  }

  // ===================================================
  // # Carregar dieta existente (edição)
  // ===================================================
  private carregarDieta(id: number): void {
    this.isLoading.set(true);
    this.dietaService.buscarPorId(id).subscribe({
      next: (dieta) => {
        this.form.patchValue({
          kcalTotal: dieta.kcalTotal,
          proteinasG: dieta.proteinasG,
          carboidratosG: dieta.carboidratosG,
          gordurasG: dieta.gordurasG,
          ingestaoAguaLitros: dieta.ingestaoAguaLitros,
          observacoes: dieta.observacoes,
        });

        this.refeicoes.clear();
        dieta.refeicoes.forEach((r) => {
          const rg = this.fb.group({
            tipo: [r.tipo, Validators.required],
            ordemExibicao: [r.ordemExibicao],
            opcoes: this.fb.array(
              r.opcoes.map((o) =>
                this.fb.group({
                  numeroOpcao: [o.numeroOpcao],
                  alimentos: this.fb.array(
                    o.alimentos.map((a) =>
                      this.fb.group({
                        nome: [a.nome, Validators.required],
                        quantidade: [a.quantidade],
                        unidade: [a.unidade],
                        calorias: [a.calorias],
                        ordem: [a.ordem],
                      })
                    )
                  ),
                })
              )
            ),
          });
          this.refeicoes.push(rg);
        });

        this.suplementos.clear();
        dieta.suplementos.forEach((s) => {
          this.suplementos.push(this.fb.group({
            nome: [s.nome, Validators.required],
            dosagem: [s.dosagem],
            timing: [s.timing],
          }));
        });

        this.isLoading.set(false);
      },
      error: () => {
        this.toastService.error('Erro ao carregar dieta.');
        this.isLoading.set(false);
        this.voltar();
      },
    });
  }

  // ===================================================
  // # Salvar
  // ===================================================
  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastService.error('Preencha todos os campos obrigatórios.');
      return;
    }

    this.isSaving.set(true);
    const payload = this.form.getRawValue();

    const obs = this.isEdicao()
      ? this.dietaService.atualizar(this.dietaId()!, payload)
      : this.dietaService.criar(this.pacienteId(), payload);

    obs.subscribe({
      next: (dieta) => {
        this.toastService.success(this.isEdicao() ? 'Dieta atualizada!' : 'Dieta criada com sucesso!');
        this.isSaving.set(false);
        if (!this.isEdicao()) {
          this.dietaId.set(dieta.id);
          this.isEdicao.set(true);
        }
      },
      error: () => {
        this.toastService.error('Erro ao salvar dieta. Tente novamente.');
        this.isSaving.set(false);
      },
    });
  }

  // ===================================================
  // # Gerar PDF
  // ===================================================
  gerarPdf(): void {
    const id = this.dietaId();
    if (!id) {
      this.toastService.error('Salve a dieta antes de gerar o PDF.');
      return;
    }
    this.isGerandoPdf.set(true);
    this.dietaService.baixarPdf(id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `dieta-${id}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
        this.isGerandoPdf.set(false);
      },
      error: () => {
        this.toastService.error('Erro ao gerar PDF.');
        this.isGerandoPdf.set(false);
      },
    });
  }

  // ===================================================
  // # Gerar com IA
  // ===================================================
  get macrosPreenchidos(): boolean {
    const v = this.form.value;
    return !!(v.kcalTotal > 0 && v.proteinasG > 0 && v.carboidratosG > 0 && v.gordurasG > 0);
  }

  gerarComIA(): void {
    if (!this.macrosPreenchidos || this.isGerandoIA()) return;

    this.isGerandoIA.set(true);
    const v = this.form.value;
    const request: GerarDietaIARequest = {
      kcalTotal: v.kcalTotal,
      proteinasG: v.proteinasG,
      carboidratosG: v.carboidratosG,
      gordurasG: v.gordurasG,
      titulo: v.titulo || undefined,
      objetivo: v.objetivo || undefined,
    };

    this.dietaService.gerarComIA(this.pacienteId(), request).subscribe({
      next: (dieta) => {
        this.preencherFormComDietaGerada(dieta);
        this.toastService.success('Dieta gerada com sucesso! Revise as refeições e salve.');
        this.isGerandoIA.set(false);
      },
      error: (err) => {
        const msg = err?.error?.message || 'Erro ao gerar dieta com IA. Verifique as configurações.';
        this.toastService.error(msg);
        this.isGerandoIA.set(false);
      },
    });
  }

  private preencherFormComDietaGerada(dieta: DietaRequest): void {
    if (dieta.titulo) this.form.patchValue({ titulo: dieta.titulo });
    if (dieta.objetivo) this.form.patchValue({ objetivo: dieta.objetivo });
    if (dieta.observacoes) this.form.patchValue({ observacoes: dieta.observacoes });

    this.refeicoes.clear();
    (dieta.refeicoes ?? []).forEach((r) => {
      const rg = this.fb.group({
        tipo: [r.tipo, Validators.required],
        ordemExibicao: [r.ordemExibicao ?? 0],
        opcoes: this.fb.array(
          (r.opcoes ?? []).map((o) =>
            this.fb.group({
              numeroOpcao: [o.numeroOpcao ?? 1],
              alimentos: this.fb.array(
                (o.alimentos ?? []).map((a) =>
                  this.fb.group({
                    nome: [a.nome, Validators.required],
                    quantidade: [a.quantidade ?? null],
                    unidade: [a.unidade ?? ''],
                    calorias: [a.calorias ?? null],
                    ordem: [a.ordem ?? 0],
                  })
                )
              ),
            })
          )
        ),
      });
      this.refeicoes.push(rg);
    });

    this.suplementos.clear();
    (dieta.suplementos ?? []).forEach((s) => {
      this.suplementos.push(
        this.fb.group({
          nome: [s.nome, Validators.required],
          dosagem: [s.dosagem ?? ''],
          timing: [s.timing ?? ''],
        })
      );
    });
  }

  estimateKcal(refeicaoCtrl: any): number {
    let total = 0;
    const opcoes = refeicaoCtrl.get('opcoes') as FormArray;
    if (opcoes && opcoes.length > 0) {
      const primeiraOpcao = opcoes.at(0);
      const alimentos = primeiraOpcao.get('alimentos') as FormArray;
      if (alimentos) {
        alimentos.controls.forEach((a: any) => {
          const calorias = a.get('calorias')?.value;
          if (calorias) {
            total += Number(calorias);
          }
        });
      }
    }
    return Math.round(total * 10) / 10;
  }

  // ===================================================
  // # Navegação
  // ===================================================
  voltar(): void {
    this.router.navigate(['/pacientes', this.pacienteId()]);
  }
}

