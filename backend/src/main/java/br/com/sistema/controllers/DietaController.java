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

@RestController
@RequestMapping("/api/v1/dietas")
@RequiredArgsConstructor
@Tag(name = "Dietas", description = "Endpoints para gestão de planos alimentares")
public class DietaController {

    private final DietaService dietaService;
    private final DietaIAService dietaIAService;
    private final RelatorioService relatorioService;

    @GetMapping("/paciente/{pacienteId}/contexto")
    @Operation(summary = "Buscar contexto do paciente", description = "Retorna objetivo e nº de refeições da última consulta")
    public ResponseEntity<DietaContextoPacienteDTO> buscarContexto(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(dietaService.buscarContextoPaciente(pacienteId));
    }

    @GetMapping("/paciente/{pacienteId}")
    @Operation(summary = "Listar dietas do paciente", description = "Retorna resumo de todas as dietas do paciente")
    public ResponseEntity<List<DietaResumoResponse>> listarPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(dietaService.listarPorPaciente(pacienteId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar dieta completa", description = "Retorna todos os dados da dieta incluindo refeições e suplementos")
    public ResponseEntity<DietaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(dietaService.buscarPorId(id));
    }

    @PostMapping("/paciente/{pacienteId}")
    @Operation(summary = "Criar dieta", description = "Cria nova dieta para o paciente")
    public ResponseEntity<DietaResponse> criar(@PathVariable Long pacienteId, @RequestBody DietaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dietaService.criar(pacienteId, request));
    }

    @PostMapping("/paciente/{pacienteId}/gerar-ia")
    @Operation(
        summary = "Gerar dieta com IA",
        description = "Usa IA para montar um plano alimentar baseado nos dados do paciente. Requer kcalTotal e macros definidos. Retorna DietaRequest para revisão antes de salvar."
    )
    public ResponseEntity<DietaRequest> gerarComIA(
            @PathVariable Long pacienteId,
            @Valid @RequestBody GerarDietaIARequest request) {
        return ResponseEntity.ok(dietaIAService.gerarDieta(pacienteId, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dieta", description = "Atualiza todos os dados da dieta")
    public ResponseEntity<DietaResponse> atualizar(@PathVariable Long id, @RequestBody DietaRequest request) {
        return ResponseEntity.ok(dietaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar dieta", description = "Remove a dieta e todos os dados relacionados")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        dietaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

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
