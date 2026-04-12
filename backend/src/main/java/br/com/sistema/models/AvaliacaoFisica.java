package br.com.sistema.models;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "tbl_avaliacoes_fisicas")
public class AvaliacaoFisica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "consulta_id", nullable = false)
    private Consulta consulta;
    
    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double altura = 0.0;
    
    // ### Perímetros (cm)
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroOmbro = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroTorax = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroCintura = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroAbdominal = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroQuadril = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroBracoDireitoRelax = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroBracoDireitoContr = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroBracoEsquerdoRelax = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroBracoEsquerdoContr = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroAntebracoDireito = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroAntebracoEsquerdo = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroCoxa = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroCoxaDireita = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroCoxaEsquerda = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroPanturrilhaDireita = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double perimetroPanturrilhaEsquerda = 0.0;
    
    
    // ### Dobras Cutâneas (mm)
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double dobraTriceps = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double dobraPeito = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double dobraAxilarMedia = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double dobraSubescapular = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double dobraAbdominal = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double dobraSupraIliaca = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double dobraCoxa = 0.0;
    
    
    // ### Composição Corporal
    @Column(columnDefinition = "DECIMAL(6,2) DEFAULT 0")
    private Double pesoAtual = 0.0;
    
    @Column(columnDefinition = "DECIMAL(6,2) DEFAULT 0")
    private Double massaMagra = 0.0;
    
    @Column(columnDefinition = "DECIMAL(6,2) DEFAULT 0")
    private Double massaGorda = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double percentualGordura = 0.0;
    
    @Column(columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double imc = 0.0;
}