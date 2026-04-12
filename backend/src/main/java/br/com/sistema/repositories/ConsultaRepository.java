package br.com.sistema.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import br.com.sistema.models.Consulta;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // Listar consultas de um paciente em ordem decrescente
    List<Consulta> findByPacienteIdOrderByDataConsultaDesc(Long pacienteId);

    // Buscar a consulta mais recente de um paciente
    Optional<Consulta> findFirstByPacienteIdOrderByDataConsultaDesc(Long pacienteId);

    // Contar consultas de um paciente
    @Query("SELECT COUNT(c) FROM Consulta c WHERE c.paciente.id = :pacienteId")
    Long countByPacienteId(Long pacienteId);

    // Buscar a primeira consulta após uma data
    Optional<Consulta> findFirstByDataConsultaAfterOrderByDataConsultaAsc(LocalDateTime dataConsulta);

    // Contar consultas entre duas datas
    Long countByDataConsultaBetween(LocalDateTime inicio, LocalDateTime fim);

    // Buscar consultas entre duas datas em ordem crescente
    List<Consulta> findByDataConsultaBetweenOrderByDataConsultaAsc(LocalDateTime inicio, LocalDateTime fim);

    // Listar todas as consultas em ordem decrescente
    List<Consulta> findAllByOrderByDataConsultaDesc();

    // Listar consultas de um paciente em ordem decrescente (paginado)
    Page<Consulta> findByPacienteIdOrderByDataConsultaDesc(Long pacienteId, Pageable pageable);

    // Listar todas as consultas em ordem decrescente (paginado)
    Page<Consulta> findAllByOrderByDataConsultaDesc(Pageable pageable);
}