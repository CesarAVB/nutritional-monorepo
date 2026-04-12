package br.com.sistema.dtos;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class ConsultaCompletaRequest {
    
    @Valid
    private AvaliacaoFisicaDTO avaliacaoFisica;
    
    @Valid
    private QuestionarioEstiloVidaDTO questionario;
    
}