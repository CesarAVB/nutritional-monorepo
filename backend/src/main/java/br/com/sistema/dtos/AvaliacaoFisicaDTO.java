package br.com.sistema.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoFisicaDTO {
    
    private Long id;
    private Long consultaId;
    private Double altura;
    
    // Perímetros (cm)
    private Double perimetroOmbro;
    private Double perimetroTorax;
    private Double perimetroCintura;
    private Double perimetroAbdominal;
    private Double perimetroQuadril;
    private Double perimetroBracoDireitoRelax;
    private Double perimetroBracoDireitoContr;
    private Double perimetroBracoEsquerdoRelax;
    private Double perimetroBracoEsquerdoContr;
    @JsonAlias({"antebracoDireito", "antebraco_direito", "perimetroAntebracoDir"})
    private Double perimetroAntebracoDireito;
    @JsonAlias({"antebracoEsquerdo", "antebraco_esquerdo", "perimetroAntebracoEsq"})
    private Double perimetroAntebracoEsquerdo;
    private Double perimetroCoxa;
    private Double perimetroCoxaDireita;
    private Double perimetroCoxaEsquerda;
    private Double perimetroPanturrilhaDireita;
    private Double perimetroPanturrilhaEsquerda;
    
    // Dobras Cutâneas (mm)
    private Double dobraTriceps;
    @JsonAlias({"peitoral", "peito"})
    private Double dobraPeito;
    private Double dobraAxilarMedia;
    private Double dobraSubescapular;
    private Double dobraAbdominal;
    private Double dobraSupraIliaca;
    private Double dobraCoxa;
    
    // Composição Corporal
    private Double pesoAtual;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Double massaMagra;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Double massaGorda;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Double percentualGordura;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Double imc;
}