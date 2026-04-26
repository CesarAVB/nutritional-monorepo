package br.com.sistema.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_alimentos_refeicao")
public class AlimentoRefeicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcao_id", nullable = false)
    private RefeicaoOpcao opcao;

    @Column(nullable = false, length = 300)
    private String nome;

    @Column(precision = 8, scale = 2)
    private BigDecimal quantidade;

    @Column(length = 50)
    private String unidade;

    @Column(nullable = false)
    private Integer ordem = 0;
}
