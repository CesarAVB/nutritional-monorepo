package br.com.sistema.dtos;

import java.time.LocalDateTime;

import br.com.sistema.enums.StatusAgendamento;
import br.com.sistema.enums.TipoAgendamento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoResponseDto {

    private Long id;
    private Long pacienteId;
    private String nomePaciente;
    private String telefoneWhatsapp;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private Integer duracaoMinutos;
    private TipoAgendamento tipo;
    private StatusAgendamento status;
    private String observacoes;
    private LocalDateTime criadoEm;
}
