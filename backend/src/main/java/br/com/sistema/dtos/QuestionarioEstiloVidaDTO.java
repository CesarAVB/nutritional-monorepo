package br.com.sistema.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionarioEstiloVidaDTO {
    
    private Long id;
    private Long consultaId;
    
    // Rotina
    private String objetivo;
    private String frequenciaTreino;
    private String tempoTreino;
    
    // Saúde
    private String cirurgias;
    private String doencas;
    private String historicoFamiliar;
    private String medicamentos;
    private String suplementos;
    private String usoAnabolizantes;
    private String cicloAnabolizantes;
    private String duracaoAnabolizantes;
    
    // Hábitos
    private Boolean fuma;
    private String frequenciaAlcool;
    private String funcionamentoIntestino;
    private String qualidadeSono;
    private Double ingestaoAguaDiaria;
    
    // Preferências
    private String alimentosNaoGosta;
    private String frutasPreferidas;
    private Integer numeroRefeicoesDesejadas;
    private String horarioMaiorFome;
    
    // Clínico
    private String pressaoArterial;
    private String intolerancias;
}