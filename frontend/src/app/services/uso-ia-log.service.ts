import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PageResponse, UsoIALogResponse, UsoIAResumoResponse } from '../models/uso-ia-log.model';

@Injectable({
  providedIn: 'root',
})
export class UsoIALogService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/ia/uso`;

  listar(page = 0, size = 20): Observable<PageResponse<UsoIALogResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<UsoIALogResponse>>(this.apiUrl, { params });
  }

  resumo(): Observable<UsoIAResumoResponse> {
    return this.http.get<UsoIAResumoResponse>(`${this.apiUrl}/resumo`);
  }
}
