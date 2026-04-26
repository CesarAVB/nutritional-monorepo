import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  DietaContextoPacienteDTO,
  DietaRequest,
  DietaResponse,
  DietaResumoResponse,
} from '../models/dieta.model';

@Injectable({
  providedIn: 'root',
})
export class DietaService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/dietas`;

  buscarContextoPaciente(pacienteId: number): Observable<DietaContextoPacienteDTO> {
    return this.http.get<DietaContextoPacienteDTO>(`${this.apiUrl}/paciente/${pacienteId}/contexto`);
  }

  listarPorPaciente(pacienteId: number): Observable<DietaResumoResponse[]> {
    return this.http.get<DietaResumoResponse[]>(`${this.apiUrl}/paciente/${pacienteId}`);
  }

  buscarPorId(id: number): Observable<DietaResponse> {
    return this.http.get<DietaResponse>(`${this.apiUrl}/${id}`);
  }

  criar(pacienteId: number, request: DietaRequest): Observable<DietaResponse> {
    return this.http.post<DietaResponse>(`${this.apiUrl}/paciente/${pacienteId}`, request);
  }

  atualizar(id: number, request: DietaRequest): Observable<DietaResponse> {
    return this.http.put<DietaResponse>(`${this.apiUrl}/${id}`, request);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  baixarPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/pdf`, { responseType: 'blob' });
  }
}
