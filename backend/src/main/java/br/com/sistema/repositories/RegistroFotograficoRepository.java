package br.com.sistema.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sistema.models.QuestionarioEstiloVida;
import br.com.sistema.models.RegistroFotografico;

@Repository
public interface RegistroFotograficoRepository extends JpaRepository<RegistroFotografico, Long> {
    
    @Query("SELECT r FROM RegistroFotografico r WHERE r.consulta.id = :consultaId")
    Optional<RegistroFotografico> findByConsultaId(Long consultaId);
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RegistroFotografico r WHERE r.consulta.id = :consultaId")
    boolean existsByConsultaId(Long consultaId);
    
    @Modifying
    @Query("DELETE FROM RegistroFotografico r WHERE r.consulta.id = :consultaId")
    void deleteByConsultaId(Long consultaId);
    
    @Query("SELECT r.consulta.id, COUNT(r) > 0 FROM RegistroFotografico r WHERE r.consulta.id IN :ids GROUP BY r.consulta.id")
    Map<Long, Boolean> existsByConsultaIdIn(@Param("ids") List<Long> ids);
    
    Optional<QuestionarioEstiloVida> findFirstByConsultaId(Long consultaId);

}