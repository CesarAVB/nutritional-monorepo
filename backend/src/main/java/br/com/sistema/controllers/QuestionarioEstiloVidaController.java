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

import br.com.sistema.dtos.QuestionarioEstiloVidaDTO;
import br.com.sistema.services.QuestionarioEstiloVidaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/questionario")
@RequiredArgsConstructor
@Tag(name = "Questionário de Estilo de Vida", description = "Endpoints para gestão de questionários")
public class QuestionarioEstiloVidaController {
    
    private final QuestionarioEstiloVidaService questionarioService;
    
    // ==============================================
    // # Método - salvar
    // # Cria um novo questionário de estilo de vida para a consulta
    // ==============================================
    @PostMapping("/consulta/{consultaId}")
    @Operation(summary = "Salvar questionário de estilo de vida", description = "Cria um novo questionário para a consulta")
    public ResponseEntity<QuestionarioEstiloVidaDTO> salvar(@PathVariable Long consultaId, @Valid @RequestBody QuestionarioEstiloVidaDTO dados) {
    	System.out.println("Recebido salvar Questionario DTO: " + dados);
    	QuestionarioEstiloVidaDTO saved = questionarioService.salvarQuestionario(consultaId, dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    // ==============================================
    // # Método - buscar
    // # Busca o questionário de estilo de vida de uma consulta
    // ==============================================
    @GetMapping("/consulta/{consultaId}")
    @Operation(summary = "Buscar questionário de estilo de vida", description = "Busca o questionário de uma consulta")
    public ResponseEntity<QuestionarioEstiloVidaDTO> buscar(@PathVariable Long consultaId) {
        QuestionarioEstiloVidaDTO questionario = questionarioService.buscarPorConsulta(consultaId);
        return ResponseEntity.ok(questionario);
    }
    
    // ==============================================
    // # Método - atualizar
    // # Atualiza o questionário de estilo de vida de uma consulta
    // ==============================================
    @PutMapping("/consulta/{consultaId}")
    @Operation(summary = "Atualizar questionário de estilo de vida", description = "Atualiza o questionário de uma consulta")
    public ResponseEntity<QuestionarioEstiloVidaDTO> atualizar(@PathVariable Long consultaId, @Valid @RequestBody QuestionarioEstiloVidaDTO dados) {
    	System.out.println("Atualizando questionário de estilo de vida para consulta ID: " + consultaId + "; DTO recebido: " + dados);
        QuestionarioEstiloVidaDTO updated = questionarioService.atualizarQuestionario(consultaId, dados);
        return ResponseEntity.ok(updated);
    }
    
    // ==============================================
    // # Método - deletar
    // # Remove o questionário de estilo de vida de uma consulta
    // ==============================================
    @DeleteMapping("/consulta/{consultaId}")
    @Operation(summary = "Deletar questionário de estilo de vida", description = "Remove o questionário de uma consulta")
    public ResponseEntity<Void> deletar(@PathVariable Long consultaId) {
        questionarioService.deletarQuestionario(consultaId);
        return ResponseEntity.noContent().build();
    }
}