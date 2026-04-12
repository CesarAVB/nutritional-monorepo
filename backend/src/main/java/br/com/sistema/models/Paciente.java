package br.com.sistema.models;

import java.time.LocalDate;

import br.com.sistema.enums.Sexo;
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
@Table(name = "tbl_pacientes")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nomeCompleto;
    
    @Column(unique = true, nullable = false, length = 11, columnDefinition = "CHAR(11)")
    private String cpf;
    
    @Column(nullable = false)
    private LocalDate dataNascimento;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sexo sexo;
    
    @Column(columnDefinition = "TEXT")
    private String prontuario;
    
    @Column(length = 15)
    private String telefoneWhatsapp;
    
    private String email;
}