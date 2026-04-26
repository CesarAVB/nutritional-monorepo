package br.com.sistema.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sistema.models.Dieta;

@Repository
public interface DietaRepository extends JpaRepository<Dieta, Long> {

    List<Dieta> findByPacienteIdOrderByDataCriacaoDesc(Long pacienteId);
}
