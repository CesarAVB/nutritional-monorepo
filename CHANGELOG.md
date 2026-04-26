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
