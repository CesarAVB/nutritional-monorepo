package br.com.sistema.dtos;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoSemanaResponseDto {

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private List<AgendamentoDiaDto> dias;
}
