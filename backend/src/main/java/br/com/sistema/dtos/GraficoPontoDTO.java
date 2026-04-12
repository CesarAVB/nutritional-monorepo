package br.com.sistema.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa um ponto em um gráfico SVG do relatório comparativo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraficoPontoDTO {

    /** Coordenada X no SVG */
    private double cx;

    /** Coordenada Y no SVG */
    private double cy;

    /** Rótulo do eixo X (data abreviada) */
    private String label;

    /** Valor original formatado para exibição */
    private String valorFormatado;
}
