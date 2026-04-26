package br.com.sistema.dtos;

import br.com.sistema.enums.ProvedorIA;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoIARequest {

    @NotNull
    private ProvedorIA provedor;

    @NotBlank
    private String apiKey;

    @NotBlank
    private String modelo;

    private String baseUrl;

    private String promptSistema;

    @NotNull
    @DecimalMin("0.1")
    @DecimalMax("1.0")
    private Double temperaturaModelo;
}
