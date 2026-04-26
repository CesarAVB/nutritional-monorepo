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

/**
 * Controlador responsavel por gerenciar as avaliacoes fisicas vinculadas as consultas.
 * Cada consulta pode conter uma unica avaliacao fisica com anotacoes corporais, medidas
 * e indicadores de saude que permitem o acompanhamento longitudinal do paciente.
 *
 * <p>Todas as operacoes sao sincronas e acessam diretamente a entidade Consulta para
 * associar ou recuperar a avaliacao fisica correspondente.</p>
 */
@RestController
@RequestMapping("/api/v1/avaliacoes")
@RequiredArgsConstructor
@Tag(name = "Avaliacoes Fisicas", description = "Endpoints para gestao de avaliacoes fisicas")
public class AvaliacaoFisicaController {
    
    private final AvaliacaoFisicaService avaliacaoFisicaService;

    /**
     * Cria uma nova avaliacao fisica vinculada a uma consulta existente.
     *
     * @param consultaId identificador da consulta que recebera a avaliacao
     * @param dto dados da avaliacao fisica com anotacoes, medidas e indicadores
     * @return avaliacao fisica salva com identificador gerado
     */
    @PostMapping("/consulta/{consultaId}")
    @Operation(summary = "Salvar avaliacao fisica", description = "Cria uma nova avaliacao fisica para a consulta")
    public ResponseEntity<AvaliacaoFisicaDTO> salvar(@PathVariable Long consultaId, @Valid @RequestBody AvaliacaoFisicaDTO dto) {
        System.out.println("Recebido salvar Avaliacao DTO: " + dto);
        AvaliacaoFisicaDTO saved = avaliacaoFisicaService.salvarAvaliacao(consultaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Recupera a avaliacao fisica associada a uma consulta especifica.
     *
     * @param consultaId identificador da consulta
     * @return avaliacao fisica encontrada ou erro caso nao exista
     */
    @GetMapping("/consulta/{consultaId}")
    @Operation(summary = "Buscar avaliacao fisica", description = "Busca a avaliacao fisica de uma consulta")
    public ResponseEntity<AvaliacaoFisicaDTO> buscar(@PathVariable Long consultaId) {
        AvaliacaoFisicaDTO avaliacao = avaliacaoFisicaService.buscarPorConsulta(consultaId);
        return ResponseEntity.ok(avaliacao);
    }

    /**
     * Atualiza os dados da avaliacao fisica de uma consulta existente.
     *
     * @param consultaId identificador da consulta
     * @param dados novos valores para a avaliacao fisica
     * @return avaliacao fisica atualizada com todas as modificacoes aplicadas
     */
    @PutMapping("/consulta/{consultaId}")
    @Operation(summary = "Atualizar avaliacao fisica", description = "Atualiza a avaliacao fisica de uma consulta")
    public ResponseEntity<AvaliacaoFisicaDTO> atualizar(@PathVariable Long consultaId, @Valid @RequestBody AvaliacaoFisicaDTO dados) {
    	System.out.println("Atualizando avaliacao fisica para consulta ID: " + consultaId + "; DTO recebido: " + dados);
        AvaliacaoFisicaDTO updated = avaliacaoFisicaService.atualizarAvaliacao(consultaId, dados);
        return ResponseEntity.ok(updated);
    }

    /**
     * Remove a avaliacao fisica vinculada a uma consulta.
     *
     * @param consultaId identificador da consulta
     * @return resposta vazia com status 204 em caso de sucesso
     */
    @DeleteMapping("/consulta/{consultaId}")
    @Operation(summary = "Deletar avaliacao fisica", description = "Remove a avaliacao fisica de uma consulta")
    public ResponseEntity<Void> deletar(@PathVariable Long consultaId) {
        avaliacaoFisicaService.deletarAvaliacao(consultaId);
        return ResponseEntity.noContent().build();
    }
}
