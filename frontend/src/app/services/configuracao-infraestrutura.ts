import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ConfiguracaoInfraestruturaRequest,
  ConfiguracaoInfraestruturaResponse,
  TesteConexaoResponse
} from '../models/configuracao-infraestrutura.model';

@Injectable({ providedIn: 'root' })
export class ConfiguracaoInfraestruturaService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/configuracoes-infraestrutura`;

  buscar(): Observable<ConfiguracaoInfraestruturaResponse> {
    return this.http.get<ConfiguracaoInfraestruturaResponse>(this.apiUrl);
  }

  salvar(data: ConfiguracaoInfraestruturaRequest): Observable<ConfiguracaoInfraestruturaResponse> {
    return this.http.put<ConfiguracaoInfraestruturaResponse>(this.apiUrl, data);
  }

  testarEmail(): Observable<TesteConexaoResponse> {
    return this.http.post<TesteConexaoResponse>(`${this.apiUrl}/testar-email`, {});
  }

  testarMinio(): Observable<TesteConexaoResponse> {
    return this.http.post<TesteConexaoResponse>(`${this.apiUrl}/testar-minio`, {});
  }

  testarRabbitmq(): Observable<TesteConexaoResponse> {
    return this.http.post<TesteConexaoResponse>(`${this.apiUrl}/testar-rabbitmq`, {});
  }
}
