package br.com.sistema.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import br.com.sistema.enums.Sexo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacienteDTO {
    
    private Long id;
    
    @NotBlank(message = "Nome completo é obrigatório")
    private String nomeCompleto;
    
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
    private String cpf;
    
    @Past(message = "Data de nascimento deve ser no passado")
    private LocalDate dataNascimento;
    
    private Sexo sexo;
    
    // Prontuário médico / observações livres
    private String prontuario;
    
    @Pattern(regexp = "\\d{10,15}", message = "Telefone inválido")
    private String telefoneWhatsapp;
    
    @Email(message = "Email inválido")
    private String email;
    
    // Campos calculados
    private Integer totalConsultas;
    private LocalDateTime ultimaConsulta;
}