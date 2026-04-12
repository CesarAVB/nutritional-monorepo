package br.com.sistema.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparativoConsultasDTO {
    private ConsultaDetalhadaDTO consultaInicial;
    private ConsultaDetalhadaDTO consultaFinal;
    private DiferencasDTO diferencas;
}