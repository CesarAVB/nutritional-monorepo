package br.com.sistema.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.dtos.ComparativoConsultasDTO;
import br.com.sistema.dtos.ConsultaAtualizacaoDTO;
import br.com.sistema.dtos.ConsultaDetalhadaDTO;
import br.com.sistema.dtos.ConsultaListagemDTO;
import br.com.sistema.dtos.ConsultaResumoDTO;
import br.com.sistema.services.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controlador responsavel por gerenciar o ciclo de vida completo das consultas.
 * Gerencia a criacao, listagem, detalhamento e comparacao de consultas, alem de
 * permitir a busca de rascunhos para pre-preenchimento de novas entradas.
 *
 * <p>Suporta paginacao para listagens extensas e permite comparacao entre duas
 * consultas do mesmo paciente para analise de progresso.</p>
 */
@RestController
@RequestMapping("/api/v1/consultas")
@RequiredArgsConstructor
@Tag(name = "Consultas", description = "Endpoints para gestao de consultas e avaliacoes")
public class ConsultaController {

	private final ConsultaService consultaService;

	/**
	 * Cria uma nova consulta para um paciente no sistema.
	 * A nova consulta inicia sem avaliacoes ou fotos associadas.
	 *
	 * @param pacienteId identificador do paciente que tera a consulta criada
	 * @return dados resumidos da consulta criada com identificador gerado
	 */
	@PostMapping("/paciente/{pacienteId}")
	@Operation(summary = "Criar nova consulta", description = "Cria uma nova consulta para o paciente")
	public ResponseEntity<ConsultaResumoDTO> criar(@PathVariable Long pacienteId) {
		ConsultaResumoDTO saved = consultaService.criarConsulta(pacienteId);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	/**
	 * Lista todas as consultas registradas no sistema.
	 * Ordenadas por data de forma decrescente.
	 *
	 * @return lista completa de consultas resumidas ordenadas por data
	 */
	@GetMapping
	@Operation(summary = "Listar todas as consultas", description = "Retorna todas as consultas do sistema ordenadas por data")
	public ResponseEntity<List<ConsultaListagemDTO>> listarTodas() {
		List<ConsultaListagemDTO> consultas = consultaService.listarTodasConsultas();
		return ResponseEntity.ok(consultas);
	}

	/**
	 * Lista consultas do sistema de forma paginada com ordenacao configuravel.
	 *
	 * @param page numero da pagina comeando em 0
	 * @param size quantidade de itens por pagina (padrao 10)
	 * @param sort campo para ordenacao (padrao dataConsulta)
	 * @param direction sentido da ordenacao: asc ou desc (padrao desc)
	 * @return pagina de consultas resumidas conforme criterio informado
	 */
	@GetMapping("/paginado")
	@Operation(summary = "Listar consultas paginadas", description = "Retorna consultas do sistema com paginacao")
	public ResponseEntity<Page<ConsultaListagemDTO>> listarTodasPaginado(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "dataConsulta") String sort,
			@RequestParam(defaultValue = "desc") String direction) {
		Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
		Page<ConsultaListagemDTO> consultas = consultaService.listarTodasConsultasPaginado(pageable);
		return ResponseEntity.ok(consultas);
	}

	/**
	 * Lista o historico completo de consultas de um paciente especifico.
	 * Ordenado por data de forma decrescente.
	 *
	 * @param pacienteId identificador do paciente
	 * @return lista de consultas resumidas do paciente ordenadas por data
	 */
	@GetMapping("/paciente/{pacienteId}")
	@Operation(summary = "Listar consultas do paciente", description = "Retorna o historico de consultas ordenado por data")
	public ResponseEntity<List<ConsultaResumoDTO>> listarPorPaciente(@PathVariable Long pacienteId) {
		List<ConsultaResumoDTO> consultas = consultaService.listarConsultasPorPaciente(pacienteId);
		return ResponseEntity.ok(consultas);
	}

	/**
	 * Recupera os dados da ultima consulta de um paciente para pre-preenchimento
	 * em uma nova entrada. Permite continuidade e reducao de digitacao.
	 *
	 * @param pacienteId identificador do paciente
	 * @return dados detalhados da ultima consulta ou 204 se nenhuma existir
	 */
	@GetMapping("/paciente/{pacienteId}/rascunho")
	@Operation(summary = "Buscar rascunho da nova consulta", description = "Retorna os dados da ultima consulta do paciente para pre-preenchimento")
	public ResponseEntity<ConsultaDetalhadaDTO> buscarRascunhoNovaConsulta(@PathVariable Long pacienteId) {
		return consultaService.buscarRascunhoNovaConsulta(pacienteId)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	/**
	 * Lista consultas de um paciente especifico de forma paginada.
	 *
	 * @param pacienteId identificador do paciente
	 * @param page numero da pagina comeando em 0
	 * @param size quantidade de itens por pagina (padrao 10)
	 * @param sort campo para ordenacao (padrao dataConsulta)
	 * @param direction sentido da ordenacao: asc ou desc (padrao desc)
	 * @return pagina de consultas resumidas do paciente conforme criterio informado
	 */
	@GetMapping("/paciente/{pacienteId}/paginado")
	@Operation(summary = "Listar consultas do paciente paginadas", description = "Retorna historico de consultas com paginacao")
	public ResponseEntity<Page<ConsultaResumoDTO>> listarPorPacientePaginado(
			@PathVariable Long pacienteId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "dataConsulta") String sort,
			@RequestParam(defaultValue = "desc") String direction) {
		Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
		Page<ConsultaResumoDTO> consultas = consultaService.listarConsultasPorPacientePaginado(pacienteId, pageable);
		return ResponseEntity.ok(consultas);
	}

	/**
	 * Recupera todos os detalhes de uma consulta especifica, incluindo
	 * avaliacao fisica, registro fotografico e demais dados relacionados.
	 *
	 * @param id identificador da consulta
	 * @return dados completos da consulta com todas as associacoes
	 */
	@GetMapping("/{id}")
	@Operation(summary = "Buscar consulta completa", description = "Retorna todos os detalhes da consulta incluindo avaliacoes e fotos")
	public ResponseEntity<ConsultaDetalhadaDTO> buscarCompleta(@PathVariable Long id) {
		ConsultaDetalhadaDTO consulta = consultaService.buscarConsultaCompleta(id);
		return ResponseEntity.ok(consulta);
	}

	/**
	 * Compara avaliacoes e dados entre duas consultas do mesmo paciente.
	 * Retorna diferenca de medidas, anotacoes e indicadores calculados.
	 *
	 * @param pacienteId identificador do paciente
	 * @param consultaInicialId identificador da consulta mais antiga
	 * @param consultaFinalId identificador da consulta mais recente
	 * @return comparativo detalhado entre as duas consultas selecionadas
	 */
	@GetMapping("/comparar/{pacienteId}")
	@Operation(summary = "Comparar duas consultas", description = "Compara avaliacoes entre duas consultas do mesmo paciente")
	public ResponseEntity<ComparativoConsultasDTO> comparar(@PathVariable Long pacienteId, @RequestParam Long consultaInicialId, @RequestParam Long consultaFinalId) {
		ComparativoConsultasDTO comparativo = consultaService.compararConsultas(pacienteId, consultaInicialId, consultaFinalId);
		return ResponseEntity.ok(comparativo);
	}

	/**
	 * Remove uma consulta e todos os seus dados relacionados,
	 * incluindo avaliacao fisica, registros fotograficos e historico.
	 *
	 * @param id identificador da consulta a ser removida
	 * @return resposta vazia com status 204 em caso de sucesso
	 */
	@DeleteMapping("/{id}")
	@Operation(summary = "Deletar consulta", description = "Remove uma consulta e seus dados relacionados")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		consultaService.deletarConsulta(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Atualiza dados basicos de uma consulta existente como
	 * data, hora e observacoes gerais.
	 *
	 * @param id identificador da consulta
	 * @param dados novos valores para os campos atualizaveis
	 * @return dados completos da consulta apos aplicacao das modificacoes
	 */
	@PutMapping("/{id}")
	@Operation(summary = "Atualizar consulta", description = "Atualiza dados basicos da consulta")
	public ResponseEntity<ConsultaDetalhadaDTO> atualizarConsulta(@PathVariable Long id, @RequestBody ConsultaAtualizacaoDTO dados) {
	    ConsultaDetalhadaDTO updated = consultaService.atualizarConsulta(id, dados);
	    return ResponseEntity.ok(updated);
	}
}
