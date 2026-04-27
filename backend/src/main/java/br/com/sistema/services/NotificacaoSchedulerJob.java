package br.com.sistema.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.sistema.models.Agendamento;
import br.com.sistema.repositories.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Job agendado para processar envio de lembretes de agendamento.
 * Executa a cada minuto verificando agendamentos elegíveis para receber notificações programadas.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacaoSchedulerJob {

    private final AgendamentoRepository agendamentoRepository;
    private final NotificacaoAgendamentoService notificacaoService;

    /**
     * Executa a cada minuto. Busca agendamentos nas próximas 73h e processa lembretes pendentes.
     * Janela de busca captura os três tipos de lembretes: 72h, 24h e 2h.
     */
    @Scheduled(fixedDelay = 60_000)
    public void processarNotificacoes() {
        try {
            LocalDateTime agora = LocalDateTime.now();
            // Janela de 2 min a 73h — captura os três lembretes
            List<Agendamento> agendamentos = agendamentoRepository.findPendentesNotificacao(
                agora.plusMinutes(2),
                agora.plusHours(73)
            );
            if (!agendamentos.isEmpty()) {
                log.debug("Processando lembretes para {} agendamentos", agendamentos.size());
                notificacaoService.processarLembretes(agendamentos);
            }
        } catch (Exception ex) {
            log.error("Erro no job de notificações: {}", ex.getMessage(), ex);
        }
    }
}
