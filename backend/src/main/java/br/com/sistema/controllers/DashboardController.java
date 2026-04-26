package br.com.sistema.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.dtos.ConsultaHojeDTO;
import br.com.sistema.dtos.DashboardStatsDTO;
import br.com.sistema.dtos.PacienteDTO;
import br.com.sistema.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controlador responsavel por fornecer dados de sntese para o painel de controle.
 * Centraliza metricas globais do sistema, consultas do dia e pacientes recentes,
 * permitindo que o dashboard exiba uma visao consolidada sem multiplas requisicoes.
 *
 * <p>Todos os dados sao obtidos em tempo real atraves de consultas agregadas
 * ao banco de dados.</p>
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints para dados do dashboard")
public class DashboardController {
    
    private final DashboardService dashboardService;

    /**
     * Recupera metricas gerais do sistema incluindo total de pacientes,
     * quantidade de consultas do dia e do mes, alem de outros indicadores
     * relevantes para acompanhamento operacional.
     *
     * @return objeto com estatisticas consolidadas do sistema
     */
    @GetMapping("/stats")
    @Operation(summary = "Obter estatisticas", description = "Retorna estatisticas gerais: total de pacientes, consultas hoje, consultas do mes, etc")
    public ResponseEntity<DashboardStatsDTO> obterEstatisticas() {
        DashboardStatsDTO stats = dashboardService.buscarEstatisticas();
        return ResponseEntity.ok(stats);
    }

    /**
     * Lista todas as consultas agendadas para a data atual.
     * Utilizado para exibir a agenda do dia no painel de controle.
     *
     * @return lista de consultas com pacientes ehorarios do dia
     */
    @GetMapping("/consultas-hoje")
    @Operation(summary = "Listar consultas de hoje", description = "Retorna todas as consultas agendadas para hoje")
    public ResponseEntity<List<ConsultaHojeDTO>> consultasHoje() {
        List<ConsultaHojeDTO> consultas = dashboardService.buscarConsultasHoje();
        return ResponseEntity.ok(consultas);
    }

    /**
     * Recupera os pacientes que foram cadastrados mais recentemente.
     * Ordenados por data de criacao de forma decrescente.
     *
     * @param limite quantidade maxima de pacientes a retornar (padrao 5)
     * @return lista de pacientes recentes com dados resumidos
     */
    @GetMapping("/pacientes-recentes")
    @Operation(summary = "Listar pacientes recentes", description = "Retorna os pacientes cadastrados recentemente")
    public ResponseEntity<List<PacienteDTO>> pacientesRecentes(
            @RequestParam(defaultValue = "5") int limite) {
        List<PacienteDTO> pacientes = dashboardService.buscarPacientesRecentes(limite);
        return ResponseEntity.ok(pacientes);
    }
}
