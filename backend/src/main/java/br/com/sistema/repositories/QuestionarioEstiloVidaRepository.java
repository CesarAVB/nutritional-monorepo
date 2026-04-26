package br.com.sistema.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sistema.models.QuestionarioEstiloVida;
import jakarta.transaction.Transactional;

@Repository
public interface QuestionarioEstiloVidaRepository extends JpaRepository<QuestionarioEstiloVida, Long> {

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tbl_questionarios_estilo_vida WHERE consulta_id = ?1", nativeQuery = true)
	void deleteByConsultaId(Long consultaId);

	@Query(value = "SELECT * FROM tbl_questionarios_estilo_vida WHERE consulta_id = ?1", nativeQuery = true)
	Optional<QuestionarioEstiloVida> findByConsultaId(Long consultaId);

	List<QuestionarioEstiloVida> findAllByConsultaIdIn(List<Long> consultaIds);

	@Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM tbl_questionarios_estilo_vida WHERE consulta_id = ?1", nativeQuery = true)
	boolean existsByConsultaId(Long consultaId);

	// Batch: retorna apenas os IDs de consultas que possuem questionário
	@Query("SELECT q.consulta.id FROM QuestionarioEstiloVida q WHERE q.consulta.id IN :ids")
	List<Long> findConsultaIdsComQuestionario(@Param("ids") List<Long> ids);
}