package br.com.sistema.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistema.dtos.ConsultaHojeDTO;
import br.com.sistema.dtos.DashboardStatsDTO;
import br.com.sistema.dtos.PacienteDTO;
import br.com.sistema.exceptions.BusinessException;
import br.com.sistema.models.Consulta;
import br.com.sistema.repositories.ConsultaRepository;
import br.com.sistema.repositories.PacienteRepository;
import br.com.sistema.repositories.QuestionarioEstiloVidaRepository;
import lombok.RequiredArgsConstructor;

/**
 * Agrega dados para o painel administrativo do sistema.
 * Fornece estatisticas, consultas do dia e pacientes recentes para exibicao no dashboard.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;
    private final PacienteService pacienteService;
    private final QuestionarioEstiloVidaRepository questionarioRepository;

    /**
     * Retorna estatisticas gerais para o dashboard:
     * total de pacientes, consultas hoje, consultas no mes e proxima consulta agendada.
     * Lanca excecao amigavel se nao houver pacientes cadastrados.
     *
     * @return Estatisticas consolidadas para exibicao
     */
    @Transactional(readOnly = true)
    public DashboardStatsDTO buscarEstatisticas() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        long totalPacientes = pacienteRepository.count();

        if (totalPacientes == 0) {
            throw new BusinessException("Ainda nao ha pacientes cadastrados no sistema");
        }

        stats.setTotalPacientes(totalPacientes);

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);
        stats.setConsultasHoje(consultaRepository.countByDataConsultaBetween(inicioDia, fimDia));

        YearMonth mesAtual = YearMonth.now();
        LocalDateTime inicioMes = mesAtual.atDay(1).atStartOfDay();
        LocalDateTime fimMes = mesAtual.atEndOfMonth().atTime(LocalTime.MAX);
        stats.setConsultasMes(consultaRepository.countByDataConsultaBetween(inicioMes, fimMes));

        LocalDateTime agora = LocalDateTime.now();
        consultaRepository.findFirstByDataConsultaAfterOrderByDataConsultaAsc(agora).ifPresentOrElse(
                consulta -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    stats.setProximaConsulta(consulta.getDataConsulta().format(formatter));
                },
                () -> stats.setProximaConsulta("-")
        );

        return stats;
    }

    /**
     * Retorna consultas agendadas para o dia atual ordenadas por horario.
     * Lanca excecao amigavel se nenhuma consulta estiver agendada.
     *
     * @return Lista de consultas de hoje com objetivo do questionario
     */
    @Transactional(readOnly = true)
    public List<ConsultaHojeDTO> buscarConsultasHoje() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);

        List<Consulta> consultas = consultaRepository.findByDataConsultaBetweenOrderByDataConsultaAsc(
                inicioDia, fimDia
        );

        if (consultas.isEmpty()) {
            throw new BusinessException("Nenhuma consulta agendada para hoje");
        }

        return consultas.stream().map(this::converterParaConsultaHojeDTO).toList();
    }

    /**
     * Retorna pacientes recentemente cadastrados ordenados por ID decrescente.
     * Lanca excecao amigavel se nenhum paciente for encontrado.
     *
     * @param limite Numero maximo de pacientes a retornar
     * @return Lista de pacientes recentes
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> buscarPacientesRecentes(int limite) {
        PageRequest pageRequest = PageRequest.of(0, limite, Sort.by("id").descending());

        List<PacienteDTO> pacientes = pacienteRepository.findAll(pageRequest).stream()
                .map(pacienteService::converterParaDTO).toList();

        if (pacientes.isEmpty()) {
            throw new BusinessException("Nenhum paciente encontrado nos registros");
        }

        return pacientes;
    }

    /**
     * Converte entidade Consulta para DTO de exibicao no dashboard de hoje.
     * Extrai iniciais do nome e objetivo do questionario quando disponivel.
     */
    private ConsultaHojeDTO converterParaConsultaHojeDTO(Consulta consulta) {
        ConsultaHojeDTO dto = new ConsultaHojeDTO();
        dto.setId(consulta.getId());
        dto.setPacienteId(consulta.getPaciente().getId());
        dto.setNomePaciente(consulta.getPaciente().getNomeCompleto());
        dto.setIniciais(getIniciais(consulta.getPaciente().getNomeCompleto()));
        dto.setHorario(consulta.getDataConsulta());

        questionarioRepository.findByConsultaId(consulta.getId())
                .ifPresent(questionario -> dto.setObjetivo(questionario.getObjetivo()));

        return dto;
    }

    /**
     * Gera iniciais do nome do paciente.
     * Retorna NN se nome vazio, 2 primeiras letras se mononimo,
     * primeira e ultima letra se nome composto.
     */
    private String getIniciais(String nome) {
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 0) return "NN";
        if (partes.length == 1) return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        return (partes[0].charAt(0) + "" + partes[partes.length - 1].charAt(0)).toUpperCase();
    }
}
