package br.com.sistema.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dados pré-computados para renderizar um gráfico de linha SVG no template Thymeleaf.
 * Todos os cálculos de coordenadas SVG são feitos no Java (service), mantendo o template limpo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraficoSVGDTO {

    // ── Configuração visual ──────────────────────────────────────────────────
    private String titulo;
    private String unidade;        // ex: "kg", "%", "cm"
    private String cor;            // cor principal da linha (hex)  ex: "#0d9488"

    // ── Linha principal ──────────────────────────────────────────────────────
    /** String de pontos SVG: "x1,y1 x2,y2 ..." para <polyline points="..."> */
    private String polylinePoints;

    /** Pontos individuais com coordenadas e valores para renderizar círculos/labels */
    private List<GraficoPontoDTO> pontos;

    // ── Linha secundária (opcional, ex: Massa Magra + Massa Gorda juntas) ───
    private String tituloSecundario;
    private String corSecundaria;
    private String polylinePointsSecundarios;
    private List<GraficoPontoDTO> pontosSecundarios;

    // ── Eixo Y ───────────────────────────────────────────────────────────────
    /**
     * Linhas de grade do eixo Y: pares [y_coord, label_texto].
     * Cada elemento é um array de 2 strings: [0]=y_svg, [1]=valor_formatado
     */
    private List<String[]> yAxisLabels;

    // ── Dimensão do SVG ──────────────────────────────────────────────────────
    private double svgWidth  = 500;
    private double svgHeight = 170;

    // ── SVG pré-renderizado (injetado via th:utext para evitar parsing Thymeleaf/XML) ──
    private String svgHtml;
}
