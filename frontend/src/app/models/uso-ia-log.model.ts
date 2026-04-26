export interface UsoIALogResponse {
  id: number;
  dataHora: string;
  provedor: string;
  modelo: string;
  tokensEntrada: number;
  tokensSaida: number;
  custoUsd: number;
  sucesso: boolean;
  pacienteId: number | null;
}

export interface UsoIAResumoResponse {
  totalChamadas: number;
  totalTokens: number;
  custoTotalUsd: number;
  custoUltimos30DiasUsd: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
