package br.com.sistema.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sistema.models.ConfiguracaoAgendamento;

@Repository
public interface ConfiguracaoAgendamentoRepository extends JpaRepository<ConfiguracaoAgendamento, Long> {
    // Singleton - busca o único registro de configuração
}
