
package br.com.sistema.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ConsultaAtualizacaoDTO {
    private LocalDateTime dataConsulta;
    private AvaliacaoFisicaDTO avaliacaoFisica;
    private QuestionarioEstiloVidaDTO questionarioEstiloVida;
    private RegistroFotograficoDTO registroFotografico;
}
