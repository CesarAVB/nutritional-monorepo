package br.com.sistema.controllers;

import java.io.ByteArrayInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.dtos.RelatorioRequestDTO;
import br.com.sistema.services.RelatorioService;

/**
 * Controlador responsavel por orquestrar a geracao de relatorios nutricionais em PDF.
 * Coordena busca de dados do paciente, montagem de contexto e conversao para PDF,
 * alem de integrar com webhook n8n para automacao de envio.
 *
 * <p>Prioriza Playwright para relatorios comparativos (suporta CSS moderno e graficos),
 * enquanto relatorios individuais usam OpenHTMLtoPDF para otimizar performance.</p>
 */
@RestController
@RequestMapping("/api/v1/relatorio")
public class RelatorioController {

    private static final Logger log = LoggerFactory.getLogger(RelatorioController.class);
    
    @Autowired
    private RelatorioService relatorioService;

    /**
     * Gera relatorio nutricional individual em PDF.
     * Busca todos os dados da consulta (paciente, avaliacao, questionario, fotos)
     * e renderiza via template Thymeleaf com fallback para Playwright.
     *
     * @param request Dados da consulta (pacienteId, consultaId, templateType)
     * @return PDF do relatorio nutricional
     * @throws Exception Se a geracao falhar
     */
    @PostMapping(value = "", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> gerarRelatorio(@RequestBody RelatorioRequestDTO request) throws Exception {
        log.info("Iniciando geracao de relatorio para o paciente ID: {}", request.getPacienteId());
        byte[] pdfBytes = relatorioService.gerarRelatorioEmPDFPriorizandoPlaywright(request);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=relatorio-nutricional.pdf");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(resource);
    }

    /**
     * Envia dados do relatorio para webhook n8n como JSON.
     * Usado para automacao de envio de relatorios via plataforma n8n.
     *
     * @param request Dados da consulta para serializar
     * @return Resposta do webhook (status e body)
     * @throws Exception Se o envio falhar
     */
    @PostMapping(value = "/testeComN8n", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> testeComN8n(@RequestBody RelatorioRequestDTO request) throws Exception {
        log.info("Invocando envio de relatorio via n8n webhook para o paciente ID: {}", request.getPacienteId());
        String webhookUrl = "https://n8nwebhook.redelognet.com.br/webhook/springboot/nutrition-help";
        var response = relatorioService.enviarRelatorioJson(request, webhookUrl);
        return ResponseEntity.status(response.statusCode()).body(response.body());
    }

    /**
     * Gera relatorio comparativo evolutivo com todas as consultas do paciente.
     * Inclui graficos de evolucao de peso, medidas e indicadores ao longo do tempo.
     * Usa Playwright para renderizar Chart.js e CSS moderno.
     *
     * @param pacienteId ID do paciente
     * @return PDF do relatorio comparativo
     * @throws Exception Se a geracao falhar
     */
    @PostMapping(value = "/comparativo/{pacienteId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> gerarRelatorioComparativo(
            @PathVariable Long pacienteId) throws Exception {
        log.info("Gerando relatorio comparativo para pacienteId={}", pacienteId);
        byte[] pdfBytes = relatorioService.gerarRelatorioComparativoEmPDF(pacienteId);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=relatorio-comparativo-nutricional.pdf");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(resource);
    }

    /**
     * Handler explicito para metodos GET na raiz do recurso.
     * Retorna 405 Method Not Allowed para orientar o cliente a usar POST.
     *
     * @return Erro 405 com header Allow: POST
     */
    @GetMapping("")
    public ResponseEntity<String> handleGet() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Allow", "POST");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers)
                .body("Metodo nao permitido. Use POST para gerar o relatorio.");
    }
}
