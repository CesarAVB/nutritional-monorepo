package br.com.sistema.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sistema.models.TacoAlimento;

@Repository
public interface TacoAlimentoRepository extends JpaRepository<TacoAlimento, Long> {

    List<TacoAlimento> findByGrupoOrderByDescricaoAsc(String grupo);

    @Query("SELECT t FROM TacoAlimento t WHERE t.grupo IN :grupos ORDER BY t.energiaKcal ASC")
    List<TacoAlimento> findByGrupoIn(@Param("grupos") List<String> grupos);

    List<TacoAlimento> findAllByOrderByGrupoAscDescricaoAsc();

    @Query("SELECT t FROM TacoAlimento t WHERE LOWER(t.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<TacoAlimento> findByDescricaoContaining(@Param("termo") String termo);

    @Query("""
        SELECT t FROM TacoAlimento t
        WHERE t.grupo IN :grupos
          AND NOT EXISTS (
            SELECT 1 FROM TacoAlimento t2
            WHERE t2.id = t.id
              AND LOWER(t2.descricao) LIKE LOWER(CONCAT('%', :exclusao, '%'))
          )
        ORDER BY t.energiaKcal ASC
        """)
    List<TacoAlimento> findByGrupoInExcluindoTermo(
            @Param("grupos") List<String> grupos,
            @Param("exclusao") String exclusao);
}
