package br.com.sistema.services;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import br.com.sistema.dtos.AvaliacaoFisicaDTO;
import br.com.sistema.dtos.ConsultaComparativaItemDTO;
import br.com.sistema.dtos.ConsultaDetalhadaDTO;
import br.com.sistema.dtos.ConsultaResumoDTO;
import br.com.sistema.dtos.DietaResponse;
import br.com.sistema.dtos.PacienteDTO;
import br.com.sistema.dtos.QuestionarioEstiloVidaDTO;
import br.com.sistema.dtos.RegistroFotograficoDTO;
import br.com.sistema.dtos.RelatorioRequestDTO;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;

/**
 * Orquestra a gera��o de relat�rios nutricionais em PDF e JSON.
 * Coordena m�ltiplos servi�os (paciente, consulta, avalia��o, etc.) e utiliza
 * Thymeleaf para templates HTML, com renderiza��o via OpenHTMLtoPDF ou Playwright.
 */
@Service
public class RelatorioService {

    @Autowired
    private DietaService dietaService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private ConsultaService consultaService;

    @Autowired
    private QuestionarioEstiloVidaService questionarioService;

    @Autowired
    private PlaywrightPdfService playwrightPdfService;

    @Autowired
    private RegistroFotograficoService registroFotograficoService;

    @Autowired
    private AvaliacaoFisicaService avaliacaoFisicaService;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private ObjectMapper objectMapper;
    
    private static final Logger log = LoggerFactory.getLogger(RelatorioService.class);	

    /**
     * Gera um PDF de relat�rio nutricional a partir de um request com paciente e consulta.
     * Busca dados de m�ltiplos servi�os, processa com Thymeleaf e converte para PDF.
     * Para templates comparativos, tenta usar Playwright primeiro com fallback para OpenHTMLtoPDF.
     *
     * @param request DTO com pacienteId, consultaId e tipo de template
     * @return Array de bytes do PDF gerado
     * @throws Exception se a gera��o falhar
     */
    public byte[] gerarRelatorioEmPDF(RelatorioRequestDTO request) throws Exception {
        
    	var paciente = pacienteService.buscarPorId(request.getPacienteId());
        var consulta = consultaService.buscarDetalhada(request.getConsultaId());
        var questionario = questionarioService.buscarPorConsulta(request.getConsultaId());
        var registroFotografico = registroFotograficoService.buscarPorConsulta(request.getConsultaId());
        var avaliacaoFisica = avaliacaoFisicaService.buscarPorConsulta(request.getConsultaId());

        escaparUrlsFotos(registroFotografico);
        String dataConsultaFormatada = formatarDataConsulta(consulta);
        Integer idadePaciente = calcularIdadePaciente(paciente);

        Context context = montarContextoThymeleaf(
            paciente, consulta, avaliacaoFisica, 
            questionario, registroFotografico, 
            dataConsultaFormatada, idadePaciente
        );

        String template = selecionarTemplate(request.getTemplateType());
        String html = templateEngine.process(template, context);

        // Para comparativo, tenta Playwright com fallback
        if ("relatorio-comparativo".equals(template)) {
            try {
                return playwrightPdfService.generatePdf(html);
            } catch (Exception ex) {
                log.warn("Playwright indispon�vel para comparativo. Fallback OpenHTMLtoPDF. Motivo: {}", ex.getMessage());
                return gerarPDF(html);
            }
        }
        return gerarPDF(html);
    }

    /**
     * Varia��o do m�todo principal que prioriza Playwright como renderizador.
     * Aplica fallback autom�tico para OpenHTMLtoPDF em caso de falha.
     *
     * @param request DTO com pacienteId, consultaId e tipo de template
     * @return Array de bytes do PDF gerado
     * @throws Exception se a gera��o falhar
     */
    public byte[] gerarRelatorioEmPDFPriorizandoPlaywright(RelatorioRequestDTO request) throws Exception {

        var paciente = pacienteService.buscarPorId(request.getPacienteId());
        var consulta = consultaService.buscarDetalhada(request.getConsultaId());
        var questionario = questionarioService.buscarPorConsulta(request.getConsultaId());
        var registroFotografico = registroFotograficoService.buscarPorConsulta(request.getConsultaId());
        var avaliacaoFisica = avaliacaoFisicaService.buscarPorConsulta(request.getConsultaId());

        escaparUrlsFotos(registroFotografico);
        String dataConsultaFormatada = formatarDataConsulta(consulta);
        Integer idadePaciente = calcularIdadePaciente(paciente);

        Context context = montarContextoThymeleaf(
                paciente, consulta, avaliacaoFisica,
                questionario, registroFotografico,
                dataConsultaFormatada, idadePaciente
        );

        String template = selecionarTemplate(request.getTemplateType());
        String html = templateEngine.process(template, context);

        try {
            return playwrightPdfService.generatePdf(html);
        } catch (Exception ex) {
            log.warn("Falha no Playwright para template={}. Aplicando fallback OpenHTMLtoPDF. Motivo: {}",
                    template, ex.getMessage());
            return gerarPDF(html);
        }
    }

    /**
     * Gera PDF do relat�rio detalhado usando Playwright como renderizador.
     * Template fixo "relatorio-nutricional-detalhado".
     *
     * @param request DTO com pacienteId e consultaId
     * @return Array de bytes do PDF gerado
     * @throws Exception se a gera��o falhar
     */
    public byte[] gerarRelatorioDetalhadoEmPDFViaPlaywright(RelatorioRequestDTO request) throws Exception {

        var paciente = pacienteService.buscarPorId(request.getPacienteId());
        var consulta = consultaService.buscarDetalhada(request.getConsultaId());
        var questionario = questionarioService.buscarPorConsulta(request.getConsultaId());
        var registroFotografico = registroFotograficoService.buscarPorConsulta(request.getConsultaId());
        var avaliacaoFisica = avaliacaoFisicaService.buscarPorConsulta(request.getConsultaId());

        escaparUrlsFotos(registroFotografico);
        String dataConsultaFormatada = formatarDataConsulta(consulta);
        Integer idadePaciente = calcularIdadePaciente(paciente);

        Context context = montarContextoThymeleaf(
                paciente, consulta, avaliacaoFisica,
                questionario, registroFotografico,
                dataConsultaFormatada, idadePaciente);

        String html = templateEngine.process("relatorio-nutricional-detalhado", context);
        try {
            return playwrightPdfService.generatePdf(html);
        } catch (Exception ex) {
            log.warn("Playwright indispon�vel para relat�rio detalhado. Fallback OpenHTMLtoPDF. Motivo: {}", ex.getMessage());
            return gerarPDF(html);
        }
    }

    /**
     * Normaliza URLs de fotos do registro fotogr�fico para uso seguro em templates HTML.
     */
    private void escaparUrlsFotos(RegistroFotograficoDTO registroFotografico) {
        if (registroFotografico != null) {
            if (registroFotografico.getFotoAnterior() != null) {
                registroFotografico.setFotoAnterior(escapeUrl(registroFotografico.getFotoAnterior()));
            }
            if (registroFotografico.getFotoPosterior() != null) {
                registroFotografico.setFotoPosterior(escapeUrl(registroFotografico.getFotoPosterior()));
            }
            if (registroFotografico.getFotoLateralEsquerda() != null) {
                registroFotografico.setFotoLateralEsquerda(escapeUrl(registroFotografico.getFotoLateralEsquerda()));
            }
            if (registroFotografico.getFotoLateralDireita() != null) {
                registroFotografico.setFotoLateralDireita(escapeUrl(registroFotografico.getFotoLateralDireita()));
            }
        }
    }

    /**
     * Formata a data/hora da consulta para exibi��o padr�o brasileira (dd/MM/yyyy HH:mm).
     */
    private String formatarDataConsulta(ConsultaDetalhadaDTO consulta) {
        if (consulta != null && consulta.getDataConsulta() != null) {
            return consulta.getDataConsulta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return "";
    }

    /**
     * Calcula a idade do paciente em anos a partir da data de nascimento.
     */
    private Integer calcularIdadePaciente(PacienteDTO paciente) {
        if (paciente != null && paciente.getDataNascimento() != null) {
            return Period.between(paciente.getDataNascimento(), LocalDate.now()).getYears();
        }
        return null;
    }

    /**
     * Monta o contexto Thymeleaf com todas as vari�veis necess�rias para renderiza��o do template.
     */
    private Context montarContextoThymeleaf(PacienteDTO paciente, ConsultaDetalhadaDTO consulta, 
            AvaliacaoFisicaDTO avaliacaoFisica, QuestionarioEstiloVidaDTO questionario,
            RegistroFotograficoDTO registroFotografico, String dataConsultaFormatada, 
            Integer idadePaciente) {
        Context context = new Context();
        context.setVariable("paciente", paciente);
        context.setVariable("consulta", consulta);
        context.setVariable("avaliacaoFisica", avaliacaoFisica);
        context.setVariable("questionario", questionario);
        context.setVariable("registroFotografico", registroFotografico);
        context.setVariable("dataConsultaFormatada", dataConsultaFormatada);
        context.setVariable("idadePaciente", idadePaciente);
        context.setVariable("logoDataUri", null);
        return context;
    }

    /**
     * Seleciona o template Thymeleaf com base no tipo de relat�rio solicitado.
     */
    private String selecionarTemplate(String templateType) {
        return switch (templateType) {
            case "simples"      -> "relatorio-nutricional-simples";
            case "detalhado"    -> "relatorio-nutricional-detalhado";
            case "comparativo"  -> "relatorio-comparativo";
            case "teste"        -> "teste-atributos";
            default             -> "relatorio-nutricional-padrao";
        };
    }

    /**
     * Gera um PDF comparativo com o hist�rico evolutivo do paciente.
     * Utiliza Playwright como renderizador principal para melhor qualidade visual.
     *
     * @param pacienteId ID do paciente
     * @return Array de bytes do PDF comparativo
     * @throws Exception se a gera��o falhar
     */
    public byte[] gerarRelatorioComparativoEmPDF(Long pacienteId) throws Exception {
        log.info("### INICIANDO RELAT�RIO COMPARATIVO para pacienteId={}", pacienteId);

        // Buscar paciente
        var paciente = pacienteService.buscarPorId(pacienteId);
        Integer idadePaciente = calcularIdadePaciente(paciente);

        // Listar todas as consultas (desc) e inverter para ordem crescente
        var consultasDesc = consultaService.listarConsultasPorPaciente(pacienteId);
        List<ConsultaResumoDTO> consultasAsc = new ArrayList<>(consultasDesc);
        Collections.reverse(consultasAsc);

        // Agrupar avalia��es f�sicas por consulta
        List<ConsultaComparativaItemDTO> itens = new ArrayList<>();
        for (ConsultaResumoDTO consulta : consultasAsc) {
            try {
                var avaliacao = avaliacaoFisicaService.buscarPorConsulta(consulta.getId());
                if (avaliacao != null) {
                    ConsultaComparativaItemDTO item = new ConsultaComparativaItemDTO();
                    item.setConsultaId(consulta.getId());
                    item.setDataConsulta(consulta.getDataConsulta());
                    item.setDataFormatada(consulta.getDataConsulta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    item.setPeso(avaliacao.getPesoAtual());
                    item.setImc(avaliacao.getImc());
                    item.setPercentualGordura(avaliacao.getPercentualGordura());
                    item.setMassaMagra(avaliacao.getMassaMagra());
                    item.setMassaGorda(avaliacao.getMassaGorda());
                    item.setPerimetroCintura(avaliacao.getPerimetroCintura());
                    item.setPerimetroAbdominal(avaliacao.getPerimetroAbdominal());
                    item.setPerimetroQuadril(avaliacao.getPerimetroQuadril());
                    item.setPerimetroBracoDireitoRelax(avaliacao.getPerimetroBracoDireitoRelax());
                    item.setPerimetroPanturrilhaDireita(avaliacao.getPerimetroPanturrilhaDireita());
                    itens.add(item);
                }
            } catch (Exception e) {
                log.warn("Avalia��o f�sica n�o encontrada para consultaId={}: {}", consulta.getId(), e.getMessage());
            }
        }

        // Preparar varia��es evolutivas
        List<Map<String, Object>> variacoes = new ArrayList<>();
        if (itens.size() >= 2) {
            var primeira = itens.get(0);
            var ultima = itens.get(itens.size() - 1);

            adicionarVariacaoSeMudou(variacoes, "Peso",
                    ultima.getPeso(),
                    primeira.getPeso(),
                    "kg",
                    ultima.getPeso() != null && primeira.getPeso() != null && ultima.getPeso() < primeira.getPeso());

            adicionarVariacaoSeMudou(variacoes, "Gordura Corporal",
                    ultima.getPercentualGordura(),
                    primeira.getPercentualGordura(),
                    "%",
                    ultima.getPercentualGordura() != null && primeira.getPercentualGordura() != null && ultima.getPercentualGordura() < primeira.getPercentualGordura());

            adicionarVariacaoSeMudou(variacoes, "Massa Muscular",
                    ultima.getMassaMagra(),
                    primeira.getMassaMagra(),
                    "kg",
                    ultima.getMassaMagra() != null && primeira.getMassaMagra() != null && ultima.getMassaMagra() > primeira.getMassaMagra());

            adicionarVariacaoSeMudou(variacoes, "IMC",
                    ultima.getImc(),
                    primeira.getImc(),
                    "kg/m�",
                    ultima.getImc() != null && primeira.getImc() != null && ultima.getImc() < primeira.getImc());
        }

        // Preparar fotos progressivas
        List<RegistroFotograficoDTO> fotos = new ArrayList<>();
        for (ConsultaResumoDTO consulta : consultasAsc) {
            try {
                var registro = registroFotograficoService.buscarPorConsulta(consulta.getId());
                if (registro != null) {
                    RegistroFotograficoDTO foto = new RegistroFotograficoDTO();
                    foto.setConsultaId(consulta.getId());
                    foto.setDataFormatada(consulta.getDataConsulta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    foto.setFotoAnterior(registro.getFotoAnterior());
                    foto.setFotoPosterior(registro.getFotoPosterior());
                    foto.setFotoLateralEsquerda(registro.getFotoLateralEsquerda());
                    foto.setFotoLateralDireita(registro.getFotoLateralDireita());
                    fotos.add(foto);
                }
            } catch (Exception e) {
                log.warn("Registro fotogr�fico n�o encontrado para consultaId={}: {}", consulta.getId(), e.getMessage());
            }
        }

        // Montar contexto
        Map<String, Object> contextMap = new LinkedHashMap<>();
        contextMap.put("paciente", paciente);
        contextMap.put("idadePaciente", idadePaciente);
        contextMap.put("itens", itens);
        contextMap.put("variacoes", variacoes);
        contextMap.put("fotos", fotos);
        contextMap.put("logoDataUri", null);

        Context thymeleafContext = new Context();
        for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
            thymeleafContext.setVariable(entry.getKey(), entry.getValue());
        }

        String html = templateEngine.process("relatorio-comparativo", thymeleafContext);

        // Playwright com fallback
        try {
            return playwrightPdfService.generatePdf(html);
        } catch (Exception ex) {
            log.warn("Playwright indispon�vel para relat�rio comparativo. Fallback OpenHTMLtoPDF. Motivo: {}", ex.getMessage());
            return gerarPDF(html);
        }
    }

    /**
     * Adiciona uma varia��o de m�trica � lista se os valores inicial e final forem diferentes.
     */
    private void adicionarVariacaoSeMudou(List<Map<String, Object>> variacoes,
                                   String metrica,
                                   Double valorFinal,
                                   Double valorInicial,
                                   String unidade,
                                   boolean melhora) {
        if (valorFinal == null || valorInicial == null) return;
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("metrica", metrica);
        item.put("delta", valorFinal - valorInicial);
        item.put("unidade", unidade);
        item.put("melhora", melhora);
        variacoes.add(item);
    }

    /**
     * Gera o PDF do plano alimentar (dieta) a partir do ID da dieta.
     * Usa Thymeleaf com template "dieta-pdf" e OpenHTMLtoPDF como renderizador.
     *
     * @param dietaId ID da dieta
     * @return Array de bytes do PDF do plano alimentar
     * @throws Exception se a gera��o falhar
     */
    public byte[] gerarPdfDieta(Long dietaId) throws Exception {
        DietaResponse dieta = dietaService.buscarPorId(dietaId);
        PacienteDTO paciente = pacienteService.buscarPorId(dieta.getPacienteId());

        Context context = new Context();
        context.setVariable("dieta", dieta);
        context.setVariable("paciente", paciente);
        context.setVariable("dataCriacao", dieta.getDataCriacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        String html = templateEngine.process("dieta-pdf", context);
        log.info("### PDF DIETA gerado para dietaId={}", dietaId);
        return gerarPDF(html);
    }

    /**
     * Converte HTML em PDF usando OpenHTMLtoPDF (PdfRendererBuilder).
     * Usado como renderizador principal ou fallback quando Playwright n�o est� dispon�vel.
     */
    private byte[] gerarPDF(String html) throws Exception {
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(pdfStream);
        builder.run();
        return pdfStream.toByteArray();
    }

    /**
     * Escapa URLs para uso seguro em contexto HTML/Thymeleaf.
     * Atualmente mant�m URLs intactas, deixando que Thymeleaf cuide da seguran�a.
     */
    private String escapeUrl(String url) {
        if (url == null) return null;
        return url;
    }

    /**
     * Serializa os dados do relat�rio em JSON e envia via HTTP POST para um endpoint externo.
     * �til para integra��o com automa��es (ex: n8n) que processam o payload.
     *
     * @param request        DTO com dados do relat�rio
     * @param destinationUrl URL do endpoint destino
     * @return HttpResponse com a resposta do servidor destino
     * @throws Exception se o envio falhar
     */
    public HttpResponse<String> enviarRelatorioJson(RelatorioRequestDTO request, String destinationUrl) throws Exception {
        // Buscar dados de todos os servi�os relacionados
        var paciente = pacienteService.buscarPorId(request.getPacienteId());
        var consulta = consultaService.buscarDetalhada(request.getConsultaId());
        var questionario = questionarioService.buscarPorConsulta(request.getConsultaId());
        var registroFotografico = registroFotograficoService.buscarPorConsulta(request.getConsultaId());
        var avaliacaoFisica = avaliacaoFisicaService.buscarPorConsulta(request.getConsultaId());

        // Processar dados
        escaparUrlsFotos(registroFotografico);
        String dataConsultaFormatada = formatarDataConsulta(consulta);
        Integer idadePaciente = calcularIdadePaciente(paciente);

        // Montar payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("paciente", paciente);
        payload.put("consulta", consulta);
        payload.put("avaliacaoFisica", avaliacaoFisica);
        payload.put("questionario", questionario);
        payload.put("registroFotografico", registroFotografico);
        payload.put("dataConsultaFormatada", dataConsultaFormatada);
        payload.put("idadePaciente", idadePaciente);
        payload.put("templateType", request.getTemplateType());
        payload.put("generatedAt", OffsetDateTime.now());

        // Serializar e enviar
        String json = objectMapper.writeValueAsString(payload);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(destinationUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        log.info("Resposta do endpoint: status={} body={}", response.statusCode(), response.body());
        return response;
    }
}

