package br.com.sistema.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietaContextoPacienteDTO {
    private String objetivoUltimaConsulta;
    private Integer numeroRefeicoesDesejadas;
}
