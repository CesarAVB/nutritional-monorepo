package br.com.sistema.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_uso_ia_log")
public class UsoIALog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 30)
    private String provedor;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(nullable = false)
    private int tokensEntrada;

    @Column(nullable = false)
    private int tokensSaida;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal custoUsd;

    @Column(nullable = false)
    private boolean sucesso;

    private Long pacienteId;
}
