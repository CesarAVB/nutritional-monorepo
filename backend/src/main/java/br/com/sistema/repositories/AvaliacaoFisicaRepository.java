package br.com.sistema.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sistema.models.AvaliacaoFisica;

@Repository
public interface AvaliacaoFisicaRepository extends JpaRepository<AvaliacaoFisica, Long> {

    @Query("SELECT a FROM AvaliacaoFisica a WHERE a.consulta.id = :consultaId")
    Optional<AvaliacaoFisica> findByConsultaId(Long consultaId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AvaliacaoFisica a WHERE a.consulta.id = :consultaId")
    boolean existsByConsultaId(Long consultaId);

    @Modifying
    @Query("DELETE FROM AvaliacaoFisica a WHERE a.consulta.id = :consultaId")
    void deleteByConsultaId(Long consultaId);

    Optional<AvaliacaoFisica> findFirstByConsultaId(Long consultaId);

    // Batch: retorna apenas os IDs de consultas que possuem avaliação física
    @Query("SELECT a.consulta.id FROM AvaliacaoFisica a WHERE a.consulta.id IN :ids")
    List<Long> findConsultaIdsComAvaliacao(@Param("ids") List<Long> ids);

}