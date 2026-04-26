package br.com.sistema.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sistema.models.ConfiguracaoIA;

@Repository
public interface ConfiguracaoIARepository extends JpaRepository<ConfiguracaoIA, Long> {

    Optional<ConfiguracaoIA> findFirstByOrderByIdAsc();
}
