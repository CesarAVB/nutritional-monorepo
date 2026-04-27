package br.com.sistema.dtos;

import br.com.sistema.enums.StatusAgendamento;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarStatusAgendamentoDto {

    @NotNull(message = "Status é obrigatório")
    private StatusAgendamento status;
}
