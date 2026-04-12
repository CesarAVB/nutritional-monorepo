package br.com.sistema.dtos;

import lombok.Data;

@Data
public class DashboardStatsDTO {
    private Long totalPacientes;
    private Long consultasHoje;
    private Long consultasMes;
    private String proximaConsulta;
}