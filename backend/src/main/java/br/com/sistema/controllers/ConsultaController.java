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

@RestController
@RequestMapping("/api/v1/consultas")
@RequiredArgsConstructor
@Tag(name = "Consultas", description = "Endpoints para gestão de consultas e avaliações")
public class ConsultaController {

	private final ConsultaService consultaService;

	// ==============================================
	// # Método - criar
	// # Cria uma nova consulta para um paciente
	// ==============================================
	@PostMapping("/paciente/{pacienteId}")
	@Operation(summary = "Criar nova consulta", description = "Cria uma nova consulta para o paciente")
	public ResponseEntity<ConsultaResumoDTO> criar(@PathVariable Long pacienteId) {
		ConsultaResumoDTO saved = consultaService.criarConsulta(pacienteId);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	// ==============================================
	// # Método - listarTodas
	// # Lista todas as consultas do sistema
	// ==============================================
	@GetMapping
	@Operation(summary = "Listar todas as consultas", description = "Retorna todas as consultas do sistema ordenadas por data")
	public ResponseEntity<List<ConsultaListagemDTO>> listarTodas() {
		List<ConsultaListagemDTO> consultas = consultaService.listarTodasConsultas();
		return ResponseEntity.ok(consultas);
	}

	// ==============================================
	// # Metodo - listarTodasPaginado
	// # Lista consultas do sistema de forma paginada
	// ==============================================
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

	// ==============================================
	// # Método - listarPorPaciente
	// # Lista consultas de um paciente
	// ==============================================
	@GetMapping("/paciente/{pacienteId}")
	@Operation(summary = "Listar consultas do paciente", description = "Retorna o histórico de consultas ordenado por data")
	public ResponseEntity<List<ConsultaResumoDTO>> listarPorPaciente(@PathVariable Long pacienteId) {
		List<ConsultaResumoDTO> consultas = consultaService.listarConsultasPorPaciente(pacienteId);
		return ResponseEntity.ok(consultas);
	}

	// ==============================================
	// # Método - buscarRascunhoNovaConsulta
	// # Retorna os dados da última consulta para pré-preenchimento da nova
	// ==============================================
	@GetMapping("/paciente/{pacienteId}/rascunho")
	@Operation(summary = "Buscar rascunho da nova consulta", description = "Retorna os dados da última consulta do paciente para pré-preenchimento")
	public ResponseEntity<ConsultaDetalhadaDTO> buscarRascunhoNovaConsulta(@PathVariable Long pacienteId) {
		return consultaService.buscarRascunhoNovaConsulta(pacienteId)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	// ==============================================
	// # Metodo - listarPorPacientePaginado
	// # Lista consultas de um paciente de forma paginada
	// ==============================================
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

	// ==============================================
	// # Método - buscarCompleta
	// # Busca os detalhes completos de uma consulta por ID
	// ==============================================
	@GetMapping("/{id}")
	@Operation(summary = "Buscar consulta completa", description = "Retorna todos os detalhes da consulta incluindo avaliações e fotos")
	public ResponseEntity<ConsultaDetalhadaDTO> buscarCompleta(@PathVariable Long id) {
		ConsultaDetalhadaDTO consulta = consultaService.buscarConsultaCompleta(id);
		return ResponseEntity.ok(consulta);
	}

	// ==============================================
	// # Método - comparar
	// # Compara duas consultas de um paciente
	// ==============================================
	@GetMapping("/comparar/{pacienteId}")
	@Operation(summary = "Comparar duas consultas", description = "Compara avaliações entre duas consultas do mesmo paciente")
	public ResponseEntity<ComparativoConsultasDTO> comparar(@PathVariable Long pacienteId, @RequestParam Long consultaInicialId, @RequestParam Long consultaFinalId) {
		ComparativoConsultasDTO comparativo = consultaService.compararConsultas(pacienteId, consultaInicialId, consultaFinalId);
		return ResponseEntity.ok(comparativo);
	}

	// ==============================================
	// # Método - deletar
	// # Deleta uma consulta e seus dados relacionados
	// ==============================================
	@DeleteMapping("/{id}")
	@Operation(summary = "Deletar consulta", description = "Remove uma consulta e seus dados relacionados")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		consultaService.deletarConsulta(id);
		return ResponseEntity.noContent().build();
	}

    // ==============================================
    // # Método - atualizarConsulta
    // # Atualiza dados básicos da consulta
    // ==============================================
	@PutMapping("/{id}")
	@Operation(summary = "Atualizar consulta", description = "Atualiza dados básicos da consulta")
	public ResponseEntity<ConsultaDetalhadaDTO> atualizarConsulta(@PathVariable Long id, @RequestBody ConsultaAtualizacaoDTO dados) {
	    ConsultaDetalhadaDTO updated = consultaService.atualizarConsulta(id, dados);
	    return ResponseEntity.ok(updated);
	}


}