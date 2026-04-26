package br.com.sistema.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuplementoDietaDTO {
    private Long id;
    private String nome;
    private String dosagem;
    private String timing;
}
