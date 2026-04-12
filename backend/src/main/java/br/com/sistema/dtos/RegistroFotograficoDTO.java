package br.com.sistema.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroFotograficoDTO {
    
    private Long id;
    private Long consultaId;
    private String fotoAnterior;
    private String fotoPosterior;
    private String fotoLateralEsquerda;
    private String fotoLateralDireita;
}