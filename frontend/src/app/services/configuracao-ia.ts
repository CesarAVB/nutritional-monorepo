import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ConfiguracaoIARequest,
  ConfiguracaoIAResponse,
  TesteConexaoResponse,
} from '../models/configuracao-ia.model';

@Injectable({
  providedIn: 'root',
})
export class ConfiguracaoIAService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/configuracoes-ia`;

  buscar(): Observable<ConfiguracaoIAResponse> {
    return this.http.get<ConfiguracaoIAResponse>(this.apiUrl);
  }

  salvar(request: ConfiguracaoIARequest): Observable<ConfiguracaoIAResponse> {
    return this.http.put<ConfiguracaoIAResponse>(this.apiUrl, request);
  }

  testarConexao(): Observable<TesteConexaoResponse> {
    return this.http.post<TesteConexaoResponse>(`${this.apiUrl}/testar`, {});
  }
}
