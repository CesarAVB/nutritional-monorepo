package br.com.sistema.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsoIALogResponse {

    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHora;
    private String provedor;
    private String modelo;
    private int tokensEntrada;
    private int tokensSaida;
    private BigDecimal custoUsd;
    private boolean sucesso;
    private Long pacienteId;
}
