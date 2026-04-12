package br.com.sistema.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.sistema.dtos.RegistroFotograficoDTO;
import br.com.sistema.services.RegistroFotograficoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/registro-fotografico")
@RequiredArgsConstructor
@Tag(name = "Registro Fotográfico", description = "Endpoints para upload e gerenciamento de fotos")
public class RegistroFotograficoController {

    private final RegistroFotograficoService registroFotograficoService;

    // ==============================================
    // # Método - salvar
    // # Faz upload das fotos de uma consulta e retorna os dados do registro
    // ==============================================
    @PostMapping(value = "/consulta/{consultaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Salvar registro fotográfico", description = "Faz upload das fotos da consulta")
    public ResponseEntity<RegistroFotograficoDTO> salvar(@PathVariable Long consultaId, @RequestParam(required = false) MultipartFile fotoAnterior, @RequestParam(required = false) MultipartFile fotoPosterior, @RequestParam(required = false) MultipartFile fotoLateralEsquerda, @RequestParam(required = false) MultipartFile fotoLateralDireita) {
        RegistroFotograficoDTO saved = registroFotograficoService.salvarRegistro(consultaId, fotoAnterior, fotoPosterior, fotoLateralEsquerda, fotoLateralDireita);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ==============================================
    // # Método - buscar
    // # Retorna as URLs do registro fotográfico de uma consulta
    // ==============================================
    @GetMapping("/consulta/{consultaId}")
    @Operation(summary = "Buscar registro fotográfico", description = "Retorna as URLs das fotos")
    public ResponseEntity<RegistroFotograficoDTO> buscar(@PathVariable Long consultaId) {
        RegistroFotograficoDTO registro = registroFotograficoService.buscarPorConsulta(consultaId);
        return ResponseEntity.ok(registro);
    }

    // ==============================================
    // # Método - atualizar
    // # Atualiza as fotos de um registro fotográfico (aceita flags de remoção)
    // ==============================================
    @PutMapping(value = "/consulta/{consultaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Atualizar registro fotográfico", description = "Atualiza as fotos da consulta")
    public ResponseEntity<RegistroFotograficoDTO> atualizar(@PathVariable Long consultaId, @RequestParam(required = false) MultipartFile fotoAnterior, @RequestParam(required = false) MultipartFile fotoPosterior, @RequestParam(required = false) MultipartFile fotoLateralEsquerda, 
        @RequestParam(required = false) MultipartFile fotoLateralDireita, @RequestParam(required = false) Boolean removerFotoAnterior, @RequestParam(required = false) Boolean removerFotoPosterior, @RequestParam(required = false) Boolean removerFotoLateralEsquerda, @RequestParam(required = false) Boolean removerFotoLateralDireita
    ) {
        System.err.println("Atualizando registro fotográfico para consulta ID: " + consultaId);
        RegistroFotograficoDTO updated = registroFotograficoService.atualizarRegistro(consultaId, fotoAnterior, fotoPosterior, fotoLateralEsquerda, fotoLateralDireita, removerFotoAnterior, removerFotoPosterior, removerFotoLateralEsquerda, removerFotoLateralDireita);
        return ResponseEntity.ok(updated);
    }

    // ==============================================
    // # Método - deletar
    // # Remove o registro fotográfico de uma consulta
    // ==============================================
    @DeleteMapping("/consulta/{consultaId}")
    @Operation(summary = "Deletar registro fotográfico", description = "Remove as fotos da consulta do S3")
    public ResponseEntity<Void> deletar(@PathVariable Long consultaId) {
        registroFotograficoService.deletarRegistro(consultaId);
        return ResponseEntity.noContent().build();
    }
}