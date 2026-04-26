package br.com.sistema.dtos;

import java.util.List;

import br.com.sistema.enums.TipoRefeicao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefeicaoDTO {
    private Long id;
    private TipoRefeicao tipo;
    private Integer ordemExibicao;
    private List<RefeicaoOpcaoDTO> opcoes;
}
