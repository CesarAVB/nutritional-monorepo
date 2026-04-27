package br.com.sistema.dtos;

import java.time.LocalDateTime;

import br.com.sistema.enums.TipoAgendamento;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoRequestDto {

    @NotNull(message = "ID do paciente é obrigatório")
    private Long pacienteId;

    @NotNull(message = "Data e hora de início são obrigatórias")
    private LocalDateTime dataHoraInicio;

    @NotNull(message = "Duração em minutos é obrigatória")
    private Integer duracaoMinutos;

    @NotNull(message = "Tipo de agendamento é obrigatório")
    private TipoAgendamento tipo;

    private String observacoes;
}
