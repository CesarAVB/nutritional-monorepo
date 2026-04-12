package br.com.sistema.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaDetalhadaDTO {
    
    private Long id;
    private Long pacienteId;
    private String nomePaciente;
    private LocalDateTime dataConsulta;
    private AvaliacaoFisicaDTO avaliacaoFisica;
    private QuestionarioEstiloVidaDTO questionario;
    private RegistroFotograficoDTO registroFotografico;
}