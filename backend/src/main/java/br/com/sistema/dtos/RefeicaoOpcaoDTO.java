package br.com.sistema.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefeicaoOpcaoDTO {
    private Long id;
    private Integer numeroOpcao;
    private List<AlimentoRefeicaoDTO> alimentos;
}
