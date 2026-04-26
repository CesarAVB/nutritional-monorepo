package br.com.sistema.models;

import br.com.sistema.enums.ProvedorIA;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "tbl_configuracoes_ia")
public class ConfiguracaoIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProvedorIA provedor;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String apiKey;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(length = 500)
    private String baseUrl;

    @Column(columnDefinition = "TEXT")
    private String promptSistema;

    @Column(nullable = false)
    private Double temperaturaModelo;
}
