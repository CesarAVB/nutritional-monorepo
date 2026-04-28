export interface ConfiguracaoInfraestruturaResponse {
  id?: number;
  emailHost?: string;
  emailPort?: number;
  emailUsername?: string;
  emailPassword?: string;
  emailHabilitado: boolean;
  rabbitmqHost?: string;
  rabbitmqPort?: number;
  rabbitmqUsername?: string;
  rabbitmqPassword?: string;
  rabbitmqVhost?: string;
  rabbitmqQueue?: string;
  rabbitmqHabilitado: boolean;
  minioEndpoint?: string;
  minioAccessKey?: string;
  minioSecretKey?: string;
  minioBucketName?: string;
  minioRegion?: string;
  minioHabilitado: boolean;
  updatedAt?: string;
}

export interface ConfiguracaoInfraestruturaRequest {
  emailHost?: string;
  emailPort?: number;
  emailUsername?: string;
  emailPassword?: string;
  emailHabilitado: boolean;
  rabbitmqHost?: string;
  rabbitmqPort?: number;
  rabbitmqUsername?: string;
  rabbitmqPassword?: string;
  rabbitmqVhost?: string;
  rabbitmqQueue?: string;
  rabbitmqHabilitado: boolean;
  minioEndpoint?: string;
  minioAccessKey?: string;
  minioSecretKey?: string;
  minioBucketName?: string;
  minioRegion?: string;
  minioHabilitado: boolean;
}

export interface TesteConexaoResponse {
  sucesso: boolean;
  mensagem: string;
}
