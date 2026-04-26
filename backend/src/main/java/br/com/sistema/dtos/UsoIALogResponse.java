package br.com.sistema.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsoIALogResponse {

    private Long id;
    private LocalDateTime dataHora;
    private String provedor;
    private String modelo;
    private int tokensEntrada;
    private int tokensSaida;
    private BigDecimal custoUsd;
    private boolean sucesso;
    private Long pacienteId;
}
