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

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints para dados do dashboard")
public class DashboardController {
    
    private final DashboardService dashboardService;

    // ==============================================
    // # Método - obterEstatisticas
    // # Retorna estatísticas gerais do sistema para o dashboard
    // ==============================================
    @GetMapping("/stats")
    @Operation(summary = "Obter estatísticas", description = "Retorna estatísticas gerais: total de pacientes, consultas hoje, consultas do mês, etc")
    public ResponseEntity<DashboardStatsDTO> obterEstatisticas() {
        DashboardStatsDTO stats = dashboardService.buscarEstatisticas();
        return ResponseEntity.ok(stats);
    }

    // ==============================================
    // # Método - consultasHoje
    // # Lista as consultas agendadas para hoje
    // ==============================================
    @GetMapping("/consultas-hoje")
    @Operation(summary = "Listar consultas de hoje", description = "Retorna todas as consultas agendadas para hoje")
    public ResponseEntity<List<ConsultaHojeDTO>> consultasHoje() {
        List<ConsultaHojeDTO> consultas = dashboardService.buscarConsultasHoje();
        return ResponseEntity.ok(consultas);
    }

    // ==============================================
    // # Método - pacientesRecentes
    // # Retorna os pacientes cadastrados recentemente
    // ==============================================
    @GetMapping("/pacientes-recentes")
    @Operation(summary = "Listar pacientes recentes", description = "Retorna os pacientes cadastrados recentemente")
    public ResponseEntity<List<PacienteDTO>> pacientesRecentes(
            @RequestParam(defaultValue = "5") int limite) {
        List<PacienteDTO> pacientes = dashboardService.buscarPacientesRecentes(limite);
        return ResponseEntity.ok(pacientes);
    }
}