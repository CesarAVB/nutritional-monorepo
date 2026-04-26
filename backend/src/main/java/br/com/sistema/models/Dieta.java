package br.com.sistema.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_dietas")
public class Dieta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(length = 200)
    private String titulo;

    @Column(nullable = false)
    private LocalDate dataCriacao;

    @Column(length = 500)
    private String objetivo;

    private Integer kcalTotal;

    @Column(name = "proteinas_g", precision = 8, scale = 2)
    private BigDecimal proteinasG;

    @Column(name = "carboidratos_g", precision = 8, scale = 2)
    private BigDecimal carboidratosG;

    @Column(name = "gorduras_g", precision = 8, scale = 2)
    private BigDecimal gordurasG;

    @Column(precision = 4, scale = 2)
    private BigDecimal ingestaoAguaLitros;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @OneToMany(mappedBy = "dieta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordemExibicao ASC")
    private List<Refeicao> refeicoes = new ArrayList<>();

    @OneToMany(mappedBy = "dieta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SuplementoDieta> suplementos = new ArrayList<>();
}
