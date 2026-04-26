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

/**
 * Controlador responsavel pelo gerenciamento do questionario de estilo de vida.
 * Captura habitos, historial medico e preferencias alimentares do paciente
 * para fundamentar o plano nutricional e acompanhamento clinico.
 *
 * <p>Cada consulta possui exatamente um questionario. A constraint de unicidade
 * e garantida pelo servico para evitar duplicidade.</p>
 */
@RestController
@RequestMapping("/api/v1/questionario")
@RequiredArgsConstructor
@Tag(name = "Questionario de Estilo de Vida", description = "Endpoints para gestao de questionarios")
public class QuestionarioEstiloVidaController {
    
    private final QuestionarioEstiloVidaService questionarioService;
    
    /**
     * Cria novo questionario de estilo de vida vinculado a uma consulta.
     * Valida que ainda nao existe questionario para esta consulta.
     *
     * @param consultaId ID da consulta
     * @param dados Dados do questionario (objetivo, habitos, historial)
     * @return Questionario criado com status HTTP 201
     */
    @PostMapping("/consulta/{consultaId}")
    @Operation(summary = "Salvar questionario de estilo de vida", description = "Cria um novo questionario para a consulta")
    public ResponseEntity<QuestionarioEstiloVidaDTO> salvar(@PathVariable Long consultaId, @Valid @RequestBody QuestionarioEstiloVidaDTO dados) {
        QuestionarioEstiloVidaDTO saved = questionarioService.salvarQuestionario(consultaId, dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    /**
     * Busca questionario de estilo de vida existente de uma consulta.
     *
     * @param consultaId ID da consulta
     * @return Dados completos do questionario
     * @throws ResourceNotFoundException Se nao existir questionario
     */
    @GetMapping("/consulta/{consultaId}")
    @Operation(summary = "Buscar questionario de estilo de vida", description = "Busca o questionario de uma consulta")
    public ResponseEntity<QuestionarioEstiloVidaDTO> buscar(@PathVariable Long consultaId) {
        QuestionarioEstiloVidaDTO questionario = questionarioService.buscarPorConsulta(consultaId);
        return ResponseEntity.ok(questionario);
    }
    
    /**
     * Atualiza questionario de estilo de vida existente.
     * Mantem campos nao preenchidos no request inalterados.
     *
     * @param consultaId ID da consulta
     * @param dados Novos dados do questionario
     * @return Questionario atualizado com todos os campos
     */
    @PutMapping("/consulta/{consultaId}")
    @Operation(summary = "Atualizar questionario de estilo de vida", description = "Atualiza o questionario de uma consulta")
    public ResponseEntity<QuestionarioEstiloVidaDTO> atualizar(@PathVariable Long consultaId, @Valid @RequestBody QuestionarioEstiloVidaDTO dados) {
        QuestionarioEstiloVidaDTO updated = questionarioService.atualizarQuestionario(consultaId, dados);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Remove questionario de estilo de vida de uma consulta.
     * Operacao irreversivel.
     *
     * @param consultaId ID da consulta
     * @return HTTP 204 sem conteudo
     */
    @DeleteMapping("/consulta/{consultaId}")
    @Operation(summary = "Deletar questionario de estilo de vida", description = "Remove o questionario de uma consulta")
    public ResponseEntity<Void> deletar(@PathVariable Long consultaId) {
        questionarioService.deletarQuestionario(consultaId);
        return ResponseEntity.noContent().build();
    }
}
