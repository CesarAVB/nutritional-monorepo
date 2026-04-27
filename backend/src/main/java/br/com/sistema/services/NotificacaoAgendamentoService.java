package br.com.sistema.services;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistema.enums.CanalNotificacao;
import br.com.sistema.enums.StatusAgendamento;
import br.com.sistema.enums.StatusNotificacao;
import br.com.sistema.enums.TipoNotificacao;
import br.com.sistema.models.Agendamento;
import br.com.sistema.models.ConfiguracaoAgendamento;
import br.com.sistema.models.NotificacaoAgendamento;
import br.com.sistema.repositories.NotificacaoAgendamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orquestra envio de notificações de agendamento via WhatsApp e Email.
 * Gerencia lembretes programados, alertas ao nutricionista e notificações de confirmação/cancelamento.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacaoAgendamentoService {

    private final NotificacaoAgendamentoRepository notificacaoRepository;
    private final EvolutionApiService evolutionApiService;
    private final EmailService emailService;
    private final ConfiguracaoAgendamentoService configuracaoService;

    /**
     * Envia notificação WhatsApp + Email imediata ao criar agendamento.
     * Registra resultado em tbl_notificacao_agendamento.
     *
     * @param agendamento agendamento criado que deve receber notificação
     */
    @Transactional
    public void enviarNotificacaoImediata(Agendamento agendamento) {
        if (jaEnviado(agendamento.getId(), TipoNotificacao.IMEDIATO)) return;

        ConfiguracaoAgendamento config = configuracaoService.buscarParaUso();
        if (!Boolean.TRUE.equals(config.getLembreteImediatoAtivo())) return;
        if (!estaJanelaAtiva(config)) return;

        String mensagem = substituirVariaveis(config.getTemplateConfirmacao(), agendamento, config);
        if (mensagem == null || mensagem.isBlank()) {
            mensagem = "Seu agendamento foi confirmado para " + agendamento.getDataHoraInicio().toLocalDate()
                + " às " + agendamento.getDataHoraInicio().toLocalTime();
        }

        enviarERegistrar(agendamento, TipoNotificacao.IMEDIATO, CanalNotificacao.WHATSAPP,
            agendamento.getPaciente().getTelefoneWhatsapp(), mensagem, config);
    }

    /**
     * Envia notificação de cancelamento ao paciente.
     *
     * @param agendamento agendamento cancelado
     */
    @Transactional
    public void enviarNotificacaoCancelamento(Agendamento agendamento) {
        ConfiguracaoAgendamento config = configuracaoService.buscarParaUso();
        if (!Boolean.TRUE.equals(config.getNotifAoCancelar())) return;

        String mensagem = substituirVariaveis(config.getTemplateConsultaCancelada(), agendamento, config);
        if (mensagem == null || mensagem.isBlank()) {
            mensagem = "Seu agendamento de " + agendamento.getDataHoraInicio().toLocalDate() + " foi cancelado.";
        }

        enviarERegistrar(agendamento, TipoNotificacao.IMEDIATO, CanalNotificacao.WHATSAPP,
            agendamento.getPaciente().getTelefoneWhatsapp(), mensagem, config);
    }

    /**
     * Processa lembretes pendentes chamado pelo scheduler.
     * Verifica lembretes de 72h, 24h e 2h antes dos agendamentos.
     *
     * @param agendamentos lista de agendamentos elegíveis para receber lembretes
     */
    @Transactional
    public void processarLembretes(List<Agendamento> agendamentos) {
        if (agendamentos.isEmpty()) return;

        ConfiguracaoAgendamento config = configuracaoService.buscarParaUso();
        if (!estaJanelaAtiva(config)) return;

        LocalDateTime agora = LocalDateTime.now();

        for (Agendamento ag : agendamentos) {
            long minutosAte = java.time.Duration.between(agora, ag.getDataHoraInicio()).toMinutes();

            // Lembrete 72h (entre 71h e 73h)
            if (Boolean.TRUE.equals(config.getLembrete72hAtivo())
                    && minutosAte >= 71 * 60 && minutosAte <= 73 * 60
                    && !jaEnviado(ag.getId(), TipoNotificacao.LEMBRETE_72H)) {
                String msg = substituirVariaveis(config.getTemplateLembrete72h(), ag, config);
                enviarERegistrar(ag, TipoNotificacao.LEMBRETE_72H, CanalNotificacao.WHATSAPP,
                    ag.getPaciente().getTelefoneWhatsapp(), msg, config);
            }

            // Lembrete 24h (entre 23h e 25h)
            if (Boolean.TRUE.equals(config.getLembrete24hAtivo())
                    && minutosAte >= 23 * 60 && minutosAte <= 25 * 60
                    && !jaEnviado(ag.getId(), TipoNotificacao.LEMBRETE_24H)) {
                String msg = substituirVariaveis(config.getTemplateLembrete24h(), ag, config);
                enviarERegistrar(ag, TipoNotificacao.LEMBRETE_24H, CanalNotificacao.WHATSAPP,
                    ag.getPaciente().getTelefoneWhatsapp(), msg, config);

                // Alerta ao nutricionista se não confirmado
                if (Boolean.TRUE.equals(config.getAlertaNaoConfirmacao())
                        && ag.getStatus() == StatusAgendamento.AGUARDANDO_CONFIRMACAO
                        && config.getEmailNotificacao() != null) {
                    String alerta = "Paciente " + ag.getPaciente().getNomeCompleto()
                        + " ainda não confirmou a consulta de "
                        + ag.getDataHoraInicio().toLocalDate() + " às "
                        + ag.getDataHoraInicio().toLocalTime();
                    enviarERegistrar(ag, TipoNotificacao.ALERTA_NUTRICIONISTA, CanalNotificacao.EMAIL,
                        config.getEmailNotificacao(), alerta, config);
                }
            }

            // Lembrete 2h (entre 1h55 e 2h05)
            if (Boolean.TRUE.equals(config.getLembrete2hAtivo())
                    && minutosAte >= 115 && minutosAte <= 125
                    && !jaEnviado(ag.getId(), TipoNotificacao.LEMBRETE_2H)) {
                String msg = substituirVariaveis(config.getTemplateLembrete2h(), ag, config);
                enviarERegistrar(ag, TipoNotificacao.LEMBRETE_2H, CanalNotificacao.WHATSAPP,
                    ag.getPaciente().getTelefoneWhatsapp(), msg, config);
            }
        }
    }

    /**
     * Verifica se o horário atual está dentro da janela de envio de notificações.
     * Respeita configuração de horário de início e fim para não enviar mensagens fora do expediente.
     */
    private boolean estaJanelaAtiva(ConfiguracaoAgendamento config) {
        if (config.getJanelaNotifInicio() == null || config.getJanelaNotifFim() == null) return true;
        LocalTime agora = LocalTime.now();
        return !agora.isBefore(config.getJanelaNotifInicio()) && !agora.isAfter(config.getJanelaNotifFim());
    }

    /**
     * Verifica se já existe notificação enviada de determinado tipo para evitar duplicatas.
     */
    private boolean jaEnviado(Long agendamentoId, TipoNotificacao tipo) {
        return notificacaoRepository.existsByAgendamentoIdAndTipoAndStatus(
            agendamentoId, tipo, StatusNotificacao.ENVIADO);
    }

    /**
     * Substitui variáveis dinâmicas no template:
     * {{paciente_nome}}, {{data_consulta}}, {{hora_consulta}}, {{tipo_consulta}},
     * {{nutricionista_nome}} (fixo: "sua nutricionista"), {{link_reagendamento}} (fixo: vazio por ora)
     */
    private String substituirVariaveis(String template, Agendamento ag, ConfiguracaoAgendamento config) {
        if (template == null) return "";
        DateTimeFormatter dataFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("HH:mm");
        return template
            .replace("{{paciente_nome}}", ag.getPaciente().getNomeCompleto())
            .replace("{{nutricionista_nome}}", "sua nutricionista")
            .replace("{{data_consulta}}", ag.getDataHoraInicio().toLocalDate().format(dataFmt))
            .replace("{{hora_consulta}}", ag.getDataHoraInicio().toLocalTime().format(horaFmt))
            .replace("{{tipo_consulta}}", ag.getTipo().name().replace("_", " ").toLowerCase())
            .replace("{{link_reagendamento}}", "");
    }

    /**
     * Envia notificação via canal especificado (WhatsApp ou Email) e registra resultado em banco.
     * Falhas de envio são registradas como FALHOU sem propagar exceção.
     */
    private void enviarERegistrar(Agendamento agendamento, TipoNotificacao tipo,
            CanalNotificacao canal, String destino, String mensagem, ConfiguracaoAgendamento config) {

        NotificacaoAgendamento notif = new NotificacaoAgendamento();
        notif.setAgendamento(agendamento);
        notif.setTipo(tipo);
        notif.setCanal(canal);
        notif.setMensagem(mensagem);
        notif.setStatus(StatusNotificacao.PENDENTE);

        boolean sucesso;
        if (canal == CanalNotificacao.WHATSAPP) {
            sucesso = evolutionApiService.enviarMensagem(
                config.getEvolutionUrl(), config.getEvolutionInstancia(),
                config.getEvolutionApiKey(), destino, mensagem);
        } else {
            sucesso = emailService.enviarEmail(destino, "Lembrete de consulta", mensagem);
        }

        notif.setStatus(sucesso ? StatusNotificacao.ENVIADO : StatusNotificacao.FALHOU);
        notif.setEnviadoEm(LocalDateTime.now());
        notificacaoRepository.save(notif);
    }
}
