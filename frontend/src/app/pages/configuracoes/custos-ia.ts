import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UsoIALogService } from '../../services/uso-ia-log.service';
import { UsoIALogResponse, UsoIAResumoResponse } from '../../models/uso-ia-log.model';

@Component({
  selector: 'app-custos-ia',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe, DecimalPipe],
  templateUrl: './custos-ia.html',
  styleUrls: ['./custos-ia.scss'],
})
export class CustosIAComponent implements OnInit {
  private service = inject(UsoIALogService);

  isCarregando = signal(true);
  registros = signal<UsoIALogResponse[]>([]);
  resumo = signal<UsoIAResumoResponse | null>(null);

  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  readonly pageSize = 20;

  ngOnInit(): void {
    this.carregarResumo();
    this.carregarRegistros(0);
  }

  carregarRegistros(page: number): void {
    this.isCarregando.set(true);
    this.service.listar(page, this.pageSize).subscribe({
      next: (data) => {
        this.registros.set(data.content);
        this.currentPage.set(data.number);
        this.totalPages.set(data.totalPages);
        this.totalElements.set(data.totalElements);
        this.isCarregando.set(false);
      },
      error: () => this.isCarregando.set(false),
    });
  }

  carregarResumo(): void {
    this.service.resumo().subscribe({
      next: (data) => this.resumo.set(data),
    });
  }

  irParaPagina(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.carregarRegistros(page);
    }
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages() }, (_, i) => i);
  }
}
