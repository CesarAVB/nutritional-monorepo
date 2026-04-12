package br.com.sistema.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sistema.models.Paciente;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    
	// Método de busca personalizada para encontrar um paciente por CPF
	Optional<Paciente> findByCpf(String cpf);
    
	// Método para verificar se um paciente com um determinado CPF já existe
	boolean existsByCpf(String cpf);	
    
	// Método de busca personalizada para encontrar pacientes por nome completo (ignora maiúsculas/minúsculas)
	List<Paciente> findByNomeCompletoContainingIgnoreCase(String nome);

	// Método de busca paginada por nome completo (ignora maiúsculas/minúsculas)
	Page<Paciente> findByNomeCompletoContainingIgnoreCase(String nome, Pageable pageable);
}