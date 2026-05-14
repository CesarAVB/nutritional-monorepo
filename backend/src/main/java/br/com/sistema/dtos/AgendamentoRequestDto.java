package br.com.sistema.dtos;

import java.time.LocalDateTime;

import br.com.sistema.enums.TipoAgendamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoRequestDto {

    private Long pacienteId;

    private String nomePaciente;

    @Pattern(regexp = "\\d{10,15}", message = "Telefone invalido")
    private String telefoneWhatsapp;

    @NotNull(message = "Data e hora de inicio sao obrigatorias")
    private LocalDateTime dataHoraInicio;

    @NotNull(message = "Duracao em minutos e obrigatoria")
    private Integer duracaoMinutos;

    @NotNull(message = "Tipo de agendamento e obrigatorio")
    private TipoAgendamento tipo;

    private String observacoes;
}
