# Plano: Feature de Agendamento

## Contexto

O sistema de nutrição já possui `Consulta` (visita clínica com avaliações), mas não tem agendamento. A feature adiciona um calendário navegável na dashboard, CRUD de agendamentos com bloqueio de conflitos, cadência de notificações via WhatsApp (Evolution API) + Email, confirmação por webhook e uma tela de configurações por tenant.

**Achados importantes do codebase:**
- Spring Boot 3.5.9, Java 21 (CLAUDE.md menciona 4/25, mas o pom.xml real é 3.5.9/21)
- Última migration: V9 — próxima deve ser V10
- `@EnableScheduling` não existe ainda — precisa adicionar
- Email: `spring-boot-starter-mail` presente no pom, mas SMTP comentado em properties
- Sem multi-tenant implementado — sistema single-tenant (dados sem isolamento de nutricionista)
- Sem entidade Usuario/Nutricionista — tratar ConfiguracaoAgendamento como singleton (igual ao ConfiguracaoIA)
- `ConfiguracaoIA` já tem padrão de mascarar API Key — replicar para AES no Evolution

---

## Etapa 1 — Fundação: Entidades, Enums e Migrations (Backend)

**Objetivo:** Criar a camada de dados sem nenhuma lógica de negócio.

### 1.1 Novos Enums

Arquivo: `backend/src/main/java/br/com/sistema/alimentos/enums/`

```java
// TipoAgendamento.java
PRIMEIRA_CONSULTA, RETORNO, AVALIACAO

// StatusAgendamento.java  
AGUARDANDO_CONFIRMACAO, CONFIRMADO, REALIZADO, CANCELADO, FALTA

// TipoNotificacao.java
IMEDIATO, LEMBRETE_72H, LEMBRETE_24H, LEMBRETE_2H, ALERTA_NUTRICIONISTA

// CanalNotificacao.java
WHATSAPP, EMAIL

// StatusNotificacao.java
PENDENTE, ENVIADO, FALHOU
```

### 1.2 Migrations Flyway

**V10__create_table_agendamentos.sql**
```sql
CREATE TABLE tbl_agendamentos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  paciente_id BIGINT NOT NULL,
  data_hora_inicio DATETIME NOT NULL,
  data_hora_fim DATETIME NOT NULL,
  duracao_minutos INT NOT NULL,
  tipo ENUM('PRIMEIRA_CONSULTA','RETORNO','AVALIACAO') NOT NULL,
  status ENUM('AGUARDANDO_CONFIRMACAO','CONFIRMADO','REALIZADO','CANCELADO','FALTA')
         NOT NULL DEFAULT 'AGUARDANDO_CONFIRMACAO',
  observacoes TEXT,
  criado_em DATETIME NOT NULL,
  atualizado_em DATETIME,
  FOREIGN KEY (paciente_id) REFERENCES tbl_pacientes(id),
  INDEX idx_agendamento_data (data_hora_inicio),
  INDEX idx_agendamento_paciente (paciente_id)
);
```

**V11__create_table_configuracao_agendamento.sql**
```sql
CREATE TABLE tbl_configuracao_agendamento (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  duracao_padrao_minutos INT NOT NULL DEFAULT 50,
  intervalo_entre_consultas_minutos INT NOT NULL DEFAULT 10,
  dias_atendimento VARCHAR(50) NOT NULL DEFAULT 'SEG,TER,QUA,QUI,SEX',
  horario_inicio TIME NOT NULL DEFAULT '08:00:00',
  horario_fim TIME NOT NULL DEFAULT '18:00:00',
  fuso_horario VARCHAR(50) NOT NULL DEFAULT 'America/Sao_Paulo',
  janela_notif_inicio TIME NOT NULL DEFAULT '08:00:00',
  janela_notif_fim TIME NOT NULL DEFAULT '22:00:00',
  email_notificacao VARCHAR(200),
  notif_ao_criar BOOLEAN NOT NULL DEFAULT TRUE,
  notif_ao_cancelar BOOLEAN NOT NULL DEFAULT TRUE,
  alerta_nao_confirmacao BOOLEAN NOT NULL DEFAULT TRUE,
  lembrete_imediato_ativo BOOLEAN NOT NULL DEFAULT TRUE,
  lembrete_72h_ativo BOOLEAN NOT NULL DEFAULT TRUE,
  lembrete_24h_ativo BOOLEAN NOT NULL DEFAULT TRUE,
  lembrete_2h_ativo BOOLEAN NOT NULL DEFAULT TRUE,
  evolution_url VARCHAR(500),
  evolution_instancia VARCHAR(200),
  evolution_api_key TEXT,    -- AES-encrypted via AttributeConverter
  template_confirmacao TEXT,
  template_lembrete_72h TEXT,
  template_lembrete_24h TEXT,
  template_lembrete_2h TEXT,
  template_consulta_confirmada TEXT,
  template_consulta_cancelada TEXT
);
INSERT INTO tbl_configuracao_agendamento (duracao_padrao_minutos, intervalo_entre_consultas_minutos)
VALUES (50, 10);
```

**V12__create_table_notificacao_agendamento.sql**
```sql
CREATE TABLE tbl_notificacao_agendamento (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  agendamento_id BIGINT NOT NULL,
  tipo ENUM('IMEDIATO','LEMBRETE_72H','LEMBRETE_24H','LEMBRETE_2H','ALERTA_NUTRICIONISTA') NOT NULL,
  canal ENUM('WHATSAPP','EMAIL') NOT NULL,
  status ENUM('PENDENTE','ENVIADO','FALHOU') NOT NULL DEFAULT 'PENDENTE',
  enviado_em DATETIME,
  mensagem TEXT,
  erro TEXT,
  FOREIGN KEY (agendamento_id) REFERENCES tbl_agendamentos(id),
  INDEX idx_notif_agendamento (agendamento_id),
  INDEX idx_notif_tipo_status (tipo, status)
);
```

### 1.3 Entidades JPA

Arquivos: `backend/src/main/java/br/com/sistema/alimentos/entity/`

- `Agendamento.java` — campos conforme migration V10, `@ManyToOne Paciente`, `@PrePersist` para `criadoEm`
- `ConfiguracaoAgendamento.java` — campos conforme V11, campo `evolutionApiKey` anotado com `@Convert(converter = AesEncryptedConverter.class)`
- `NotificacaoAgendamento.java` — campos conforme V12, `@ManyToOne Agendamento`
- `AesEncryptedConverter.java` — `AttributeConverter<String, String>` com javax.crypto AES/GCM; chave lida de `${agendamento.aes-key}` nas properties

---

## Etapa 2 — Backend CRUD Agendamento

**Objetivo:** Endpoints REST para criar, listar, editar, cancelar agendamentos + verificação de conflito + slots disponíveis.

### 2.1 DTOs

Pacote: `dtos/request/` e `dtos/response/`

```
AgendamentoRequest.java  → pacienteId, dataHoraInicio, duracaoMinutos, tipo, observacoes
AgendamentoResponse.java → todos campos + nomePaciente, telefoneWhatsapp
AgendamentoSemanaResponse.java → List<AgendamentoDiaDTO> (7 dias, para calendário)
SlotDisponivelResponse.java → List<LocalTime> slots livres numa data
```

### 2.2 Repository

`AgendamentoRepository.java` — métodos customizados:
```java
// Detectar conflito de horário
List<Agendamento> findConflitos(LocalDateTime inicio, LocalDateTime fim, Long excluirId);
// Agendamentos de um dia
List<Agendamento> findByDia(LocalDate data);
// Para o job de notificações (próximos X minutos)
List<Agendamento> findPendentesNotificacao(LocalDateTime from, LocalDateTime to);
// Contagem de hoje (para badge)
long countHoje();
```

### 2.3 AgendamentoService

Lógica principal:
- `criar()` — verifica conflito → verifica dias/horários de atendimento da config → persiste com `AGUARDANDO_CONFIRMACAO`
- `atualizar()` — permite reagendar, revalida conflitos
- `cancelar(id)` — muda status para `CANCELADO`
- `listarSemana(dataReferencia)` — retorna 7 dias com agendamentos
- `listarPorDia(data)` — lista do dia para tela de agendamentos
- `calcularSlotsDisponiveis(data)` — usa config (horário início/fim, duração padrão, intervalo) e exclui horários já ocupados
- `confirmarPorWhatsapp(numeroPaciente)` — atualiza status para `CONFIRMADO`
- `cancelarPorWhatsapp(numeroPaciente)` — atualiza status para `CANCELADO`

### 2.4 AgendamentoController

`/api/v1/agendamentos`
```
POST /                  → criar
GET /semana?data=       → calendário semanal
GET /dia?data=          → lista do dia
GET /slots?data=        → horários disponíveis
GET /{id}               → detalhe
PUT /{id}               → editar
PATCH /{id}/cancelar    → cancelar
PATCH /{id}/status      → atualizar status (REALIZADO, FALTA, etc.)
GET /contador-hoje      → { quantidade: N } para badge no header
```

### 2.5 DashboardController — atualizar

Remover `consultasHoje()` ou manter como alias. Adicionar `countAgendamentosHoje()`.

---

## Etapa 3 — Backend ConfiguracaoAgendamento

**Objetivo:** CRUD da configuração singleton + endpoint de teste da Evolution API.

### 3.1 ConfiguracaoAgendamentoService

- `buscar()` — retorna config mascarando `evolutionApiKey` (igual ao padrão de ConfiguracaoIA)
- `salvar(request)` — preserva a chave se contiver "..." (padrão já usado em ConfiguracaoIA)
- `buscarParaUso()` — retorna com chave real (uso interno)
- `testarConexaoWhatsapp()` — chama Evolution API `GET /instance/fetchInstances` com credenciais

### 3.2 ConfiguracaoAgendamentoController

`/api/v1/configuracoes-agendamento`
```
GET /          → buscar (com chave mascarada)
PUT /          → salvar
POST /testar-whatsapp → testar conexão Evolution API
```

---

## Etapa 4 — Backend Notificações e Webhook

**Objetivo:** Job agendado para envio de notificações e webhook para receber respostas do WhatsApp.

### 4.1 EvolutionApiService

Usa `RestTemplate` (bean já existente em `RestTemplateConfig`):
- `enviarMensagem(url, instancia, apiKey, numero, mensagem)` — POST para Evolution API
- `buscarInstancia(url, instancia, apiKey)` — GET para verificar status

### 4.2 EmailService

Ativar `JavaMailSender` (dependência já no pom):
- Descomentrar configurações SMTP no `application-local.properties` e `application-prod.properties`
- `enviarEmail(destinatario, assunto, corpo)` — método simples

### 4.3 NotificacaoAgendamentoService

Resposável por:
- `processarNotificacoesAgendamento(agendamento, tipo)` — cria registros em `tbl_notificacao_agendamento` e chama EvolutionApiService/EmailService
- `substituirVariaveis(template, agendamento)` — substitui `{{paciente_nome}}`, `{{nutricionista_nome}}`, `{{data_consulta}}`, etc.
- `jaEnviado(agendamentoId, tipo)` — verifica duplicata antes de enviar

### 4.4 NotificacaoSchedulerJob

Arquivo: `backend/src/main/java/br/com/sistema/alimentos/service/NotificacaoSchedulerJob.java`

```java
@Component
@EnableScheduling  // adicionar em main class também
public class NotificacaoSchedulerJob {
    @Scheduled(fixedDelay = 60000)  // a cada 1 minuto
    public void processarNotificacoes() {
        // 1. Carrega config
        // 2. Verifica janela de horário permitido
        // 3. Busca agendamentos com notificação pendente (72h, 24h, 2h à frente)
        // 4. Para cada um, verifica se já enviou (tbl_notificacao_agendamento)
        // 5. Substitui variáveis no template, envia, registra
    }
}
```

### 4.5 WhatsAppWebhookController

`/webhook/whatsapp`
```
POST / → recebe evento Evolution API
```

Lógica:
- Filtra `event == "messages.upsert"` e `fromMe == false`
- Extrai número do remetente
- Normaliza texto: `CONFIRMAR` → `confirmarPorWhatsapp()`, `CANCELAR` → `cancelarPorWhatsapp()`
- Envia mensagem de feedback pelo mesmo canal
- Endpoint **sem autenticação** (rota liberada no SecurityConfig / CorsConfig)

---

## Etapa 5 — Frontend: Dashboard com Calendário Semanal

**Objetivo:** Substituir a seção "Consultas de Hoje" por um calendário semanal e adicionar badge no header.

### 5.1 Arquivos a modificar

- `frontend/src/app/pages/dashboard/dashboard.ts` — remover `consultasHoje` signal, adicionar `semanaAgendamentos`, `dataReferencia` (signal), `navegarSemana()`, `irParaDia(data)`
- `frontend/src/app/pages/dashboard/dashboard.html` — substituir seção "Consultas de Hoje" pelo componente `<app-week-calendar>`
- `frontend/src/app/pages/dashboard/dashboard.scss`

### 5.2 Novo componente

`frontend/src/app/shared/week-calendar/` (componente reutilizável)
- Exibe 7 dias da semana atual
- Cada dia mostra chips/badges com horários dos agendamentos
- Navegação por semana (setas anterior/próxima, botão "Hoje")
- Click em dia → `router.navigate(['/agendamentos'], { queryParams: { data } })`
- Input: `agendamentos: AgendamentoSemanaResponse`

### 5.3 Badge no header

`frontend/src/app/components/navbar/navbar.ts`:
- Injetar `AgendamentoService`
- Signal `consultasHoje = signal(0)`
- `ngOnInit` → chamar `GET /api/v1/agendamentos/contador-hoje`
- Template: ícone calendário com badge vermelho se `consultasHoje() > 0`, navega para `/agendamentos?data=hoje`

### 5.4 Novo model e service

`frontend/src/app/models/agendamento.model.ts`
`frontend/src/app/services/agendamento.ts`

---

## Etapa 6 — Frontend: Telas de Agendamento

**Objetivo:** Tela de lista por dia e form de criação/edição.

### 6.1 Estrutura de pastas

```
frontend/src/app/pages/agendamentos/
  agendamentos-dia/
    agendamentos-dia.ts
    agendamentos-dia.html
    agendamentos-dia.scss
  agendamento-form/
    agendamento-form.ts
    agendamento-form.html
    agendamento-form.scss
```

### 6.2 AgendamentosDiaComponent

- Recebe `?data=YYYY-MM-DD` via `ActivatedRoute`
- Lista agendamentos do dia com status colorido
- Botão "Novo Agendamento" abre form
- Ações por agendamento: editar, cancelar, marcar como realizado/falta

### 6.3 AgendamentoFormComponent

Reactive form com:
- **Busca de paciente**: campo de busca com autocomplete chamando `/api/v1/pacientes/buscar`
- **Data**: date picker (HTML native ou similar)
- **Horário**: select populado dinamicamente com `GET /api/v1/agendamentos/slots?data=`
- **Duração**: número (pre-populado da config)
- **Tipo**: select com TipoAgendamento
- **Observações**: textarea
- **Status**: select com StatusAgendamento (apenas na edição)
- Submit → POST ou PUT, navega de volta para `/agendamentos?data=`

### 6.4 Rotas a adicionar em `app.routes.ts`

```typescript
{ path: 'agendamentos', component: AgendamentosDiaComponent, canActivate: [authGuard], title: 'Agendamentos - NutriControl' },
{ path: 'agendamentos/novo', component: AgendamentoFormComponent, canActivate: [authGuard], title: 'Novo Agendamento - NutriControl' },
{ path: 'agendamentos/:id/editar', component: AgendamentoFormComponent, canActivate: [authGuard], title: 'Editar Agendamento - NutriControl' },
```

---

## Etapa 7 — Frontend: Tela de Configurações de Agendamento

**Objetivo:** Tela com 5 seções (tabs ou accordion) para configurar tudo por tenant.

### 7.1 Estrutura

```
frontend/src/app/pages/configuracoes/
  configuracoes-agendamento/
    configuracoes-agendamento.ts
    configuracoes-agendamento.html
    configuracoes-agendamento.scss
```

### 7.2 Seções da tela

**5a — Integração WhatsApp:**
- Campos: URL da instância, Nome da instância, API Key (input type=password)
- Botão "Testar Conexão" → chama `POST /api/v1/configuracoes-agendamento/testar-whatsapp`
- Exibe status retornado (número conectado, estado)

**5b — Cadência de Notificações:**
- Toggle + campo "horas antes" para cada lembrete (imediato, 72h, 24h, 2h)
- Toggle "Alertar nutricionista se não confirmado"
- Campos de horário início/fim para janela de envio

**5c — Templates de Mensagem:**
- 6 textareas (confirmação, lembrete 72h, 24h, 2h, consulta confirmada, consulta cancelada)
- Painel lateral (ou tooltip) mostrando variáveis disponíveis: `{{paciente_nome}}`, `{{nutricionista_nome}}`, `{{data_consulta}}`, `{{hora_consulta}}`, `{{tipo_consulta}}`, `{{link_reagendamento}}`

**5d — Configurações de Agenda:**
- Duração padrão (minutos)
- Intervalo entre consultas (minutos)
- Checkboxes dias da semana (SEG a DOM)
- Horário início/fim
- Select fuso horário

**5e — Notificações para o Nutricionista:**
- Toggles: ao criar, ao cancelar, alerta de não confirmação
- Campo email de destino

### 7.3 Rota a adicionar

```typescript
{ path: 'configuracoes/agendamento', component: ConfiguracoesAgendamentoComponent, canActivate: [authGuard], title: 'Configurações de Agendamento - NutriControl' },
```

Menu em `navbar.html`: adicionar link "Configurações de Agendamento" no user dropdown (junto com Configurações de IA).

---

## Arquivos Críticos

| Arquivo | Ação |
|---|---|
| `backend/src/main/resources/db/migration/V10__*.sql` | Criar |
| `backend/src/main/resources/db/migration/V11__*.sql` | Criar |
| `backend/src/main/resources/db/migration/V12__*.sql` | Criar |
| `backend/src/main/java/.../entity/Agendamento.java` | Criar |
| `backend/src/main/java/.../entity/ConfiguracaoAgendamento.java` | Criar |
| `backend/src/main/java/.../entity/NotificacaoAgendamento.java` | Criar |
| `backend/src/main/java/.../entity/AesEncryptedConverter.java` | Criar |
| `backend/src/main/java/.../service/AgendamentoService.java` | Criar |
| `backend/src/main/java/.../service/ConfiguracaoAgendamentoService.java` | Criar |
| `backend/src/main/java/.../service/NotificacaoAgendamentoService.java` | Criar |
| `backend/src/main/java/.../service/EvolutionApiService.java` | Criar |
| `backend/src/main/java/.../service/EmailService.java` | Criar |
| `backend/src/main/java/.../service/NotificacaoSchedulerJob.java` | Criar |
| `backend/src/main/java/.../controller/AgendamentoController.java` | Criar |
| `backend/src/main/java/.../controller/ConfiguracaoAgendamentoController.java` | Criar |
| `backend/src/main/java/.../controller/WhatsAppWebhookController.java` | Criar |
| `backend/src/main/resources/application-local.properties` | Editar (SMTP + AES key) |
| `backend/src/main/resources/application-prod.properties` | Editar (SMTP + AES key) |
| `backend/src/main/java/.../BackendApplication.java` | Editar (adicionar @EnableScheduling) |
| `frontend/src/app/models/agendamento.model.ts` | Criar |
| `frontend/src/app/services/agendamento.ts` | Criar |
| `frontend/src/app/shared/week-calendar/` | Criar |
| `frontend/src/app/pages/dashboard/dashboard.ts` | Editar |
| `frontend/src/app/pages/dashboard/dashboard.html` | Editar |
| `frontend/src/app/pages/agendamentos/` | Criar |
| `frontend/src/app/pages/configuracoes/configuracoes-agendamento/` | Criar |
| `frontend/src/app/components/navbar/navbar.ts` | Editar (badge) |
| `frontend/src/app/components/navbar/navbar.html` | Editar (badge + link) |
| `frontend/src/app/app.routes.ts` | Editar (novas rotas) |

---

## Verificação por Etapa

| Etapa | Como testar |
|---|---|
| 1 | `mvn flyway:migrate` — migrations V10/V11/V12 aplicadas sem erro |
| 2 | Swagger UI `/swagger-ui.html` → testar POST/GET agendamentos; verificar erro 409 em conflito |
| 3 | Swagger → `GET /api/v1/configuracoes-agendamento`; `POST /testar-whatsapp` com credenciais reais |
| 4 | Criar agendamento e aguardar 1 minuto → verificar linha em `tbl_notificacao_agendamento`; simular webhook com curl |
| 5 | `ng serve` → dashboard exibe calendário semanal; badge aparece se houver agendamento hoje |
| 6 | Criar agendamento via form → slot some da lista; navegação por semana funciona |
| 7 | Salvar config → API Key aparece mascarada no GET; "Testar Conexão" retorna status |

---

## Observações

- **Single-tenant**: `ConfiguracaoAgendamento` é singleton (INSERT inicial na migration), igual a `ConfiguracaoIA`
- **AES key**: adicionar `agendamento.aes-key=<base64-32bytes>` em `application-local.properties` (não commitada) e `application-prod.properties` como env var `${AGENDAMENTO_AES_KEY}`
- **Webhook sem auth**: rota `/webhook/whatsapp` deve ser adicionada à lista de rotas públicas no `CorsConfig` (e no SecurityConfig quando for implementado)
- **Pacote base real**: exploração encontrou `br.com.sistema` nos entities — confirmar antes de criar novos arquivos (CLAUDE.md diz `br.com.sistema.alimentos`)
