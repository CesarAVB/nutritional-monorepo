package br.com.sistema.dtos;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ConsultaHojeDTO {
    private Long id;
    private Long pacienteId;
    private String nomePaciente;
    private String iniciais;
    private LocalDateTime horario;
    private String objetivo;
}