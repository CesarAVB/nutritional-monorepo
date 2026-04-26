package br.com.sistema.dtos;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietaResumoResponse {
    private Long id;
    private String titulo;
    private LocalDate dataCriacao;
    private String objetivo;
    private Integer kcalTotal;
    private Integer totalRefeicoes;
}
