package br.com.sistema.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.dtos.UsoIALogResponse;
import br.com.sistema.dtos.UsoIAResumoResponse;
import br.com.sistema.services.UsoIALogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ia/uso")
@RequiredArgsConstructor
@Tag(name = "Uso de IA", description = "Histórico de chamadas e custos estimados de IA")
public class UsoIALogController {

    private final UsoIALogService usoIALogService;

    @GetMapping
    @Operation(summary = "Listar histórico de uso", description = "Retorna histórico paginado de chamadas à API de IA com tokens e custo estimado")
    public ResponseEntity<Page<UsoIALogResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataHora"));
        return ResponseEntity.ok(usoIALogService.listar(pageable));
    }

    @GetMapping("/resumo")
    @Operation(summary = "Resumo de custos", description = "Retorna totais acumulados de chamadas, tokens e custo estimado em USD")
    public ResponseEntity<UsoIAResumoResponse> resumo() {
        return ResponseEntity.ok(usoIALogService.resumo());
    }
}
