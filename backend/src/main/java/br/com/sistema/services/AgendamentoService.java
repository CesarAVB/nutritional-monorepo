package br.com.sistema.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistema.dtos.AgendamentoDiaDto;
import br.com.sistema.dtos.AgendamentoRequestDto;
import br.com.sistema.dtos.AgendamentoResponseDto;
import br.com.sistema.dtos.AgendamentoSemanaResponseDto;
import br.com.sistema.dtos.ContadorHojeDto;
import br.com.sistema.enums.StatusAgendamento;
import br.com.sistema.exceptions.BusinessException;
import br.com.sistema.exceptions.ResourceNotFoundException;
import br.com.sistema.models.Agendamento;
import br.com.sistema.models.ConfiguracaoAgendamento;
import br.com.sistema.models.Paciente;
import br.com.sistema.repositories.AgendamentoRepository;
import br.com.sistema.repositories.ConfiguracaoAgendamentoRepository;
import br.com.sistema.repositories.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orquestra operações de agendamento de consultas.
 * Valida disponibilidade de horários, aplica regras de janela de atendimento
 * e gerencia o ciclo de vida dos agendamentos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ConfiguracaoAgendamentoRepository configuracaoRepository;
    private final PacienteRepository pacienteRepository;
    private final NotificacaoAgendamentoService notificacaoService;

    /**
     * Cria novo agendamento validando disponibilidade de horário e janela de atendimento.
     *
     * @param request dados do agendamento a ser criado
     * @return agendamento criado com status AGUARDANDO_CONFIRMACAO
     */
    @Transactional
    public AgendamentoResponseDto criar(AgendamentoRequestDto request) {
        Paciente paciente = resolverPacienteOptional(request)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        LocalDateTime dataHoraFim = request.getDataHoraInicio().plusMinutes(request.getDuracaoMinutos());

        ConfiguracaoAgendamento config = carregarConfiguracao();

        validarJanelaAtendimento(request.getDataHoraInicio(), config);

        List<Agendamento> conflitos = agendamentoRepository.findConflitos(
            request.getDataHoraInicio(),
            dataHoraFim,
            null
        );

        if (!conflitos.isEmpty()) {
            throw new BusinessException("Horário já ocupado por outro agendamento");
        }

        Agendamento agendamento = new Agendamento();
        agendamento.setPaciente(paciente);
        agendamento.setDataHoraInicio(request.getDataHoraInicio());
        agendamento.setDataHoraFim(dataHoraFim);
        agendamento.setDuracaoMinutos(request.getDuracaoMinutos());
        agendamento.setTipo(request.getTipo());
        agendamento.setStatus(StatusAgendamento.AGUARDANDO_CONFIRMACAO);
        agendamento.setObservacoes(request.getObservacoes());

        Agendamento saved = agendamentoRepository.save(agendamento);

        boolean notificacaoEnviada = true;
        try {
            notificacaoService.enviarNotificacaoImediata(saved);
        } catch (Exception ex) {
            log.warn("Falha ao enviar notificação imediata para agendamento {}: {}", saved.getId(), ex.getMessage());
            notificacaoEnviada = false;
        }

        AgendamentoResponseDto dto = toDto(saved);
        dto.setNotificacaoEnviada(notificacaoEnviada);
        return dto;
    }

    /**
     * Atualiza agendamento existente validando novamente disponibilidade.
     *
     * @param id identificador do agendamento
     * @param request novos dados do agendamento
     * @return agendamento atualizado
     */
    @Transactional
    public AgendamentoResponseDto atualizar(Long id, AgendamentoRequestDto request) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        Paciente paciente = resolverPacienteOptional(request)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        LocalDateTime dataHoraFim = request.getDataHoraInicio().plusMinutes(request.getDuracaoMinutos());

        ConfiguracaoAgendamento config = carregarConfiguracao();

        validarJanelaAtendimento(request.getDataHoraInicio(), config);

        List<Agendamento> conflitos = agendamentoRepository.findConflitos(
            request.getDataHoraInicio(),
            dataHoraFim,
            id
        );

        if (!conflitos.isEmpty()) {
            throw new BusinessException("Horário já ocupado por outro agendamento");
        }

        agendamento.setPaciente(paciente);
        agendamento.setDataHoraInicio(request.getDataHoraInicio());
        agendamento.setDataHoraFim(dataHoraFim);
        agendamento.setDuracaoMinutos(request.getDuracaoMinutos());
        agendamento.setTipo(request.getTipo());
        agendamento.setObservacoes(request.getObservacoes());

        Agendamento updated = agendamentoRepository.save(agendamento);

        return toDto(updated);
    }

    /**
     * Atualiza status de um agendamento.
     *
     * @param id identificador do agendamento
     * @param novoStatus novo status a ser aplicado
     * @return agendamento atualizado
     */
    @Transactional
    public AgendamentoResponseDto atualizarStatus(Long id, StatusAgendamento novoStatus) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        agendamento.setStatus(novoStatus);
        Agendamento updated = agendamentoRepository.save(agendamento);

        return toDto(updated);
    }

    /**
     * Cancela um agendamento alterando seu status para CANCELADO.
     *
     * @param id identificador do agendamento
     */
    @Transactional
    public void cancelar(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        Agendamento saved = agendamentoRepository.save(agendamento);

        try {
            notificacaoService.enviarNotificacaoCancelamento(saved);
        } catch (Exception ex) {
            log.warn("Falha ao enviar notificação de cancelamento para agendamento {}: {}", saved.getId(), ex.getMessage());
        }
    }

    /**
     * Busca agendamento por identificador.
     *
     * @param id identificador do agendamento
     * @return dados completos do agendamento
     */
    @Transactional(readOnly = true)
    public AgendamentoResponseDto buscarPorId(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        return toDto(agendamento);
    }

    /**
     * Lista todos os agendamentos de um dia específico.
     *
     * @param data data da busca
     * @return lista de agendamentos do dia
     */
    @Transactional(readOnly = true)
    public List<AgendamentoResponseDto> listarPorDia(LocalDate data) {
        List<Agendamento> agendamentos = agendamentoRepository.findTodosByDia(data);
        return agendamentos.stream().map(this::toDto).toList();
    }

    /**
     * Lista agendamentos de uma semana completa a partir de uma data de referência.
     * Semana começa na segunda-feira da semana da data informada.
     *
     * @param dataReferencia data de referência (null = hoje)
     * @return estrutura com 7 dias de agendamentos
     */
    @Transactional(readOnly = true)
    public AgendamentoSemanaResponseDto listarSemana(LocalDate dataReferencia) {
        LocalDate data = dataReferencia != null ? dataReferencia : LocalDate.now();
        LocalDate dataInicio = data.with(DayOfWeek.MONDAY);
        LocalDate dataFim = dataInicio.plusDays(6);

        List<AgendamentoDiaDto> dias = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate diaAtual = dataInicio.plusDays(i);
            List<Agendamento> agendamentosDia = agendamentoRepository.findByDia(diaAtual);
            List<AgendamentoResponseDto> dtos = agendamentosDia.stream().map(this::toDto).toList();
            dias.add(new AgendamentoDiaDto(diaAtual, dtos));
        }

        return new AgendamentoSemanaResponseDto(dataInicio, dataFim, dias);
    }

    /**
     * Calcula slots de horário disponíveis para agendamento em uma data.
     * Considera configuração de horário de atendimento e agendamentos já existentes.
     *
     * @param data data para verificação de disponibilidade
     * @return lista de horários disponíveis
     */
    @Transactional(readOnly = true)
    public List<LocalTime> calcularSlotsDisponiveis(LocalDate data) {
        ConfiguracaoAgendamento config = carregarConfiguracao();

        Map<DayOfWeek, String> mapa = Map.of(
            DayOfWeek.MONDAY, "SEG", DayOfWeek.TUESDAY, "TER",
            DayOfWeek.WEDNESDAY, "QUA", DayOfWeek.THURSDAY, "QUI",
            DayOfWeek.FRIDAY, "SEX", DayOfWeek.SATURDAY, "SAB",
            DayOfWeek.SUNDAY, "DOM"
        );

        String diaStr = mapa.get(data.getDayOfWeek());
        List<String> diasPermitidos = Arrays.asList(config.getDiasAtendimento().split(","));

        if (!diasPermitidos.contains(diaStr)) {
            return List.of();
        }

        int intervalo = config.getIntervaloEntreConsultasMinutos();
        if (intervalo <= 0) {
            intervalo = config.getDuracaoPadraoMinutos();
        }
        if (intervalo <= 0) {
            return List.of();
        }

        List<LocalTime> slots = new ArrayList<>();
        LocalTime horarioAtual = config.getHorarioInicio();
        LocalTime limite = config.getHorarioFim().minusMinutes(config.getDuracaoPadraoMinutos());

        // intervalo > 0 garante avanço; cap de 200 slots evita loop infinito por config corrompida
        int maxSlots = 200;
        while (!horarioAtual.isAfter(limite) && slots.size() < maxSlots) {
            slots.add(horarioAtual);
            horarioAtual = horarioAtual.plusMinutes(intervalo);
        }

        List<Agendamento> agendamentosDia = agendamentoRepository.findByDia(data);

        List<LocalTime> slotsDisponiveis = new ArrayList<>();

        for (LocalTime slot : slots) {
            LocalDateTime inicio = LocalDateTime.of(data, slot);
            LocalDateTime fim = inicio.plusMinutes(config.getDuracaoPadraoMinutos());

            boolean ocupado = agendamentosDia.stream().anyMatch(a ->
                a.getDataHoraInicio().isBefore(fim) && a.getDataHoraFim().isAfter(inicio)
            );

            if (!ocupado) {
                slotsDisponiveis.add(slot);
            }
        }

        return slotsDisponiveis;
    }

    /**
     * Conta agendamentos ativos de hoje para dashboard.
     *
     * @return contador com quantidade de agendamentos
     */
    @Transactional(readOnly = true)
    public ContadorHojeDto contarHoje() {
        long quantidade = agendamentoRepository.countHoje(LocalDate.now());
        return new ContadorHojeDto(quantidade);
    }

    /**
     * Confirma agendamento de um paciente via WhatsApp.
     * Busca próximo agendamento aguardando confirmação do número informado.
     *
     * @param numeroPaciente telefone WhatsApp do paciente
     * @return agendamento confirmado
     */
    @Transactional
    public AgendamentoResponseDto confirmarPorWhatsapp(String numeroPaciente) {
        List<Agendamento> agendamentos = agendamentoRepository.findAguardandoConfirmacaoPorTelefone(
            numeroPaciente,
            LocalDateTime.now()
        );

        if (agendamentos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum agendamento pendente encontrado para este número");
        }

        Agendamento agendamento = agendamentos.get(0);
        agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        Agendamento updated = agendamentoRepository.save(agendamento);

        return toDto(updated);
    }

    /**
     * Cancela agendamento de um paciente via WhatsApp.
     * Busca próximo agendamento aguardando confirmação do número informado.
     *
     * @param numeroPaciente telefone WhatsApp do paciente
     * @return agendamento cancelado
     */
    @Transactional
    public AgendamentoResponseDto cancelarPorWhatsapp(String numeroPaciente) {
        List<Agendamento> agendamentos = agendamentoRepository.findAguardandoConfirmacaoPorTelefone(
            numeroPaciente,
            LocalDateTime.now()
        );

        if (agendamentos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum agendamento pendente encontrado para este número");
        }

        Agendamento agendamento = agendamentos.get(0);
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        Agendamento updated = agendamentoRepository.save(agendamento);

        return toDto(updated);
    }

    /**
     * Carrega configuração singleton de agendamento.
     *
     * @return configuração de agendamento
     */
    private Optional<Paciente> resolverPacienteOptional(AgendamentoRequestDto request) {
        if (request.getPacienteId() != null) {
            return pacienteRepository.findById(request.getPacienteId());
        }

        validarDadosPacienteRapido(request);

        Paciente paciente = new Paciente();
        paciente.setNomeCompleto(request.getNomePaciente().trim());
        paciente.setTelefoneWhatsapp(somenteDigitos(request.getTelefoneWhatsapp()));
        paciente.setCadastroIncompleto(true);
        paciente.setOrigemAgendamento(true);

        return Optional.of(pacienteRepository.save(paciente));
    }

    private void validarDadosPacienteRapido(AgendamentoRequestDto request) {
        if (request.getNomePaciente() == null || request.getNomePaciente().trim().length() < 3) {
            throw new BusinessException("Informe o nome do paciente para criar o agendamento");
        }

        String telefone = somenteDigitos(request.getTelefoneWhatsapp());
        if (telefone.length() < 10 || telefone.length() > 15) {
            throw new BusinessException("Informe um telefone valido para criar o agendamento");
        }
    }

    private String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private ConfiguracaoAgendamento carregarConfiguracao() {
        return configuracaoRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Configuração de agendamento não encontrada"));
    }

    /**
     * Valida se data e hora estão dentro da janela de atendimento configurada.
     * Verifica dia da semana permitido e horário dentro do intervalo.
     *
     * @param dataHora data e hora a validar
     * @param config configuração de agendamento
     */
    private void validarJanelaAtendimento(LocalDateTime dataHora, ConfiguracaoAgendamento config) {
        Map<DayOfWeek, String> mapa = Map.of(
            DayOfWeek.MONDAY, "SEG", DayOfWeek.TUESDAY, "TER",
            DayOfWeek.WEDNESDAY, "QUA", DayOfWeek.THURSDAY, "QUI",
            DayOfWeek.FRIDAY, "SEX", DayOfWeek.SATURDAY, "SAB",
            DayOfWeek.SUNDAY, "DOM"
        );

        String diaStr = mapa.get(dataHora.getDayOfWeek());
        List<String> diasPermitidos = Arrays.asList(config.getDiasAtendimento().split(","));

        if (!diasPermitidos.contains(diaStr)) {
            throw new BusinessException("Dia da semana fora do horário de atendimento");
        }

        LocalTime hora = dataHora.toLocalTime();
        if (hora.isBefore(config.getHorarioInicio()) || hora.isAfter(config.getHorarioFim())) {
            throw new BusinessException("Horário fora da janela de atendimento configurada");
        }
    }

    /**
     * Converte entidade Agendamento para DTO de resposta.
     *
     * @param a agendamento
     * @return DTO de resposta
     */
    private AgendamentoResponseDto toDto(Agendamento a) {
        AgendamentoResponseDto dto = new AgendamentoResponseDto();
        dto.setId(a.getId());
        dto.setPacienteId(a.getPaciente().getId());
        dto.setNomePaciente(a.getPaciente().getNomeCompleto());
        dto.setTelefoneWhatsapp(a.getPaciente().getTelefoneWhatsapp());
        dto.setDataHoraInicio(a.getDataHoraInicio());
        dto.setDataHoraFim(a.getDataHoraFim());
        dto.setDuracaoMinutos(a.getDuracaoMinutos());
        dto.setTipo(a.getTipo());
        dto.setStatus(a.getStatus());
        dto.setObservacoes(a.getObservacoes());
        dto.setCriadoEm(a.getCriadoEm());
        return dto;
    }
}
