package br.com.sistema.dtos;

import java.math.BigDecimal;
import java.util.List;

import br.com.sistema.enums.TipoRefeicao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietaRequest {

    private String titulo;
    private String objetivo;
    private Integer kcalTotal;
    private BigDecimal proteinasG;
    private BigDecimal carboidratosG;
    private BigDecimal gordurasG;
    private BigDecimal ingestaoAguaLitros;
    private String observacoes;
    private List<RefeicaoRequest> refeicoes;
    private List<SuplementoRequest> suplementos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefeicaoRequest {
        private TipoRefeicao tipo;
        private Integer ordemExibicao;
        private List<OpcaoRequest> opcoes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpcaoRequest {
        private Integer numeroOpcao;
        private List<AlimentoRequest> alimentos;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlimentoRequest {
        private String nome;
        private BigDecimal quantidade;
        private String unidade;
        private Integer ordem;
        private BigDecimal calorias;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuplementoRequest {
        private String nome;
        private String dosagem;
        private String timing;
    }
}
