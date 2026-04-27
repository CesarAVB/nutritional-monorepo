package br.com.sistema.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.sistema.models.Agendamento;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /**
     * Busca agendamentos que conflitam com o intervalo dado, excluindo um ID específico.
     * Utilizado para validar disponibilidade de horário ao criar ou editar agendamentos.
     *
     * @param inicio data e hora de início do intervalo
     * @param fim data e hora de fim do intervalo
     * @param excluirId ID do agendamento a ser excluído da verificação (null ao criar novo)
     * @return lista de agendamentos que conflitam com o intervalo
     */
    @Query("SELECT a FROM Agendamento a WHERE a.status NOT IN ('CANCELADO','FALTA') " +
           "AND a.dataHoraInicio < :fim AND a.dataHoraFim > :inicio " +
           "AND (:excluirId IS NULL OR a.id <> :excluirId)")
    List<Agendamento> findConflitos(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim,
        @Param("excluirId") Long excluirId
    );

    /**
     * Busca agendamentos ativos de um dia específico ordenados por horário.
     * Exclui agendamentos cancelados ou com falta para exibir apenas compromissos válidos.
     *
     * @param data data da busca
     * @return lista de agendamentos ativos do dia ordenados por horário de início
     */
    @Query("SELECT a FROM Agendamento a JOIN FETCH a.paciente " +
           "WHERE DATE(a.dataHoraInicio) = :data AND a.status NOT IN ('CANCELADO','FALTA') " +
           "ORDER BY a.dataHoraInicio ASC")
    List<Agendamento> findByDia(@Param("data") LocalDate data);

    /**
     * Busca todos os agendamentos de um dia específico, independente do status.
     * Utilizado para exibir a lista completa de agendamentos do dia incluindo cancelados.
     *
     * @param data data da busca
     * @return lista de todos os agendamentos do dia ordenados por horário de início
     */
    @Query("SELECT a FROM Agendamento a JOIN FETCH a.paciente " +
           "WHERE DATE(a.dataHoraInicio) = :data ORDER BY a.dataHoraInicio ASC")
    List<Agendamento> findTodosByDia(@Param("data") LocalDate data);

    /**
     * Conta agendamentos ativos de hoje para exibição em dashboard.
     *
     * @param hoje data de hoje
     * @return quantidade de agendamentos ativos
     */
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE DATE(a.dataHoraInicio) = :hoje " +
           "AND a.status NOT IN ('CANCELADO','FALTA')")
    long countHoje(@Param("hoje") LocalDate hoje);

    /**
     * Busca agendamentos pendentes de notificação em um intervalo de tempo.
     * Utilizado pelo scheduler para enviar lembretes programados.
     *
     * @param from início do intervalo
     * @param to fim do intervalo
     * @return lista de agendamentos pendentes no intervalo
     */
    @Query("SELECT a FROM Agendamento a JOIN FETCH a.paciente " +
           "WHERE a.dataHoraInicio BETWEEN :from AND :to " +
           "AND a.status NOT IN ('CANCELADO','FALTA','REALIZADO')")
    List<Agendamento> findPendentesNotificacao(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    /**
     * Busca próximo agendamento aguardando confirmação de um paciente pelo telefone WhatsApp.
     * Utilizado para processar confirmações e cancelamentos via webhook do WhatsApp.
     *
     * @param numero telefone WhatsApp do paciente
     * @param agora momento atual para filtrar apenas agendamentos futuros
     * @return lista de agendamentos aguardando confirmação ordenados por data
     */
    /**
     * Busca o agendamento realizado mais recente de um paciente para exibir última visita.
     *
     * @param pacienteId ID do paciente
     * @return agendamento mais recente com status REALIZADO
     */
    @Query("SELECT a FROM Agendamento a WHERE a.paciente.id = :pacienteId " +
           "AND a.status = 'REALIZADO' ORDER BY a.dataHoraInicio DESC LIMIT 1")
    Optional<Agendamento> findUltimaVisita(@Param("pacienteId") Long pacienteId);

    @Query("SELECT a FROM Agendamento a JOIN FETCH a.paciente p " +
           "WHERE p.telefoneWhatsapp = :numero " +
           "AND a.status = 'AGUARDANDO_CONFIRMACAO' " +
           "AND a.dataHoraInicio > :agora " +
           "ORDER BY a.dataHoraInicio ASC")
    List<Agendamento> findAguardandoConfirmacaoPorTelefone(
        @Param("numero") String numero,
        @Param("agora") LocalDateTime agora
    );
}
