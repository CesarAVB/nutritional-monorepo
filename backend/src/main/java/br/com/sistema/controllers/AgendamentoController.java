package br.com.sistema.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.dtos.AgendamentoRequestDto;
import br.com.sistema.dtos.AgendamentoResponseDto;
import br.com.sistema.dtos.AgendamentoSemanaResponseDto;
import br.com.sistema.dtos.AtualizarStatusAgendamentoDto;
import br.com.sistema.dtos.ContadorHojeDto;
import br.com.sistema.services.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Gerencia operações de agendamento de consultas.
 * Permite criar, consultar, atualizar e cancelar agendamentos, além de
 * calcular horários disponíveis e visualizar a agenda semanal.
 *
 * <p>Suporta listagem por dia e semana, validação de disponibilidade
 * e contagem de agendamentos para dashboard.</p>
 */
@RestController
@RequestMapping("/api/v1/agendamentos")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Endpoints para gestão de agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    /**
     * Cria novo agendamento validando disponibilidade de horário.
     *
     * @param request dados do agendamento incluindo paciente, horário e tipo
     * @return agendamento criado com status inicial AGUARDANDO_CONFIRMACAO
     */
    @PostMapping
    @Operation(summary = "Criar agendamento", description = "Cria novo agendamento validando disponibilidade de horário")
    public ResponseEntity<AgendamentoResponseDto> criar(@Valid @RequestBody AgendamentoRequestDto request) {
        AgendamentoResponseDto created = agendamentoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Atualiza agendamento existente revalidando disponibilidade.
     *
     * @param id identificador do agendamento
     * @param request novos dados do agendamento
     * @return agendamento atualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar agendamento", description = "Atualiza dados do agendamento validando novamente disponibilidade")
    public ResponseEntity<AgendamentoResponseDto> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AgendamentoRequestDto request) {
        AgendamentoResponseDto updated = agendamentoService.atualizar(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Busca agendamento por identificador.
     *
     * @param id identificador do agendamento
     * @return dados completos do agendamento
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar agendamento", description = "Busca agendamento por ID")
    public ResponseEntity<AgendamentoResponseDto> buscarPorId(@PathVariable Long id) {
        AgendamentoResponseDto agendamento = agendamentoService.buscarPorId(id);
        return ResponseEntity.ok(agendamento);
    }

    /**
     * Lista agendamentos de uma semana a partir da data informada.
     * Retorna 7 dias completos começando na segunda-feira da semana.
     *
     * @param data data de referência (opcional, padrão hoje)
     * @return estrutura com agendamentos organizados por dia da semana
     */
    @GetMapping("/semana")
    @Operation(summary = "Listar semana", description = "Lista agendamentos de uma semana completa")
    public ResponseEntity<AgendamentoSemanaResponseDto> listarSemana(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        AgendamentoSemanaResponseDto semana = agendamentoService.listarSemana(data);
        return ResponseEntity.ok(semana);
    }

    /**
     * Lista todos os agendamentos de um dia específico.
     *
     * @param data data da busca
     * @return lista de agendamentos do dia ordenados por horário
     */
    @GetMapping("/dia")
    @Operation(summary = "Listar por dia", description = "Lista todos os agendamentos de um dia específico")
    public ResponseEntity<List<AgendamentoResponseDto>> listarPorDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<AgendamentoResponseDto> agendamentos = agendamentoService.listarPorDia(data);
        return ResponseEntity.ok(agendamentos);
    }

    /**
     * Calcula horários disponíveis para agendamento em uma data.
     * Considera configuração de atendimento e agendamentos existentes.
     *
     * @param data data para verificação de disponibilidade
     * @return lista de horários disponíveis
     */
    @GetMapping("/slots")
    @Operation(summary = "Calcular slots disponíveis", description = "Retorna horários disponíveis para agendamento em uma data")
    public ResponseEntity<List<LocalTime>> calcularSlotsDisponiveis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<LocalTime> slots = agendamentoService.calcularSlotsDisponiveis(data);
        return ResponseEntity.ok(slots);
    }

    /**
     * Conta agendamentos ativos de hoje para exibição em dashboard.
     *
     * @return contador com quantidade de agendamentos
     */
    @GetMapping("/contador-hoje")
    @Operation(summary = "Contar agendamentos hoje", description = "Retorna quantidade de agendamentos ativos hoje")
    public ResponseEntity<ContadorHojeDto> contarHoje() {
        ContadorHojeDto contador = agendamentoService.contarHoje();
        return ResponseEntity.ok(contador);
    }

    /**
     * Atualiza status de um agendamento.
     *
     * @param id identificador do agendamento
     * @param request novo status a ser aplicado
     * @return agendamento com status atualizado
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status", description = "Atualiza status de um agendamento")
    public ResponseEntity<AgendamentoResponseDto> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusAgendamentoDto request) {
        AgendamentoResponseDto updated = agendamentoService.atualizarStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    /**
     * Cancela agendamento alterando status para CANCELADO.
     *
     * @param id identificador do agendamento
     * @return resposta sem conteúdo indicando sucesso
     */
    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar agendamento", description = "Cancela um agendamento existente")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        agendamentoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
