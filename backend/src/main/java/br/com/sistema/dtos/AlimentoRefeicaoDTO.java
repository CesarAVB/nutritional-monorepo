package br.com.sistema.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlimentoRefeicaoDTO {
    private Long id;
    private String nome;
    private BigDecimal quantidade;
    private String unidade;
    private Integer ordem;
    private BigDecimal calorias;
}
