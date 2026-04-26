package br.com.sistema.dtos.ia;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GerarDietaIARequest {

    @NotNull
    @Min(1)
    private Integer kcalTotal;

    @NotNull
    private BigDecimal proteinasG;

    @NotNull
    private BigDecimal carboidratosG;

    @NotNull
    private BigDecimal gordurasG;

    private String titulo;
    private String objetivo;
}
