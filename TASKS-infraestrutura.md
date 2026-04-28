# Tasks — Configurações de Infraestrutura via Frontend

## Contexto

Configurações de MinIO/S3, RabbitMQ e Email estão hardcoded em properties. O objetivo é torná-las editáveis via frontend, persistidas no banco, aplicadas em runtime sem restart — seguindo o padrão de `tbl_configuracoes_ia` (V7) e `tbl_configuracao_agendamento` (V11).

---

## Tasks

### TASK-01 — Migration Flyway V13
**Arquivo:** `backend/src/main/resources/db/migration/V13__create_table_configuracoes_infraestrutura.sql`

Criar `tbl_configuracoes_infraestrutura` (registro singleton, id=1):
- Campos Email: `email_host`, `email_port`, `email_username`, `email_password`, `email_habilitado`
- Campos RabbitMQ: `rabbitmq_host`, `rabbitmq_port`, `rabbitmq_username`, `rabbitmq_password`, `rabbitmq_vhost`, `rabbitmq_queue`, `rabbitmq_habilitado`
- Campos MinIO: `minio_endpoint`, `minio_access_key`, `minio_secret_key`, `minio_bucket_name`, `minio_region`, `minio_habilitado`
- `updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
- INSERT inicial com todos os `*_habilitado = FALSE`

---

### TASK-02 — Entidade e Repository
**Novos arquivos:**
- `backend/src/main/java/br/com/sistema/alimentos/models/ConfiguracaoInfraestrutura.java` — @Entity com todos os campos, @Lombok @Data
- `backend/src/main/java/br/com/sistema/alimentos/repositories/ConfiguracaoInfraestruturaRepository.java` — `extends JpaRepository<ConfiguracaoInfraestrutura, Long>` com `findFirstBy()`

---

### TASK-03 — DTOs
**Novos arquivos:**
- `backend/src/main/java/br/com/sistema/alimentos/dtos/request/ConfiguracaoInfraestruturaRequest.java` — todos os campos editáveis
- `backend/src/main/java/br/com/sistema/alimentos/dtos/response/ConfiguracaoInfraestruturaResponse.java` — mascara senhas (retorna `"***"` se preenchida)

---

### TASK-04 — Reconfiguração dinâmica dos Services

**Modificar** `backend/src/main/java/br/com/sistema/alimentos/services/EmailService.java`:
- Remover injeção de `JavaMailSender` via Spring
- Manter `JavaMailSenderImpl mailSender` como campo mutável
- Adicionar `reconfigurar(ConfiguracaoInfraestrutura config)` que recria o sender com novos dados

**Modificar** `backend/src/main/java/br/com/sistema/alimentos/services/AuditProducerService.java`:
- Remover injeção de `RabbitTemplate` via Spring
- Manter `CachingConnectionFactory` e `RabbitTemplate` como campos mutáveis
- Adicionar `reconfigurar(ConfiguracaoInfraestrutura config)` que destrói a factory atual e recria

**Modificar** `backend/src/main/java/br/com/sistema/alimentos/services/S3Service.java`:
- Remover injeção de `S3Client` e `S3Presigner` via Spring
- Manter como campos mutáveis
- Adicionar `reconfigurar(ConfiguracaoInfraestrutura config)` que recria os clientes S3

---

### TASK-05 — ConfiguracaoInfraestruturaService
**Novo arquivo:** `backend/src/main/java/br/com/sistema/alimentos/services/ConfiguracaoInfraestruturaService.java`

- `buscar()` — retorna DTO (cria registro se não existir)
- `salvar(request)` — persiste e chama `aplicarConfiguracoes()`
- `@PostConstruct aplicarConfiguracoes()` — carrega do banco e chama `reconfigurar()` nos 3 services
- Injeta: `EmailService`, `AuditProducerService`, `S3Service`, `ConfiguracaoInfraestruturaRepository`

---

### TASK-06 — Remover auto-configuração Spring e limpar properties

**Modificar** `backend/src/main/java/br/com/sistema/alimentos/configurations/MinioConfig.java` — remover (S3Client deixa de ser bean Spring)
**Modificar** `backend/src/main/java/br/com/sistema/alimentos/configurations/MinioInitializer.java` — remover
**Modificar** `backend/src/main/java/br/com/sistema/alimentos/configurations/DummyS3Config.java` — remover
**Modificar** `backend/src/main/java/br/com/sistema/alimentos/configurations/RabbitMQConfig.java` — manter apenas declaração da Queue durável (sem @Value de host/porta)
**Modificar** `backend/src/main/resources/application-local.properties` — remover blocos `spring.mail.*`, `spring.rabbitmq.*`, `minio.*`
**Modificar** `backend/src/main/resources/application-prod.properties` — idem
**Modificar** `backend/src/main/resources/application.properties` — adicionar:
```
spring.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
  org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration
```

---

### TASK-07 — Controller
**Novo arquivo:** `backend/src/main/java/br/com/sistema/alimentos/controllers/ConfiguracaoInfraestruturaController.java`

Endpoints:
- `GET  /api/v1/configuracoes-infraestrutura` — buscar
- `PUT  /api/v1/configuracoes-infraestrutura` — salvar
- `POST /api/v1/configuracoes-infraestrutura/testar-email` — envia email de teste
- `POST /api/v1/configuracoes-infraestrutura/testar-minio` — verifica bucket
- `POST /api/v1/configuracoes-infraestrutura/testar-rabbitmq` — verifica conexão
- Retorno dos testes: `{ sucesso: boolean, mensagem: string }`

---

### TASK-08 — Models TypeScript
**Novo arquivo:** `frontend/src/app/models/configuracao-infraestrutura.ts`

```typescript
export interface ConfiguracaoInfraestruturaResponse { ... }
export interface ConfiguracaoInfraestruturaRequest { ... }
export interface TesteConexaoResponse { sucesso: boolean; mensagem: string; }
```

---

### TASK-09 — Service Angular
**Novo arquivo:** `frontend/src/app/services/configuracao-infraestrutura.ts`

- `buscar()` GET
- `salvar(data)` PUT
- `testarEmail()`, `testarMinio()`, `testarRabbitmq()` POST

---

### TASK-10 — Componente Angular
**Novos arquivos:**
- `frontend/src/app/pages/configuracoes/configuracoes-infraestrutura/configuracoes-infraestrutura.ts`
- `frontend/src/app/pages/configuracoes/configuracoes-infraestrutura/configuracoes-infraestrutura.html`

Padrão `configuracoes-agendamento`:
- 3 abas com signal `abaAtiva`: **Email** | **MinIO/S3** | **RabbitMQ**
- Toggle `*_habilitado` habilita/desabilita campos da seção
- Botão "Testar conexão" por aba
- Toggle mostrar/ocultar senha
- Signals: `isSaving`, `isTestandoEmail`, `isTestandoMinio`, `isTestandoRabbitmq`, `isCarregando`

---

### TASK-11 — Roteamento e Menu
**Modificar** `frontend/src/app/app.routes.ts`:
- Adicionar `{ path: 'infraestrutura', component: ConfiguracoesInfraestruturaComponent }` nos children de `configuracoes`

**Modificar** layout/menu de configurações:
- Adicionar link "Infraestrutura" ao lado de "Agendamento", "IA" e "Custos IA"

---

## Verificação

1. Backend inicia sem as properties removidas → sem erro de startup
2. `GET /api/v1/configuracoes-infraestrutura` retorna registro com senhas mascaradas
3. Frontend `/configuracoes/infraestrutura` carrega sem erro
4. Salvar config de Email → `POST /testar-email` envia email real
5. Salvar config de MinIO → `POST /testar-minio` conecta ao bucket
6. Salvar config de RabbitMQ → `POST /testar-rabbitmq` conecta à fila
7. Upload de foto → vai para o MinIO configurado
8. Cadastro de paciente → auditoria chega na fila RabbitMQ configurada
9. Notificação de agendamento → email enviado pelo servidor configurado
