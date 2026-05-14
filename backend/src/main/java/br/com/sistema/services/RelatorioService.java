package br.com.sistema.services;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

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
 * Orquestra a geraï¿½ï¿½o de relatï¿½rios nutricionais em PDF e JSON.
 * Coordena mï¿½ltiplos serviï¿½os (paciente, consulta, avaliaï¿½ï¿½o, etc.) e utiliza
 * Thymeleaf para templates HTML, com renderizaï¿½ï¿½o via OpenHTMLtoPDF ou Playwright.
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
     * Gera um PDF de relatï¿½rio nutricional a partir de um request com paciente e consulta.
     * Busca dados de mï¿½ltiplos serviï¿½os, processa com Thymeleaf e converte para PDF.
     * Para templates comparativos, tenta usar Playwright primeiro com fallback para OpenHTMLtoPDF.
     *
     * @param request DTO com pacienteId, consultaId e tipo de template
     * @return Array de bytes do PDF gerado
     * @throws Exception se a geraï¿½ï¿½o falhar
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
                log.warn("Playwright indisponï¿½vel para comparativo. Fallback OpenHTMLtoPDF. Motivo: {}", ex.getMessage());
                return gerarPDF(html);
            }
        }
        return gerarPDF(html);
    }

    /**
     * Variaï¿½ï¿½o do mï¿½todo principal que prioriza Playwright como renderizador.
     * Aplica fallback automï¿½tico para OpenHTMLtoPDF em caso de falha.
     *
     * @param request DTO com pacienteId, consultaId e tipo de template
     * @return Array de bytes do PDF gerado
     * @throws Exception se a geraï¿½ï¿½o falhar
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
     * Gera PDF do relatï¿½rio detalhado usando Playwright como renderizador.
     * Template fixo "relatorio-nutricional-detalhado".
     *
     * @param request DTO com pacienteId e consultaId
     * @return Array de bytes do PDF gerado
     * @throws Exception se a geraï¿½ï¿½o falhar
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
            log.warn("Playwright indisponï¿½vel para relatï¿½rio detalhado. Fallback OpenHTMLtoPDF. Motivo: {}", ex.getMessage());
            return gerarPDF(html);
        }
    }

    /**
     * Normaliza URLs de fotos do registro fotogrï¿½fico para uso seguro em templates HTML.
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
     * Formata a data/hora da consulta para exibiï¿½ï¿½o padrï¿½o brasileira (dd/MM/yyyy HH:mm).
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
     * Monta o contexto Thymeleaf com todas as variï¿½veis necessï¿½rias para renderizaï¿½ï¿½o do template.
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
     * Seleciona o template Thymeleaf com base no tipo de relatï¿½rio solicitado.
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
     * Gera um PDF comparativo com o histï¿½rico evolutivo do paciente.
     * Utiliza Playwright como renderizador principal para melhor qualidade visual.
     *
     * @param pacienteId ID do paciente
     * @return Array de bytes do PDF comparativo
     * @throws Exception se a geraï¿½ï¿½o falhar
     */
    public byte[] gerarRelatorioComparativoEmPDF(Long pacienteId) throws Exception {
        log.info("### INICIANDO RELATï¿½RIO COMPARATIVO para pacienteId={}", pacienteId);

        var paciente = pacienteService.buscarPorId(pacienteId);
        Integer idadePaciente = calcularIdadePaciente(paciente);

        List<ConsultaResumoDTO> consultasResumo = new ArrayList<>(consultaService.listarConsultasPorPaciente(pacienteId));
        consultasResumo.sort(Comparator.comparing(ConsultaResumoDTO::getId, Comparator.nullsLast(Comparator.naturalOrder())));

        List<ConsultaComparativaItemDTO> consultasComAvaliacao = new ArrayList<>();
        int numeroConsulta = 1;
        for (ConsultaResumoDTO consulta : consultasResumo) {
            try {
                var avaliacao = avaliacaoFisicaService.buscarPorConsulta(consulta.getId());
                if (avaliacao != null) {
                    ConsultaComparativaItemDTO item = new ConsultaComparativaItemDTO();
                    item.setConsultaId(consulta.getId());
                    item.setDataConsulta(consulta.getDataConsulta());
                    item.setDataFormatada(consulta.getDataConsulta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    item.setDataAbreviada(consulta.getDataConsulta().format(DateTimeFormatter.ofPattern("MMM/yy", new Locale("pt", "BR"))).toUpperCase(new Locale("pt", "BR")));
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
                    item.setObjetivo(consulta.getObjetivo());
                    item.setNumeroConsulta(numeroConsulta);
                    consultasComAvaliacao.add(item);
                }
                numeroConsulta++;
            } catch (Exception e) {
                log.warn("Avaliaï¿½ï¿½o fï¿½sica nï¿½o encontrada para consultaId={}: {}", consulta.getId(), e.getMessage());
            }
        }

        List<ConsultaComparativaItemDTO> consultasComparativas = new ArrayList<>();
        if (consultasComAvaliacao.size() == 1) {
            consultasComparativas.add(consultasComAvaliacao.get(0));
        } else if (consultasComAvaliacao.size() >= 2) {
            consultasComparativas.add(consultasComAvaliacao.get(0));
            consultasComparativas.add(consultasComAvaliacao.get(consultasComAvaliacao.size() - 1));
        }
        aplicarRotulosComparativos(consultasComparativas);

        for (ConsultaComparativaItemDTO consulta : consultasComparativas) {
            try {
                var registro = registroFotograficoService.buscarPorConsulta(consulta.getConsultaId());
                if (registro != null) {
                    escaparUrlsFotos(registro);
                    consulta.setFotoAnterior(registro.getFotoAnterior());
                    consulta.setFotoPosterior(registro.getFotoPosterior());
                    consulta.setFotoLateralEsquerda(registro.getFotoLateralEsquerda());
                    consulta.setFotoLateralDireita(registro.getFotoLateralDireita());
                }
            } catch (Exception e) {
                log.warn("Registro fotogrï¿½fico nï¿½o encontrado para consultaId={}: {}", consulta.getConsultaId(), e.getMessage());
            }
        }

        List<ConsultaComparativaItemDTO> consultasComFoto = new ArrayList<>();
        for (ConsultaComparativaItemDTO consulta : consultasComparativas) {
            boolean temFoto = consulta.getFotoAnterior() != null
                    || consulta.getFotoPosterior() != null
                    || consulta.getFotoLateralEsquerda() != null
                    || consulta.getFotoLateralDireita() != null;
            if (temFoto) {
                consultasComFoto.add(consulta);
            }
        }
        boolean hasComparacaoFotos = !consultasComFoto.isEmpty();

        Map<String, Object> evolucao = new LinkedHashMap<>();
        Map<String, Object> resumoExecutivo = new LinkedHashMap<>();
        List<Map<String, Object>> topVariacoes = new ArrayList<>();

        if (consultasComparativas.size() >= 2) {
            var primeira = consultasComparativas.get(0);
            var ultima = consultasComparativas.get(1);

            evolucao.put("primeiraData", primeira.getDataFormatada());
            evolucao.put("ultimaData", ultima.getDataFormatada());
            evolucao.put("melhoriaPeso", isMelhora(ultima.getPeso(), primeira.getPeso(), false));
            evolucao.put("melhoriaImc", isMelhora(ultima.getImc(), primeira.getImc(), false));
            evolucao.put("melhoriaGordura", isMelhora(ultima.getPercentualGordura(), primeira.getPercentualGordura(), false));
            evolucao.put("melhoriaMagra", isMelhora(ultima.getMassaMagra(), primeira.getMassaMagra(), true));
            evolucao.put("diffPeso", formatDelta(ultima.getPeso(), primeira.getPeso(), "kg"));
            evolucao.put("diffImc", formatDelta(ultima.getImc(), primeira.getImc(), ""));
            evolucao.put("diffGordura", formatDelta(ultima.getPercentualGordura(), primeira.getPercentualGordura(), "%"));
            evolucao.put("diffMassaMagra", formatDelta(ultima.getMassaMagra(), primeira.getMassaMagra(), "kg"));
            evolucao.put("diffCintura", formatDelta(ultima.getPerimetroCintura(), primeira.getPerimetroCintura(), "cm"));
            evolucao.put("diffAbdominal", formatDelta(ultima.getPerimetroAbdominal(), primeira.getPerimetroAbdominal(), "cm"));
            evolucao.put("diffQuadril", formatDelta(ultima.getPerimetroQuadril(), primeira.getPerimetroQuadril(), "cm"));

            adicionarVariacaoSeMudou(topVariacoes, "Peso", ultima.getPeso(), primeira.getPeso(), "kg", isMelhora(ultima.getPeso(), primeira.getPeso(), false));
            adicionarVariacaoSeMudou(topVariacoes, "IMC", ultima.getImc(), primeira.getImc(), "kg/m²", isMelhora(ultima.getImc(), primeira.getImc(), false));
            adicionarVariacaoSeMudou(topVariacoes, "Gordura Corporal", ultima.getPercentualGordura(), primeira.getPercentualGordura(), "%", isMelhora(ultima.getPercentualGordura(), primeira.getPercentualGordura(), false));
            adicionarVariacaoSeMudou(topVariacoes, "Massa Magra", ultima.getMassaMagra(), primeira.getMassaMagra(), "kg", isMelhora(ultima.getMassaMagra(), primeira.getMassaMagra(), true));
            topVariacoes.sort((a, b) -> Double.compare(Math.abs((Double) b.get("delta")), Math.abs((Double) a.get("delta"))));
            if (topVariacoes.size() > 3) {
                topVariacoes = new ArrayList<>(topVariacoes.subList(0, 3));
            }
        }

        Map<String, Object> reportData = new LinkedHashMap<>();
        reportData.put("labels", consultasComparativas.stream().map(ConsultaComparativaItemDTO::getDataAbreviada).toList());
        reportData.put("peso", consultasComparativas.stream().map(ConsultaComparativaItemDTO::getPeso).toList());
        reportData.put("imc", consultasComparativas.stream().map(ConsultaComparativaItemDTO::getImc).toList());
        reportData.put("percentualGordura", consultasComparativas.stream().map(ConsultaComparativaItemDTO::getPercentualGordura).toList());
        reportData.put("massaMagra", consultasComparativas.stream().map(ConsultaComparativaItemDTO::getMassaMagra).toList());
        reportData.put("massaGorda", consultasComparativas.stream().map(ConsultaComparativaItemDTO::getMassaGorda).toList());
        reportData.put("cintura", consultasComparativas.stream().map(ConsultaComparativaItemDTO::getPerimetroCintura).toList());
        reportData.put("abdominal", consultasComparativas.stream().map(ConsultaComparativaItemDTO::getPerimetroAbdominal).toList());
        reportData.put("quadril", consultasComparativas.stream().map(ConsultaComparativaItemDTO::getPerimetroQuadril).toList());

        int totalConsultas = consultasComparativas.size();
        resumoExecutivo.put("periodo", totalConsultas >= 2 ? consultasComparativas.get(0).getDataFormatada() + " a " + consultasComparativas.get(totalConsultas - 1).getDataFormatada() : "—");
        resumoExecutivo.put("totalConsultas", totalConsultas);
        resumoExecutivo.put("consultasComFoto", consultasComFoto.size());
        resumoExecutivo.put("diasAcompanhamento", totalConsultas >= 2
                ? java.time.temporal.ChronoUnit.DAYS.between(consultasComparativas.get(0).getDataConsulta().toLocalDate(), consultasComparativas.get(totalConsultas - 1).getDataConsulta().toLocalDate())
                : 0);

        Context thymeleafContext = new Context();
        thymeleafContext.setVariable("paciente", paciente);
        thymeleafContext.setVariable("idadePaciente", idadePaciente);
        thymeleafContext.setVariable("totalConsultas", totalConsultas);
        thymeleafContext.setVariable("consultasComparativas", consultasComparativas);
        thymeleafContext.setVariable("consultasComFoto", consultasComFoto);
        thymeleafContext.setVariable("hasComparacaoFotos", hasComparacaoFotos);
        thymeleafContext.setVariable("evolucao", evolucao);
        thymeleafContext.setVariable("resumoExecutivo", resumoExecutivo);
        thymeleafContext.setVariable("topVariacoes", topVariacoes);
        thymeleafContext.setVariable("reportData", reportData);
        thymeleafContext.setVariable("logoDataUri", null);

        String html = templateEngine.process("relatorio-comparativo", thymeleafContext);

        try {
            return playwrightPdfService.generatePdf(html);
        } catch (Exception ex) {
            log.warn("Playwright indisponï¿½vel para relatï¿½rio comparativo. Fallback OpenHTMLtoPDF. Motivo: {}", ex.getMessage());
            return gerarPDF(html);
        }
    }

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

    private void aplicarRotulosComparativos(List<ConsultaComparativaItemDTO> consultasComparativas) {
        if (consultasComparativas.isEmpty()) {
            return;
        }

        consultasComparativas.get(0).setDataAbreviada("Inicial");
        if (consultasComparativas.size() > 1) {
            consultasComparativas.get(consultasComparativas.size() - 1).setDataAbreviada("Ultima");
        }
    }

    private String formatDelta(Double valorFinal, Double valorInicial, String unidade) {
        if (valorFinal == null || valorInicial == null) return "—";
        double delta = valorFinal - valorInicial;
        String sinal = delta > 0 ? "+" : "";
        String sufixo = (unidade == null || unidade.isBlank()) ? "" : " " + unidade;
        return String.format(Locale.US, "%s%.2f%s", sinal, delta, sufixo);
    }

    private boolean isMelhora(Double valorFinal, Double valorInicial, boolean maiorEhMelhor) {
        if (valorFinal == null || valorInicial == null) return false;
        return maiorEhMelhor ? valorFinal > valorInicial : valorFinal < valorInicial;
    }

    /**
     * Gera o PDF do plano alimentar (dieta) a partir do ID da dieta.
     * Usa Thymeleaf com template "dieta-pdf" e OpenHTMLtoPDF como renderizador.
     *
     * @param dietaId ID da dieta
     * @return Array de bytes do PDF do plano alimentar
     * @throws Exception se a geraï¿½ï¿½o falhar
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
     * Usado como renderizador principal ou fallback quando Playwright nï¿½o estï¿½ disponï¿½vel.
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
     * Atualmente mantï¿½m URLs intactas, deixando que Thymeleaf cuide da seguranï¿½a.
     */
    private String escapeUrl(String url) {
        if (url == null) return null;
        return url;
    }

    /**
     * Serializa os dados do relatï¿½rio em JSON e envia via HTTP POST para um endpoint externo.
     * ï¿½til para integraï¿½ï¿½o com automaï¿½ï¿½es (ex: n8n) que processam o payload.
     *
     * @param request        DTO com dados do relatï¿½rio
     * @param destinationUrl URL do endpoint destino
     * @return HttpResponse com a resposta do servidor destino
     * @throws Exception se o envio falhar
     */
    public HttpResponse<String> enviarRelatorioJson(RelatorioRequestDTO request, String destinationUrl) throws Exception {
        // Buscar dados de todos os serviï¿½os relacionados
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

