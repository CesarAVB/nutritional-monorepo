# Sistema de Gestão Nutricional - API

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> **Microserviço REST** para gestão completa de consultas nutricionais, acompanhamento evolutivo de pacientes, avaliações físicas detalhadas e controle de histórico nutricional.

🌐 **API em Produção**: [https://api-sysnutritional.cesaravb.com.br](https://api-sysnutritional.cesaravb.com.br)

---

## Sobre o Projeto

Sistema desenvolvido para **nutricionistas** que precisam gerenciar seus pacientes de forma profissional, com foco em:

- **Acompanhamento Evolutivo**: Comparação de medidas, peso e percentual de gordura entre consultas
- **Registro Fotográfico**: Armazenamento de fotos (anterior, posterior, laterais) para análise visual
- **Anamnese Completa**: Questionário detalhado de estilo de vida, hábitos e histórico clínico
- **Avaliação Antropométrica**: Perímetros corporais, dobras cutâneas e composição corporal
- **Histórico Detalhado**: Timeline completa de todas as consultas por paciente

---

## Tecnologias Utilizadas

### Backend
- **Java 21** - Linguagem de programação
- **Spring Boot 3.5.9** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Hibernate** - ORM para mapeamento objeto-relacional
- **Spring Validation** - Validação de dados com Bean Validation

### Banco de Dados
- **MySQL 8.0** - Banco de dados relacional
- **Flyway** - Versionamento e migração de banco de dados

### Documentação
- **SpringDoc OpenAPI 3** - Documentação automática da API
- **Swagger UI** - Interface interativa para testar endpoints

### Ferramentas
- **Lombok** - Redução de código boilerplate
- **Log4j2** - Sistema de logs estruturado
- **Maven** - Gerenciamento de dependências
- **Spring DevTools** - Hot reload durante desenvolvimento

---

## Estrutura do Projeto

```
backend-nutritional/
│
├── src/main/java/br/com/sistema/
│   ├── configurations/          # Configurações (CORS, OpenAPI)
│   ├── controllers/             # Endpoints REST
│   ├── dtos/                    # Data Transfer Objects
│   ├── exceptions/              # Tratamento de exceções
│   ├── models/                  # Entidades JPA
│   ├── repositories/            # Interfaces de acesso a dados
│   ├── services/                # Lógica de negócio
│   └── Startup.java             # Classe principal
│
├── src/main/resources/
│   ├── db/migration/            # Scripts Flyway
│   ├── application.properties   # Configurações principais
│   ├── application-prod.properties
│   └── log4j2-spring.xml        # Configuração de logs

└── pom.xml                      # Dependências Maven
```

---

## Configuração e Instalação

### Pré-requisitos

- **Java 21+** instalado
- **MySQL 8.0+** instalado e rodando
- **Maven 3.6+** instalado
- **Git** para clonar o repositório

### 1) Clone o Repositório
```bash
git clone https://github.com/seu-usuario/backend-nutritional.git
cd backend-nutritional
```

### 2) Configure o Banco de Dados
```sql
-- Conecte ao MySQL
mysql -u root -p

-- Crie o banco de dados
CREATE DATABASE nutricontrol_db;
CREATE USER 'nutricontrol_user'@'localhost' IDENTIFIED BY 'sua_senha';
GRANT ALL PRIVILEGES ON nutricontrol_db.* TO 'nutricontrol_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3) Configure as Variáveis de Ambiente (Produção)

Crie um arquivo `.env` ou configure as variáveis:
```bash
export MYSQLHOST=localhost
export MYSQLPORT=3306
export MYSQLDATABASE=nutricontrol_db
export MYSQLUSER=nutricontrol_user
export MYSQLPASSWORD=sua_senha
```

### 4) Execute as Migrations
```bash
mvn flyway:migrate
```

### 5) Compile e Execute
```bash
# Compilar
mvn clean install

# Executar
mvn spring-boot:run

# Ou executar o JAR
java -jar target/nutritional-0.0.1-SNAPSHOT.jar
```

---

## Endpoints da API

### Pacientes

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/v1/pacientes` | Cadastrar novo paciente |
| `GET`  | `/api/v1/pacientes` | Listar todos os pacientes |
| `GET`  | `/api/v1/pacientes/{id}` | Buscar paciente por ID |
| `GET`  | `/api/v1/pacientes/cpf/{cpf}` | Buscar paciente por CPF |
| `GET`  | `/api/v1/pacientes/buscar?nome=João` | Buscar por nome |
| `PUT`  | `/api/v1/pacientes/{id}` | Atualizar paciente |
| `DELETE`| `/api/v1/pacientes/{id}` | Deletar paciente |

### Consultas

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/v1/consultas/paciente/{pacienteId}` | Criar nova consulta |
| `GET`  | `/api/v1/consultas/paciente/{pacienteId}` | Listar consultas do paciente |
| `GET`  | `/api/v1/consultas/{id}` | Buscar consulta completa |
| `GET`  | `/api/v1/consultas/comparar/{pacienteId}?consultaInicialId=1&consultaFinalId=2` | Comparar duas consultas |
| `PUT`  | `/api/v1/consultas/{id}` | Atualizar dados da consulta |
| `DELETE`| `/api/v1/consultas/{id}` | Deletar consulta |

### Avaliações Físicas

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/v1/avaliacoes/consulta/{consultaId}` | Salvar avaliação física |
| `GET`  | `/api/v1/avaliacoes/consulta/{consultaId}` | Buscar avaliação física |
| `PUT`  | `/api/v1/avaliacoes/consulta/{consultaId}` | Atualizar avaliação física |
| `DELETE`| `/api/v1/avaliacoes/consulta/{consultaId}` | Deletar avaliação física |

### Questionários de Estilo de Vida

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/v1/questionario/consulta/{consultaId}` | Salvar questionário |
| `GET`  | `/api/v1/questionario/consulta/{consultaId}` | Buscar questionário |
| `PUT`  | `/api/v1/questionario/consulta/{consultaId}` | Atualizar questionário |
| `DELETE`| `/api/v1/questionario/consulta/{consultaId}` | Deletar questionário |

### Registro Fotográfico

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/v1/registro-fotografico/consulta/{consultaId}` | Upload de fotos |
| `GET`  | `/api/v1/registro-fotografico/consulta/{consultaId}` | Buscar fotos |
| `PUT`  | `/api/v1/registro-fotografico/consulta/{consultaId}` | Atualizar fotos |
| `DELETE`| `/api/v1/registro-fotografico/consulta/{consultaId}` | Deletar fotos |

---

## Exemplos de Uso

### Cadastrar Paciente
```bash
curl -X POST https://api-sysnutritional.cesaravb.com.br/api/v1/pacientes \
  -H "Content-Type: application/json" \
  -d '{
    "nomeCompleto": "Ana Paula Santos",
    "cpf": "11122233344",
    "dataNascimento": "1995-07-20",
    "telefoneWhatsapp": "21999887766",
    "email": "ana.paula@email.com"
  }'
```

### Criar Consulta
```bash
curl -X POST https://api-sysnutritional.cesaravb.com.br/api/v1/consultas/paciente/1
```

### Salvar Avaliação Física
```bash
curl -X POST https://api-sysnutritional.cesaravb.com.br/api/v1/avaliacoes/consulta/1 \
  -H "Content-Type: application/json" \
  -d '{
    "altura": 1.75,
    "pesoAtual": 80.5,
    "percentualGordura": 18.5,
    "massaMagra": 65.6,
    "massaGorda": 14.9,
    "imc": 26.3
  }'
```

### Comparar Consultas
```bash
curl https://api-sysnutritional.cesaravb.com.br/api/v1/consultas/comparar/1?consultaInicialId=1&consultaFinalId=2
```

---

## Documentação Interativa

### Desenvolvimento (Local)
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Produção
- **Swagger UI**: [https://api-sysnutritional.cesaravb.com.br/swagger-ui.html](https://api-sysnutritional.cesaravb.com.br/swagger-ui.html)
- **OpenAPI JSON**: [https://api-sysnutritional.cesaravb.com.br/api-docs](https://api-sysnutritional.cesaravb.com.br/api-docs)

---

## Modelo de Dados

### Principais Entidades
```
Paciente
├── Consulta (1:N)
    ├── QuestionarioEstiloVida (1:1)
    ├── AvaliacaoFisica (1:1)
    └── RegistroFotografico (1:1)
```

### Tabelas

- `tbl_pacientes` - Dados cadastrais dos pacientes
- `tbl_consultas` - Registro de cada atendimento
- `tbl_questionarios_estilo_vida` - Anamnese subjetiva
- `tbl_avaliacoes_fisicas` - Medidas antropométricas
- `tbl_registros_fotograficos` - Armazenamento de fotos

---

## Segurança

- ✅ Validação de CPF único
- ✅ CORS configurado para origens específicas
- ✅ Tratamento global de exceções
- ✅ Validação de dados com Bean Validation
- ⚠️ **Em desenvolvimento**: Autenticação JWT

---

## Deploy

### Docker (Recomendado)
```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/nutritional-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```
```bash
docker build -t nutritional-api .
docker run -p 8080:8080 nutritional-api
```

### Produção

A API está em produção em: **https://api-sysnutritional.cesaravb.com.br**

Configurações de ambiente em produção:
- Nginx como proxy reverso
- SSL/TLS via Cloudflare
- MySQL 8.0 dedicado
- Logs centralizados

---

## Melhorias Futuras

- [x] Upload de fotos com armazenamento em S3
- [ ] Geração de relatórios em PDF
- [ ] Integração com WhatsApp para envio de dietas
- [ ] Sistema de autenticação e autorização
- [ ] Cache com Redis
- [ ] Testes unitários e de integração completos
- [ ] CI/CD com GitHub Actions

---

## Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

---

## Contato

- **Website**: [cesaravb.com.br](https://cesaravb.com.br)
- **API**: [api-sysnutritional.cesaravb.com.br](https://api-sysnutritional.cesaravb.com.br)

---

<div align="center">
  <sub>Desenvolvido por César Augusto</sub>
</div>
