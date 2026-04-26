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

/**
 * Controlador responsavel pelo gerenciamento do registro fotografico nutricional.
 * Gerencia upload, atualizacao e remocao de fotos corporais (4 angulos) armazenadas
 * no S3/MinIO para acompanhamento visual do progresso do paciente.
 *
 * <p>Todas as fotos sao acessadas via URLs presigned com expiracao de 1 hora.
 * Parametros de remocao permite excluir fotos individuais sem reenviar todo o conjunto.</p>
 */
@RestController
@RequestMapping("/api/v1/registro-fotografico")
@RequiredArgsConstructor
@Tag(name = "Registro Fotografico", description = "Endpoints para upload e gerenciamento de fotos")
public class RegistroFotograficoController {

    private final RegistroFotograficoService registroFotograficoService;

    /**
     * Cria novo registro fotografico para uma consulta.
     * Aceita ate 4 fotos (anterior, posterior, laterais) em formato multipart.
     * Todas sao armazenadas no S3 e vinculadas a consulta.
     *
     * @param consultaId ID da consulta
     * @param fotoAnterior Foto frontal (opcional)
     * @param fotoPosterior Foto traseira (opcional)
     * @param fotoLateralEsquerda Foto perfil esquerdo (opcional)
     * @param fotoLateralDireita Foto perfil direito (opcional)
     * @return Keys das fotos salvas com status HTTP 201
     */
    @PostMapping(value = "/consulta/{consultaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Salvar registro fotografico", description = "Faz upload das fotos da consulta")
    public ResponseEntity<RegistroFotograficoDTO> salvar(@PathVariable Long consultaId, @RequestParam(required = false) MultipartFile fotoAnterior, @RequestParam(required = false) MultipartFile fotoPosterior, @RequestParam(required = false) MultipartFile fotoLateralEsquerda, @RequestParam(required = false) MultipartFile fotoLateralDireita) {
        RegistroFotograficoDTO saved = registroFotograficoService.salvarRegistro(consultaId, fotoAnterior, fotoPosterior, fotoLateralEsquerda, fotoLateralDireita);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Busca registro fotografico de uma consulta.
     * Retorna URLs presigned com expiracao de 1 hora para acesso temporario.
     *
     * @param consultaId ID da consulta
     * @return URLs presigned das 4 fotos ou erro se registro nao existir
     */
    @GetMapping("/consulta/{consultaId}")
    @Operation(summary = "Buscar registro fotografico", description = "Retorna as URLs das fotos")
    public ResponseEntity<RegistroFotograficoDTO> buscar(@PathVariable Long consultaId) {
        RegistroFotograficoDTO registro = registroFotograficoService.buscarPorConsulta(consultaId);
        return ResponseEntity.ok(registro);
    }

    /**
     * Atualiza registro fotografico existente (ou cria se nao existir - UPSERT).
     * Permite enviar novas fotos e/ou remover fotos especificas.
     * Fotos existentes sao automaticamente deletadas do S3 quando substituidas.
     *
     * @param consultaId ID da consulta
     * @param fotoAnterior Nova foto frontal (opcional)
     * @param fotoPosterior Nova foto traseira (opcional)
     * @param fotoLateralEsquerda Novo perfil esquerdo (opcional)
     * @param fotoLateralDireita Novo perfil direito (opcional)
     * @param removerFotoAnterior Flag para remover foto frontal
     * @param removerFotoPosterior Flag para remover foto traseira
     * @param removerFotoLateralEsquerda Flag para remover perfil esquerdo
     * @param removerFotoLateralDireita Flag para remover perfil direito
     * @return URLs atualizadas das fotos
     */
    @PutMapping(value = "/consulta/{consultaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Atualizar registro fotografico", description = "Atualiza as fotos da consulta")
    public ResponseEntity<RegistroFotograficoDTO> atualizar(@PathVariable Long consultaId, @RequestParam(required = false) MultipartFile fotoAnterior, @RequestParam(required = false) MultipartFile fotoPosterior, @RequestParam(required = false) MultipartFile fotoLateralEsquerda, 
        @RequestParam(required = false) MultipartFile fotoLateralDireita, @RequestParam(required = false) Boolean removerFotoAnterior, @RequestParam(required = false) Boolean removerFotoPosterior, @RequestParam(required = false) Boolean removerFotoLateralEsquerda, @RequestParam(required = false) Boolean removerFotoLateralDireita
    ) {
        RegistroFotograficoDTO updated = registroFotograficoService.atualizarRegistro(consultaId, fotoAnterior, fotoPosterior, fotoLateralEsquerda, fotoLateralDireita, removerFotoAnterior, removerFotoPosterior, removerFotoLateralEsquerda, removerFotoLateralDireita);
        return ResponseEntity.ok(updated);
    }

    /**
     * Remove registro fotografico e todas as fotos do S3.
     * Operacao irreversivel que deleta arquivos de armazenamento.
     *
     * @param consultaId ID da consulta
     * @return HTTP 204 sem conteudo
     */
    @DeleteMapping("/consulta/{consultaId}")
    @Operation(summary = "Deletar registro fotografico", description = "Remove as fotos da consulta do S3")
    public ResponseEntity<Void> deletar(@PathVariable Long consultaId) {
        registroFotograficoService.deletarRegistro(consultaId);
        return ResponseEntity.noContent().build();
    }
}
