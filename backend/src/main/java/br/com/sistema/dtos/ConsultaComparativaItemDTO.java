package br.com.sistema.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa os dados de uma consulta para exibição no relatório comparativo.
 * Cada instância corresponde a uma consulta com avaliação física registrada.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaComparativaItemDTO {

    private Long consultaId;
    private LocalDateTime dataConsulta;
    private String dataFormatada;         // "dd/MM/yyyy"
    private String dataAbreviada;         // "MMM/yy" para eixo X dos gráficos

    // Composição Corporal
    private Double peso;
    private Double imc;
    private Double percentualGordura;
    private Double massaMagra;
    private Double massaGorda;

    // Perímetros-chave (cm)
    private Double perimetroCintura;
    private Double perimetroAbdominal;
    private Double perimetroQuadril;
    private Double perimetroBracoDireitoRelax;
    private Double perimetroPanturrilhaDireita;

    // Objetivo do paciente na consulta
    private String objetivo;

    // Registro fotográfico da consulta (opcional)
    private String fotoAnterior;
    private String fotoPosterior;
    private String fotoLateralEsquerda;
    private String fotoLateralDireita;

    // Número sequencial da consulta (1, 2, 3...)
    private int numeroConsulta;
}
