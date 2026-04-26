package br.com.sistema.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsoIAResumoResponse {

    private long totalChamadas;
    private long totalTokens;
    private BigDecimal custoTotalUsd;
    private BigDecimal custoUltimos30DiasUsd;
}
