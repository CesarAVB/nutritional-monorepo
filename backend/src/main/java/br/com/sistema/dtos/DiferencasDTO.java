package br.com.sistema.dtos;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiferencasDTO {
    private Double diferencaPeso;
    private Double diferencaPercentualGordura;
    private Double diferencaMassaMagra;
    private Double diferencaMassaGorda;
    private Double diferencaImc;
    private Map<String, Double> diferencasPerimetros;
    private Map<String, Double> diferencasDobras;
}