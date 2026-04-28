export type ProvedorIA = 'OPENAI' | 'OPENROUTER';

export interface ConfiguracaoIAResponse {
  id: number;
  provedor: ProvedorIA;
  apiKey: string;
  modelo: string;
  baseUrl?: string;
  promptSistema?: string;
  temperaturaModelo: number;
  precoInputPorMilhao?: number;
  precoOutputPorMilhao?: number;
}

export interface ConfiguracaoIARequest {
  provedor: ProvedorIA;
  apiKey: string;
  modelo: string;
  baseUrl?: string;
  promptSistema?: string;
  temperaturaModelo: number;
  precoInputPorMilhao?: number | null;
  precoOutputPorMilhao?: number | null;
}

export interface TesteConexaoResponse {
  sucesso: boolean;
  modelo?: string;
  provedor?: ProvedorIA;
  erro?: string;
}
