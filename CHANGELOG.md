## [1.20.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.19.5...v1.20.0) (2026-04-28)

### Features

* adicionar campos de precificação para entrada e saída no modelo de configuração IA ([99d5aa2](https://github.com/CesarAVB/nutritional-monorepo/commit/99d5aa25bcaa594883f2abda2df646e55acdd795))

## [1.19.5](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.19.4...v1.19.5) (2026-04-28)

### Bug Fixes

* normalizar número de telefone antes de enviar mensagem via Evolution API ([e90cf9b](https://github.com/CesarAVB/nutritional-monorepo/commit/e90cf9b81de9d929eff6ee62af8c14e1a1e57261))

## [1.19.4](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.19.3...v1.19.4) (2026-04-28)

### Bug Fixes

* corrigir lógica de intervalo entre consultas para evitar loops infinitos ([5e0e630](https://github.com/CesarAVB/nutritional-monorepo/commit/5e0e63012592d8162763d7978e7c51997cbf6eb9))

## [1.19.3](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.19.2...v1.19.3) (2026-04-28)

### Bug Fixes

* melhorar mensagens de erro e sucesso na conexão com a API Evolution ([725e1f3](https://github.com/CesarAVB/nutritional-monorepo/commit/725e1f32b3187ecf20d4ec58ad75997040be095c))

## [1.19.2](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.19.1...v1.19.2) (2026-04-28)

### Bug Fixes

* corrigir decodificação da chave AES removendo o padding desnecessário ([d6d5c10](https://github.com/CesarAVB/nutritional-monorepo/commit/d6d5c103e0043050e6542f9ed83ea2a43213db3a))

## [1.19.1](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.19.0...v1.19.1) (2026-04-28)

### Bug Fixes

* corrigir decodificação da chave AES para incluir padding adequado ([b5df76f](https://github.com/CesarAVB/nutritional-monorepo/commit/b5df76fc5342c94ea5d152cb135a9044c87c2147))

## [1.19.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.18.1...v1.19.0) (2026-04-28)

### Features

* adicionar gerenciamento de configurações de infraestrutura (Email, RabbitMQ, MinIO/S3) ([d51e1de](https://github.com/CesarAVB/nutritional-monorepo/commit/d51e1deebcb3bb2d1af1a0b7108d02ed48358bab))

## [1.18.1](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.18.0...v1.18.1) (2026-04-27)

### Bug Fixes

* corrigir formatação de horário ao salvar agendamento ([08cfddb](https://github.com/CesarAVB/nutritional-monorepo/commit/08cfddb674b143c88c5f6c3c7e1ad5b00e41a225))

## [1.18.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.17.0...v1.18.0) (2026-04-27)

### Features

* aprimorar calendário semanal com estilo e funcionalidade de eventos ([ec10041](https://github.com/CesarAVB/nutritional-monorepo/commit/ec100411e5b95ba4369ab198702fc1237da60713))

## [1.17.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.16.0...v1.17.0) (2026-04-27)

### Features

* adicionar layout de configurações com sidebar e conteúdo principal ([decd0ed](https://github.com/CesarAVB/nutritional-monorepo/commit/decd0ed28a2000b219291a5526d77f8b8a1dbc22))

## [1.16.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.15.0...v1.16.0) (2026-04-27)

### Features

* adicionar campo de notificação enviada ao DTO de agendamento e lógica de aviso no formulário ([48d9634](https://github.com/CesarAVB/nutritional-monorepo/commit/48d963419c52067f346ac6299f044164f15f8a9d))

## [1.15.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.14.0...v1.15.0) (2026-04-27)

### Features

* renomear colunas de templates de confirmação e lembretes na configuração de agendamento ([d3d9386](https://github.com/CesarAVB/nutritional-monorepo/commit/d3d9386731f72367bcae9eb034b01bf7c6344ebb))

## [1.14.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.13.0...v1.14.0) (2026-04-27)

### Features

* adicionar configurações de agendamento com interface e serviço ([7ddba9f](https://github.com/CesarAVB/nutritional-monorepo/commit/7ddba9f935c5d903e5965b485503284b494a803a))
* adicionar funcionalidades de agendamento com calendário semanal e contagem de consultas ([95bda61](https://github.com/CesarAVB/nutritional-monorepo/commit/95bda617be56906a4169eaab478168c0b56a991c))
* adicionar funcionalidades de agendamento com formulários e listagem por dia ([e22ce64](https://github.com/CesarAVB/nutritional-monorepo/commit/e22ce640676f137461f1af86be1df7efdd889c5d))
* adicionar funcionalidades de agendamento e notificações com criptografia AES ([a8077aa](https://github.com/CesarAVB/nutritional-monorepo/commit/a8077aaa61da4e5a6ea4cffa0f50a22a1cd5858a))
* atualizar placeholders de templates de lembretes e confirmações para suportar interpolação ([812e94a](https://github.com/CesarAVB/nutritional-monorepo/commit/812e94a317761e85f6f39e94fef96b9edd02dd5c))
* implementar serviço de agendamento com CRUD, validação de conflitos e integração com WhatsApp ([f140704](https://github.com/CesarAVB/nutritional-monorepo/commit/f140704b9f46ef73a66ae6e48b0950e9864b1692))
* implementar serviço de notificações de agendamento via WhatsApp e Email, incluindo agendamento de lembretes ([d3cf4fe](https://github.com/CesarAVB/nutritional-monorepo/commit/d3cf4fe896e5705a0e1b3e8fccc55589d280363f))

## [1.13.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.12.0...v1.13.0) (2026-04-27)

### Features

* atualizar a distribuição calórica esperada e simplificar a apresentação das refeições ([c06a888](https://github.com/CesarAVB/nutritional-monorepo/commit/c06a888a4d08563b0bd00ef8b83a94b61e291919))

### Bug Fixes

* arredondar o total para uma casa decimal na função de cálculo ([59c1258](https://github.com/CesarAVB/nutritional-monorepo/commit/59c12581ce3ec5b616015e3f6a61458fc8a8f5d5))

## [1.12.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.11.0...v1.12.0) (2026-04-27)

### Features

* adicionar distribuição calórica esperada e ajustes na apresentação das kcal totais ([bd4c497](https://github.com/CesarAVB/nutritional-monorepo/commit/bd4c497927e942d9207f63eaeb22437710fc2499))

## [1.11.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.10.0...v1.11.0) (2026-04-27)

### Features

* adicionar campo de calorias em AlimentoRefeicao e atualizar interfaces relacionadas ([01026b8](https://github.com/CesarAVB/nutritional-monorepo/commit/01026b828e4a3792cb46eac8547e01a7187fccab))
* adicionar campo de calorias em AlimentoRefeicao e atualizar métodos relacionados ([e918d0d](https://github.com/CesarAVB/nutritional-monorepo/commit/e918d0d5a7952a89957a4b2f400ef45f4f538608))

## [1.10.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.9.0...v1.10.0) (2026-04-26)

### Features

* implementar registro e visualização de uso da IA ([19be718](https://github.com/CesarAVB/nutritional-monorepo/commit/19be71851187fe259fe49018e7ee26f44f4d6632))

## [1.9.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.8.0...v1.9.0) (2026-04-26)

### Features

* adiciona configuração do Flyway para migrações de banco de dados ([301c01a](https://github.com/CesarAVB/nutritional-monorepo/commit/301c01a704f14c1166ca113ba1389cc3431df2d6))

## [1.8.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.7.0...v1.8.0) (2026-04-26)

### Features

* refatorar serviços e controladores para melhorar a legibilidade e a consistência dos métodos ([fe50567](https://github.com/CesarAVB/nutritional-monorepo/commit/fe50567c5f9bacd9cae51839a35a8da23a06e496))

## [1.7.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.6.1...v1.7.0) (2026-04-26)

### Features

* adicionar documentação e comentários detalhados no S3Service.java ([9d962e9](https://github.com/CesarAVB/nutritional-monorepo/commit/9d962e962d2999fcb2da75a39e8fb4acf9c5468f))
* atualiza documentação e estrutura do projeto, cria tabelas de dietas e melhora estilo de formulários ([8eac0ae](https://github.com/CesarAVB/nutritional-monorepo/commit/8eac0aed75025e29034752075b737058a70501b9))

## [1.6.1](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.6.0...v1.6.1) (2026-04-26)

### Bug Fixes

* corrige nome da tabela e ajusta anotações das colunas em TacoAlimento ([d2276a4](https://github.com/CesarAVB/nutritional-monorepo/commit/d2276a478a9bbb2a2909ec0ac757844cbebe6db5))

## [1.6.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.5.0...v1.6.0) (2026-04-26)

### Features

* adiciona campo de título da dieta e ingestão de água no formulário de dietas ([6258e94](https://github.com/CesarAVB/nutritional-monorepo/commit/6258e94b396a1ac37a8e7b221a3115af4c5ab1a0))
* adiciona classes de configuração e requisições para integração com IA ([326f166](https://github.com/CesarAVB/nutritional-monorepo/commit/326f166ac833fb30698f6bbc7f807c5d62520b8f))
* adiciona endpoint para geração de dieta com IA ([4c57e3f](https://github.com/CesarAVB/nutritional-monorepo/commit/4c57e3fa38b7b8df3ca1769d7525f8ef9adbbd90))
* adiciona interfaces e serviço para configuração de IA ([191dc39](https://github.com/CesarAVB/nutritional-monorepo/commit/191dc3932cff81660e9eea4644c84f89926fca24))
* adiciona página de configurações de IA e funcionalidade para geração de dietas com IA ([3f7c36e](https://github.com/CesarAVB/nutritional-monorepo/commit/3f7c36e0995447ac8616b59a8ceb3016ac0d31a4))
* adiciona serviço DietaIAService para geração de dietas com IA ([d2c7239](https://github.com/CesarAVB/nutritional-monorepo/commit/d2c72394615fe22a38880bc9b489807546fa3797))
* adicionar consultas em batch e otimizar carregamento de dados nas listas de pacientes e consultas ([9024791](https://github.com/CesarAVB/nutritional-monorepo/commit/90247919f56468bf1a319c8aa4ebb41df6aa449b))
* adicionar variáveis de design e refatorar estilos na lista de pacientes ([232c4b4](https://github.com/CesarAVB/nutritional-monorepo/commit/232c4b481c7a5577d135f15a9653010c404156c8))
* corrigir nomes de colunas na tabela de dietas e adicionar transações somente leitura nos serviços ([7d9e4eb](https://github.com/CesarAVB/nutritional-monorepo/commit/7d9e4ebf6044c98489fbb606bc674b259b12ab91))
* criar formulário de dieta com funcionalidades de edição e geração de PDF ([c61cd53](https://github.com/CesarAVB/nutritional-monorepo/commit/c61cd534c9555edd011d062a3e36e980718f2191))

## [1.5.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.4.0...v1.5.0) (2026-04-12)

### Features

* atualizar Dockerfile para usar imagem do Playwright e simplificar a instalação do Chromium ([d871cc6](https://github.com/CesarAVB/nutritional-monorepo/commit/d871cc6061f23d6dc40e7539b03e05be4e5517c1))

## [1.4.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.3.0...v1.4.0) (2026-04-12)

### Features

* melhorar a resolução do executável do Chromium e adicionar tratamento de erros no PlaywrightPdfService ([0f3214e](https://github.com/CesarAVB/nutritional-monorepo/commit/0f3214e768408a2ebe3e37c4a1a0f8877d65d503))

## [1.3.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.2.0...v1.3.0) (2026-04-12)

### Features

* adicionar verificação de disponibilidade do Playwright e fallback para geração de PDF ([08c84d0](https://github.com/CesarAVB/nutritional-monorepo/commit/08c84d0cb67a741a7111555e1ee220b80b89a6d6))

## [1.2.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.1.0...v1.2.0) (2026-04-12)

### Features

* otimizar instalação do Chromium no Docker e remover código desnecessário do PlaywrightPdfService ([d71ed7b](https://github.com/CesarAVB/nutritional-monorepo/commit/d71ed7b2db1dec3511fe2bc9f64d9c373c65d825))

## [1.1.0](https://github.com/CesarAVB/nutritional-monorepo/compare/v1.0.0...v1.1.0) (2026-04-12)

### Features

* garantir instalação do Chromium no Playwright para evitar downloads desnecessários ([b4f809f](https://github.com/CesarAVB/nutritional-monorepo/commit/b4f809fe213de4af7a5115ff0c1192930489af42))

## 1.0.0 (2026-04-12)

### Features

* adicionar serviços de dashboard, paciente e toast, além de arquivos de configuração e estilos básicos ([f433974](https://github.com/CesarAVB/nutritional-monorepo/commit/f433974272cff554e8efeb6e6deb716dc9267e15))
* adicionar workflow de release com configuração para semantic-release ([da9bfab](https://github.com/CesarAVB/nutritional-monorepo/commit/da9bfab048734606a507f3a95c077ae6cdca3f8e))
