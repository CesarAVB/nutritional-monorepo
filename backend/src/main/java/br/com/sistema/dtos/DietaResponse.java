package br.com.sistema.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietaResponse {
    private Long id;
    private Long pacienteId;
    private String nomePaciente;
    private String titulo;
    private LocalDate dataCriacao;
    private String objetivo;
    private Integer kcalTotal;
    private BigDecimal proteinasG;
    private BigDecimal carboidratosG;
    private BigDecimal gordurasG;
    private BigDecimal ingestaoAguaLitros;
    private String observacoes;
    private List<RefeicaoDTO> refeicoes;
    private List<SuplementoDietaDTO> suplementos;
}
