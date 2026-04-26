package br.com.sistema.dtos;

import br.com.sistema.enums.ProvedorIA;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoIAResponse {

    private Long id;
    private ProvedorIA provedor;
    private String apiKey;
    private String modelo;
    private String baseUrl;
    private String promptSistema;
    private Double temperaturaModelo;
}
