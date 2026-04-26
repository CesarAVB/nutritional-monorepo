package br.com.sistema.models;

import java.math.BigDecimal;

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
@Table(name = "tbl_taco_alimentos")
public class TacoAlimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer codigoTaco;

    @Column(nullable = false, length = 100)
    private String grupo;

    @Column(nullable = false, length = 300)
    private String descricao;

    @Column(precision = 6, scale = 2)
    private BigDecimal umidade;

    @Column(precision = 8, scale = 2)
    private BigDecimal energiaKcal;

    @Column(precision = 8, scale = 2)
    private BigDecimal energiaKj;

    @Column(precision = 6, scale = 2)
    private BigDecimal proteinaG;

    @Column(precision = 6, scale = 2)
    private BigDecimal lipideosG;

    @Column(precision = 6, scale = 2)
    private BigDecimal colesterolMg;

    @Column(precision = 6, scale = 2)
    private BigDecimal carboidratosG;

    @Column(precision = 6, scale = 2)
    private BigDecimal fibraAlimentarG;

    @Column(precision = 6, scale = 2)
    private BigDecimal cinzasG;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(length = 100)
    private String fonte;
}
