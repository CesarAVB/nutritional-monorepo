package br.com.sistema.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sistema.models.UsoIALog;

@Repository
public interface UsoIALogRepository extends JpaRepository<UsoIALog, Long> {

    @Query("SELECT COALESCE(SUM(u.custoUsd), 0) FROM UsoIALog u")
    BigDecimal sumCustoTotal();

    @Query("SELECT COALESCE(SUM(u.custoUsd), 0) FROM UsoIALog u WHERE u.dataHora >= :desde")
    BigDecimal sumCustoDesde(@Param("desde") LocalDateTime desde);

    @Query("SELECT COALESCE(SUM(u.tokensEntrada + u.tokensSaida), 0) FROM UsoIALog u")
    long sumTokensTotal();
}
