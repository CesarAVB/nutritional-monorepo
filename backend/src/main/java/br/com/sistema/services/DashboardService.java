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

@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;
    private final PacienteService pacienteService;
    private final QuestionarioEstiloVidaRepository questionarioRepository;
    
    
    // ==============================================
    // # Método - buscarEstatisticas
    // # Retorna estatísticas para o dashboard (pacientes, consultas, próxima consulta)
    // ==============================================
    @Transactional(readOnly = true)
    public DashboardStatsDTO buscarEstatisticas() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        long totalPacientes = pacienteRepository.count();
        
        // Se não houver pacientes, lança exceção amigável
        if (totalPacientes == 0) {
            throw new BusinessException("Ainda não há pacientes cadastrados no sistema");
        }
        
        // Total de pacientes
        stats.setTotalPacientes(totalPacientes);
        
        // Consultas hoje
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);
        stats.setConsultasHoje(consultaRepository.countByDataConsultaBetween(inicioDia, fimDia));
        
        // Consultas do mês atual
        YearMonth mesAtual = YearMonth.now();
        LocalDateTime inicioMes = mesAtual.atDay(1).atStartOfDay();
        LocalDateTime fimMes = mesAtual.atEndOfMonth().atTime(LocalTime.MAX);
        stats.setConsultasMes(consultaRepository.countByDataConsultaBetween(inicioMes, fimMes));
        
        // Próxima consulta
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
    
    // ==============================================
    // # Método - buscarConsultasHoje
    // # Retorna as consultas agendadas para hoje
    // ==============================================
    @Transactional(readOnly = true)
    public List<ConsultaHojeDTO> buscarConsultasHoje() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);
        
        List<Consulta> consultas = consultaRepository.findByDataConsultaBetweenOrderByDataConsultaAsc(
            inicioDia, fimDia
        );
        
        // Se não houver consultas hoje, lança exceção amigável
        if (consultas.isEmpty()) {
            throw new BusinessException("Nenhuma consulta agendada para hoje");
        }
        
        return consultas.stream().map(this::converterParaConsultaHojeDTO).toList();
    }
    
    // ==============================================
    // # Método - buscarPacientesRecentes
    // # Retorna pacientes cadastrados recentemente com limite
    // ==============================================
    @Transactional(readOnly = true)
    public List<PacienteDTO> buscarPacientesRecentes(int limite) {
        PageRequest pageRequest = PageRequest.of(0, limite, Sort.by("id").descending());
        
        List<PacienteDTO> pacientes = pacienteRepository.findAll(pageRequest).stream().map(pacienteService::converterParaDTO).toList();
        
        // Se não houver pacientes recentes, lança exceção amigável
        if (pacientes.isEmpty()) {
            throw new BusinessException("Nenhum paciente encontrado nos registros");
        }
        
        return pacientes;
    }
    
    // ==============================================
    // # Método - converterParaConsultaHojeDTO
    // # Converte uma entidade Consulta para ConsultaHojeDTO
    // ==============================================
    private ConsultaHojeDTO converterParaConsultaHojeDTO(Consulta consulta) {
        ConsultaHojeDTO dto = new ConsultaHojeDTO();
        dto.setId(consulta.getId());
        dto.setPacienteId(consulta.getPaciente().getId());
        dto.setNomePaciente(consulta.getPaciente().getNomeCompleto());
        dto.setIniciais(getIniciais(consulta.getPaciente().getNomeCompleto()));
        dto.setHorario(consulta.getDataConsulta());
        
        // Buscar o questionário separadamente
        questionarioRepository.findByConsultaId(consulta.getId()).ifPresent(questionario -> dto.setObjetivo(questionario.getObjetivo()));
        
        return dto;
    }
    
    // ==============================================
    // # Método - getIniciais
    // # Retorna as iniciais do nome do paciente
    // ==============================================
    private String getIniciais(String nome) {
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 0) return "NN";
        if (partes.length == 1) return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        return (partes[0].charAt(0) + "" + partes[partes.length - 1].charAt(0)).toUpperCase();
    }
}