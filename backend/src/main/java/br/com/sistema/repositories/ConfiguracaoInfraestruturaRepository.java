package br.com.sistema.repositories;

import br.com.sistema.models.ConfiguracaoInfraestrutura;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfiguracaoInfraestruturaRepository extends JpaRepository<ConfiguracaoInfraestrutura, Long> {
    Optional<ConfiguracaoInfraestrutura> findFirstBy();
}
