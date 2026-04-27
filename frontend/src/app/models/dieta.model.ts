export type TipoRefeicao =
  | 'AO_ACORDAR'
  | 'DESJEJUM'
  | 'ALMOCO'
  | 'LANCHE_DA_TARDE'
  | 'JANTAR'
  | 'CEIA';

export const TIPOS_REFEICAO: { value: TipoRefeicao; label: string }[] = [
  { value: 'AO_ACORDAR', label: 'Ao Acordar' },
  { value: 'DESJEJUM', label: 'Desjejum' },
  { value: 'ALMOCO', label: 'Almoço' },
  { value: 'LANCHE_DA_TARDE', label: 'Lanche da Tarde' },
  { value: 'JANTAR', label: 'Jantar' },
  { value: 'CEIA', label: 'Ceia' },
];

export interface AlimentoRefeicaoDTO {
  id?: number;
  nome: string;
  quantidade?: number;
  unidade?: string;
  ordem?: number;
  calorias?: number;
}

export interface RefeicaoOpcaoDTO {
  id?: number;
  numeroOpcao: number;
  alimentos: AlimentoRefeicaoDTO[];
}

export interface RefeicaoDTO {
  id?: number;
  tipo: TipoRefeicao;
  ordemExibicao: number;
  opcoes: RefeicaoOpcaoDTO[];
}

export interface SuplementoDietaDTO {
  id?: number;
  nome: string;
  dosagem?: string;
  timing?: string;
}

export interface DietaResumoResponse {
  id: number;
  titulo?: string;
  dataCriacao: string;
  objetivo?: string;
  kcalTotal?: number;
  totalRefeicoes: number;
}

export interface DietaResponse {
  id: number;
  pacienteId: number;
  nomePaciente: string;
  titulo?: string;
  dataCriacao: string;
  objetivo?: string;
  kcalTotal?: number;
  proteinasG?: number;
  carboidratosG?: number;
  gordurasG?: number;
  ingestaoAguaLitros?: number;
  observacoes?: string;
  refeicoes: RefeicaoDTO[];
  suplementos: SuplementoDietaDTO[];
}

export interface DietaContextoPacienteDTO {
  objetivoUltimaConsulta?: string;
  numeroRefeicoesDesejadas?: number;
}

export interface GerarDietaIARequest {
  kcalTotal: number;
  proteinasG: number;
  carboidratosG: number;
  gordurasG: number;
  titulo?: string;
  objetivo?: string;
}

export interface DietaRequest {
  titulo?: string;
  objetivo?: string;
  kcalTotal?: number;
  proteinasG?: number;
  carboidratosG?: number;
  gordurasG?: number;
  ingestaoAguaLitros?: number;
  observacoes?: string;
  refeicoes: {
    tipo: TipoRefeicao;
    ordemExibicao: number;
    opcoes: {
      numeroOpcao: number;
      alimentos: {
        nome: string;
        quantidade?: number;
        unidade?: string;
        calorias?: number;
        ordem?: number;
      }[];
    }[];
  }[];
  suplementos: {
    nome: string;
    dosagem?: string;
    timing?: string;
  }[];
}
