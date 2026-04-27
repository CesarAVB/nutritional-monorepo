import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ConfiguracaoAgendamentoDto, TesteWhatsappResponseDto } from '../models/configuracao-agendamento.model';

@Injectable({ providedIn: 'root' })
export class ConfiguracaoAgendamentoService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/configuracoes-agendamento`;

  buscar(): Observable<ConfiguracaoAgendamentoDto> {
    return this.http.get<ConfiguracaoAgendamentoDto>(this.apiUrl);
  }

  salvar(dto: ConfiguracaoAgendamentoDto): Observable<ConfiguracaoAgendamentoDto> {
    return this.http.put<ConfiguracaoAgendamentoDto>(this.apiUrl, dto);
  }

  testarWhatsapp(): Observable<TesteWhatsappResponseDto> {
    return this.http.post<TesteWhatsappResponseDto>(`${this.apiUrl}/testar-whatsapp`, {});
  }
}
