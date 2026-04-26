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
@Table(name = "tbl_alimentos")
public class TacoAlimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Column(nullable = false, unique = true, name = "codigo_taco")
    private Integer codigoTaco;

    @Column(nullable = false, length = 100)
    private String grupo;

    @Column(nullable = false, length = 300)
    private String descricao;

    @Column(precision = 6, scale = 2)
    private BigDecimal umidade;

    @Column(precision = 8, scale = 2, name = "energia_kcal")
    private BigDecimal energiaKcal;

    @Column(precision = 8, scale = 2, name = "energia_kj")
    private BigDecimal energiaKj;

    @Column(precision = 6, scale = 2, name = "proteina_g")
    private BigDecimal proteinaG;

    @Column(precision = 6, scale = 2, name = "lipideos_g")
    private BigDecimal lipideosG;

    @Column(precision = 6, scale = 2, name = "colesterol_mg")
    private BigDecimal colesterolMg;

    @Column(precision = 6, scale = 2, name = "carboidratos_g")
    private BigDecimal carboidratosG;

    @Column(precision = 6, scale = 2, name = "fibra_alimentar_g")
    private BigDecimal fibraAlimentarG;

    @Column(precision = 6, scale = 2, name = "cinzas_g")
    private BigDecimal cinzasG;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(length = 100)
    private String fonte;
}
