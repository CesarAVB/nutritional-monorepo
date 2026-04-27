package br.com.sistema.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sistema.enums.StatusNotificacao;
import br.com.sistema.enums.TipoNotificacao;
import br.com.sistema.models.NotificacaoAgendamento;

/**
 * Repositório para persistir histórico de notificações de agendamento.
 * Fornece consultas para verificar duplicatas e listar notificações por agendamento.
 */
@Repository
public interface NotificacaoAgendamentoRepository extends JpaRepository<NotificacaoAgendamento, Long> {

    /**
     * Verifica se já existe notificação enviada de determinado tipo para um agendamento.
     * Utilizado para evitar reenvio duplicado de lembretes e alertas.
     *
     * @param agendamentoId identificador do agendamento
     * @param tipo tipo da notificação (LEMBRETE_72H, LEMBRETE_24H, etc.)
     * @param status status da notificação (ENVIADO)
     * @return true se já existe notificação enviada deste tipo
     */
    boolean existsByAgendamentoIdAndTipoAndStatus(
        Long agendamentoId,
        TipoNotificacao tipo,
        StatusNotificacao status
    );

    /**
     * Lista todas as notificações de um agendamento ordenadas por data de envio.
     *
     * @param agendamentoId identificador do agendamento
     * @return lista de notificações ordenadas da mais recente para a mais antiga
     */
    List<NotificacaoAgendamento> findByAgendamentoIdOrderByEnviadoEmDesc(Long agendamentoId);
}
