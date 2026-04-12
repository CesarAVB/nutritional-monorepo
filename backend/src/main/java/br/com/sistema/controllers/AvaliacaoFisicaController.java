package br.com.sistema.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.dtos.AvaliacaoFisicaDTO;
import br.com.sistema.services.AvaliacaoFisicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/avaliacoes")
@RequiredArgsConstructor
@Tag(name = "Avaliações Físicas", description = "Endpoints para gestão de avaliações físicas")
public class AvaliacaoFisicaController {
    
    private final AvaliacaoFisicaService avaliacaoFisicaService;
    
    // ## Criar nova avaliação física para uma consulta ##
    @PostMapping("/consulta/{consultaId}")
    @Operation(summary = "Salvar avaliação física", description = "Cria uma nova avaliação física para a consulta")
    public ResponseEntity<AvaliacaoFisicaDTO> salvar(@PathVariable Long consultaId, @Valid @RequestBody AvaliacaoFisicaDTO dto) {
        System.out.println("Recebido salvar Avaliacao DTO: " + dto);
        AvaliacaoFisicaDTO saved = avaliacaoFisicaService.salvarAvaliacao(consultaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    
    // ## Buscar avaliação física por consulta ##
    @GetMapping("/consulta/{consultaId}")
    @Operation(summary = "Buscar avaliação física", description = "Busca a avaliação física de uma consulta")
    public ResponseEntity<AvaliacaoFisicaDTO> buscar(@PathVariable Long consultaId) {
        AvaliacaoFisicaDTO avaliacao = avaliacaoFisicaService.buscarPorConsulta(consultaId);
        return ResponseEntity.ok(avaliacao);
    }
    
    // ## Atualizar avaliação física ##
    @PutMapping("/consulta/{consultaId}")
    @Operation(summary = "Atualizar avaliação física", description = "Atualiza a avaliação física de uma consulta")
    public ResponseEntity<AvaliacaoFisicaDTO> atualizar(@PathVariable Long consultaId, @Valid @RequestBody AvaliacaoFisicaDTO dados) {
    	System.out.println("Atualizando avaliação física para consulta ID: " + consultaId + "; DTO recebido: " + dados);
        AvaliacaoFisicaDTO updated = avaliacaoFisicaService.atualizarAvaliacao(consultaId, dados);
        return ResponseEntity.ok(updated);
    }
    
    // ## Deletar avaliação física ##
    @DeleteMapping("/consulta/{consultaId}")
    @Operation(summary = "Deletar avaliação física", description = "Remove a avaliação física de uma consulta")
    public ResponseEntity<Void> deletar(@PathVariable Long consultaId) {
        avaliacaoFisicaService.deletarAvaliacao(consultaId);
        return ResponseEntity.noContent().build();
    }
}