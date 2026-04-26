package br.com.sistema.controllers;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.dtos.DietaContextoPacienteDTO;
import br.com.sistema.dtos.DietaRequest;
import br.com.sistema.dtos.DietaResponse;
import br.com.sistema.dtos.DietaResumoResponse;
import br.com.sistema.dtos.ia.GerarDietaIARequest;
import br.com.sistema.services.DietaIAService;
import br.com.sistema.services.DietaService;
import br.com.sistema.services.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador responsavel por gerenciar os planos alimentares dos pacientes.
 * Gerencia a criacao manual e assistida por IA, listagem, detalhamento, atualizacao
 * e remocao de dietas, alem da geracao de relatorios em PDF.
 *
 * <p>O servico de IA retorna um rascunho de dieta para revisao antes da persistencia,
 * permitindo que o profissional ajuste parametros antes de salvar definitivamente.</p>
 */
@RestController
@RequestMapping("/api/v1/dietas")
@RequiredArgsConstructor
@Tag(name = "Dietas", description = "Endpoints para gestao de planos alimentares")
public class DietaController {

    private final DietaService dietaService;
    private final DietaIAService dietaIAService;
    private final RelatorioService relatorioService;

    /**
     * Recupera o contexto nutricional do paciente na ultima consulta,
     * incluindo objetivo e numero de refeicoes diarias recomendadas.
     *
     * @param pacienteId identificador do paciente
     * @return objetivo e numero de refeicoes da consulta mais recente
     */
    @GetMapping("/paciente/{pacienteId}/contexto")
    @Operation(summary = "Buscar contexto do paciente", description = "Retorna objetivo e no de refeicoes da ultima consulta")
    public ResponseEntity<DietaContextoPacienteDTO> buscarContexto(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(dietaService.buscarContextoPaciente(pacienteId));
    }

    /**
     * Lista todos os resumos de dietas ja criados para um paciente,
     * ordenados por data de criacao decrescente.
     *
     * @param pacienteId identificador do paciente
     * @return lista de dietas resumidas do paciente
     */
    @GetMapping("/paciente/{pacienteId}")
    @Operation(summary = "Listar dietas do paciente", description = "Retorna resumo de todas as dietas do paciente")
    public ResponseEntity<List<DietaResumoResponse>> listarPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(dietaService.listarPorPaciente(pacienteId));
    }

    /**
     * Recupera todos os dados de uma dieta especifica, incluindo
     * refeicoes, horarios, grupos alimentares e suplementos.
     *
     * @param id identificador da dieta
     * @return dados completos da dieta com todas as refeicoes
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar dieta completa", description = "Retorna todos os dados da dieta incluindo refeicoes e suplementos")
    public ResponseEntity<DietaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(dietaService.buscarPorId(id));
    }

    /**
     * Cria uma nova dieta manual para o paciente com todas as
     * refeicoes, horarios e recomendacoes nutricional definidas.
     *
     * @param pacienteId identificador do paciente
     * @param request dados completos da dieta incluindo refeicoes e macros
     * @return dieta criada com identificador gerado
     */
    @PostMapping("/paciente/{pacienteId}")
    @Operation(summary = "Criar dieta", description = "Cria nova dieta para o paciente")
    public ResponseEntity<DietaResponse> criar(@PathVariable Long pacienteId, @RequestBody DietaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dietaService.criar(pacienteId, request));
    }

    /**
     * Usa IA para gerar um rascunho de plano alimentar baseado nos
     * dados do paciente. Requer kcalTotal e distribuicao de macros
     * definidos no request. Retorna DietaRequest para revisao antes de salvar.
     *
     * @param pacienteId identificador do paciente
     * @param request parametros de geracao incluindo kcal e macros desejados
     * @return rascunho de dieta gerada pela IA para revisao
     */
    @PostMapping("/paciente/{pacienteId}/gerar-ia")
    @Operation(
        summary = "Gerar dieta com IA",
        description = "Usa IA para montar um plano alimentar baseado nos dados do paciente. Requer kcalTotal e macros definidos. Retorna DietaRequest para revisao antes de salvar."
    )
    public ResponseEntity<DietaRequest> gerarComIA(
            @PathVariable Long pacienteId,
            @Valid @RequestBody GerarDietaIARequest request) {
        return ResponseEntity.ok(dietaIAService.gerarDieta(pacienteId, request));
    }

    /**
     * Atualiza todos os dados de uma dieta existente incluindo
     * refeicoes, horarios e recomendacoes de suplementos.
     *
     * @param id identificador da dieta
     * @param request novos dados da dieta
     * @return dieta atualizada com todas as modificacoes aplicadas
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dieta", description = "Atualiza todos os dados da dieta")
    public ResponseEntity<DietaResponse> atualizar(@PathVariable Long id, @RequestBody DietaRequest request) {
        return ResponseEntity.ok(dietaService.atualizar(id, request));
    }

    /**
     * Remove uma dieta e todos os seus dados relacionados,
     * incluindo refeicoes e associacoes com a consulta.
     *
     * @param id identificador da dieta a ser removida
     * @return resposta vazia com status 204 em caso de sucesso
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar dieta", description = "Remove a dieta e todos os dados relacionados")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        dietaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gera um arquivo PDF com o plano alimentar completo para download
     * ou impressao. O documento contem refeicoes, horarios e instrucoes.
     *
     * @param id identificador da dieta
     * @return arquivo PDF com o plano alimentar
     */
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Gerar PDF da dieta", description = "Gera o plano alimentar em PDF")
    public ResponseEntity<InputStreamResource> gerarPdf(@PathVariable Long id) throws Exception {
        byte[] pdfBytes = relatorioService.gerarPdfDieta(id);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=dieta-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(resource);
    }
}
