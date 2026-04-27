import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  AgendamentoSemanaResponseDto,
  AgendamentoResponseDto,
  ContadorHojeDto,
  AgendamentoRequestDto,
  StatusAgendamento
} from '../models/agendamento.model';

@Injectable({ providedIn: 'root' })
export class AgendamentoService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/agendamentos`;

  listarSemana(data?: string): Observable<AgendamentoSemanaResponseDto> {
    const params = data ? `?data=${data}` : '';
    return this.http.get<AgendamentoSemanaResponseDto>(`${this.apiUrl}/semana${params}`);
  }

  listarPorDia(data: string): Observable<AgendamentoResponseDto[]> {
    return this.http.get<AgendamentoResponseDto[]>(`${this.apiUrl}/dia?data=${data}`);
  }

  calcularSlots(data: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/slots?data=${data}`);
  }

  contarHoje(): Observable<ContadorHojeDto> {
    return this.http.get<ContadorHojeDto>(`${this.apiUrl}/contador-hoje`);
  }

  criar(dto: AgendamentoRequestDto): Observable<AgendamentoResponseDto> {
    return this.http.post<AgendamentoResponseDto>(this.apiUrl, dto);
  }

  atualizar(id: number, dto: AgendamentoRequestDto): Observable<AgendamentoResponseDto> {
    return this.http.put<AgendamentoResponseDto>(`${this.apiUrl}/${id}`, dto);
  }

  atualizarStatus(id: number, status: StatusAgendamento): Observable<AgendamentoResponseDto> {
    return this.http.patch<AgendamentoResponseDto>(`${this.apiUrl}/${id}/status`, { status });
  }

  cancelar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/cancelar`, {});
  }

  buscarPorId(id: number): Observable<AgendamentoResponseDto> {
    return this.http.get<AgendamentoResponseDto>(`${this.apiUrl}/${id}`);
  }
}
