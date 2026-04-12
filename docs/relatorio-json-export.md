# Relatório JSON Export

Descrição
---------
Este documento descreve o método `enviarRelatorioJson(RelatorioRequestDTO request, String destinationUrl)` implementado em `RelatorioService`.

O método monta um JSON contendo todos os dados do relatório (paciente, consulta, avaliação física, questionário de estilo de vida e registro fotográfico), adiciona metadados (data/hora de geração, tipo de template, idade do paciente e data da consulta formatada) e envia o JSON via HTTP POST para a URL informada.

Observações técnicas
--------------------
- O método usa o `ObjectMapper` (configurado em `JacksonConfig`) para serializar o payload. O `JavaTimeModule` já está registrado, portanto `OffsetDateTime` e `LocalDate` são serializados corretamente.
- Os URLs de imagem são processados por `escaparUrlsFotos(...)` antes do envio (mantém as URLs como estão para evitar problemas de parsing).
- O método retorna `HttpResponse<String>` contendo status e corpo da resposta do endpoint remoto.
- Use HTTPS e endpoints autenticados conforme necessário; o método atual não implementa autenticação, retries ou timeouts avançados (podemos adicionar se desejado).

Assinatura do método
--------------------
`public HttpResponse<String> enviarRelatorioJson(RelatorioRequestDTO request, String destinationUrl) throws Exception`

Estrutura (esqueleto) do JSON
----------------------------
```json
{
  "paciente": {
    // Campos do PacienteDTO (ex.: id, nome, dataNascimento, sexo, endereco, contato, etc.)
  },
  "consulta": {
    // Campos do ConsultaDetalhadaDTO (ex.: id, dataConsulta, motivo, profissional, observacoes, etc.)
  },
  "avaliacaoFisica": {
    // Campos de AvaliacaoFisicaDTO (ex.: peso, altura, imc, perimetros, dobras, etc.)
  },
  "questionario": {
    // Campos de QuestionarioEstiloVidaDTO (ex.: atividadeFisica, sono, alimentacao, tabagismo, etc.)
  },
  "registroFotografico": {
    "fotoAnterior": "https://...",
    "fotoPosterior": "https://...",
    "fotoLateralEsquerda": "https://...",
    "fotoLateralDireita": "https://..."
  },
  "dataConsultaFormatada": "dd/MM/yyyy HH:mm",
  "idadePaciente": 34,
  "templateType": "detalhado",
  "generatedAt": "2026-01-30T12:34:56+00:00"
}
```

Exemplo rápido de uso
---------------------
Pseudocódigo Java:

```java
RelatorioRequestDTO request = new RelatorioRequestDTO(...);
String url = "https://meu-endpoint.example.com/relatorios";
HttpResponse<String> resp = relatorioService.enviarRelatorioJson(request, url);
if (resp.statusCode() == 200) {
  // sucesso
}
```

Melhorias sugeridas
-------------------
- Adicionar headers de autenticação (Bearer token, API key) conforme a necessidade do endpoint alvo.
- Implementar retries com backoff e timeouts configuráveis.
- Validar formato da URL antes de enviar e validar o payload com um schema JSON (se o receptor exigir).
- Caso precise que as imagens fiquem embutidas no payload, implementar download das imagens e conversão para Data URIs antes da serialização.

---
Arquivo gerado automaticamente pelo script de mudanças do serviço de relatórios.