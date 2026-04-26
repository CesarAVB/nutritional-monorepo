package br.com.sistema.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistema.dtos.PacienteDTO;
import br.com.sistema.exceptions.BusinessException;
import br.com.sistema.exceptions.ResourceNotFoundException;
import br.com.sistema.models.Consulta;
import br.com.sistema.models.Paciente;
import br.com.sistema.repositories.ConsultaRepository;
import br.com.sistema.repositories.PacienteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PacienteService {
    
    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;
    
    // ==============================================
    // # Método - cadastrarPaciente
    // # Cadastra um novo paciente garantindo unicidade de CPF
    // ==============================================
    @Transactional
    public PacienteDTO cadastrarPaciente(PacienteDTO dto) {
        if (pacienteRepository.existsByCpf(dto.getCpf())) {
            throw new BusinessException("CPF já cadastrado no sistema");
        }
        
        // Validar sexo na criação (regras de banco: coluna NOT NULL)
        if (dto.getSexo() == null) {
            throw new BusinessException("Sexo é obrigatório");
        }
        
        Paciente paciente = new Paciente();
        paciente.setNomeCompleto(dto.getNomeCompleto());
        paciente.setCpf(dto.getCpf());
        paciente.setDataNascimento(dto.getDataNascimento());
        paciente.setTelefoneWhatsapp(dto.getTelefoneWhatsapp());
        paciente.setEmail(dto.getEmail());
        paciente.setSexo(dto.getSexo());
        paciente.setProntuario(dto.getProntuario());
        Paciente saved = pacienteRepository.save(paciente);
        return converterParaDTO(saved);
    }
    
    // ==============================================
    // # Método - buscarPorId
    // # Busca paciente por ID
    // ==============================================
    @Transactional(readOnly = true)
    public PacienteDTO buscarPorId(Long id) {
        Paciente paciente = pacienteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
        return converterParaDTO(paciente);
    }
    
    // ==============================================
    // # Método - buscarPorCpf
    // # Busca paciente por CPF
    // ==============================================
    @Transactional(readOnly = true)
    public PacienteDTO buscarPorCpf(String cpf) {
        Paciente paciente = pacienteRepository.findByCpf(cpf).orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
        return converterParaDTO(paciente);
    }
    
    // ==============================================
    // # Método - listarTodos
    // # Lista todos os pacientes
    // ==============================================
    @Transactional(readOnly = true)
    public List<PacienteDTO> listarTodos() {
        // return pacienteRepository.findAll().stream().map(this::converterParaDTO).toList(); 									// Ordenação por ID decrescente para mostrar os mais recentes primeiro
    	return pacienteRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(this::converterParaDTO).toList(); 	// Ordenação por ID decrescente para mostrar os mais recentes primeiro

    }
    
    // ==============================================
    // # Metodo - listarTodosPaginado
    // # Lista pacientes de forma paginada
    // ==============================================
    @Transactional(readOnly = true)
    public Page<PacienteDTO> listarTodosPaginado(Pageable pageable) {
        Page<Paciente> page = pacienteRepository.findAll(pageable);
        Map<Long, Object[]> summaryMap = buildConsultaSummaryMap(
                page.getContent().stream().map(Paciente::getId).toList());
        return page.map(p -> converterParaDTOComSummary(p, summaryMap));
    }

    // ==============================================
    // # Metodo - buscarPorNome
    // # Busca pacientes por nome
    // ==============================================
    @Transactional(readOnly = true)
    public List<PacienteDTO> buscarPorNome(String nome) {
        return pacienteRepository.findByNomeCompletoContainingIgnoreCase(nome).stream().map(this::converterParaDTO).toList();
    }

    // ==============================================
    // # Metodo - buscarPorNomePaginado
    // # Busca pacientes por nome de forma paginada
    // ==============================================
    @Transactional(readOnly = true)
    public Page<PacienteDTO> buscarPorNomePaginado(String nome, Pageable pageable) {
        Page<Paciente> page = pacienteRepository.findByNomeCompletoContainingIgnoreCase(nome, pageable);
        Map<Long, Object[]> summaryMap = buildConsultaSummaryMap(
                page.getContent().stream().map(Paciente::getId).toList());
        return page.map(p -> converterParaDTOComSummary(p, summaryMap));
    }
    
    // ==============================================
    // # Método - atualizarPaciente
    // # Atualiza informações do paciente (parcialmente)
    // ==============================================
    @Transactional
    public PacienteDTO atualizarPaciente(Long id, PacienteDTO dto) {
        Paciente paciente = pacienteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
        paciente.setNomeCompleto(dto.getNomeCompleto());
        paciente.setTelefoneWhatsapp(dto.getTelefoneWhatsapp());
        paciente.setEmail(dto.getEmail());
        // Atualiza sexo apenas se o DTO fornecer o valor (suporta atualizações parciais sem sobrescrever)
        if (dto.getSexo() != null) {
            paciente.setSexo(dto.getSexo());
        }
        if (dto.getProntuario() != null) {
            paciente.setProntuario(dto.getProntuario());
        }
        Paciente updated = pacienteRepository.save(paciente);
        return converterParaDTO(updated);
    }
    
    // ==============================================
    // # Método - deletarPaciente
    // # Remove um paciente por ID
    // ==============================================
    @Transactional
    public void deletarPaciente(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paciente não encontrado");
        }
        pacienteRepository.deleteById(id);
    }
    
    // ==============================================
    // # Método - converterParaDTO
    // # Converte entidade Paciente para PacienteDTO adicionando campos calculados
    // # Usado em operações de entidade única (buscarPorId, cadastrar, atualizar)
    // ==============================================
    public PacienteDTO converterParaDTO(Paciente paciente) {
        PacienteDTO dto = new PacienteDTO();
        dto.setId(paciente.getId());
        dto.setNomeCompleto(paciente.getNomeCompleto());
        dto.setCpf(paciente.getCpf());
        dto.setDataNascimento(paciente.getDataNascimento());
        dto.setTelefoneWhatsapp(paciente.getTelefoneWhatsapp());
        dto.setEmail(paciente.getEmail());
        dto.setSexo(paciente.getSexo());
        dto.setProntuario(paciente.getProntuario());

        Long totalConsultas = consultaRepository.countByPacienteId(paciente.getId());
        dto.setTotalConsultas(totalConsultas.intValue());

        consultaRepository.findFirstByPacienteIdOrderByDataConsultaDesc(paciente.getId())
                .ifPresent(c -> dto.setUltimaConsulta(c.getDataConsulta()));

        return dto;
    }

    // ==============================================
    // # Método - buildConsultaSummaryMap
    // # Carrega count + última consulta para uma lista de pacientes em uma única query
    // ==============================================
    private Map<Long, Object[]> buildConsultaSummaryMap(List<Long> pacienteIds) {
        Map<Long, Object[]> map = new HashMap<>();
        if (!pacienteIds.isEmpty()) {
            consultaRepository.findResumoByPacienteIds(pacienteIds)
                    .forEach(row -> map.put((Long) row[0], row));
        }
        return map;
    }

    // ==============================================
    // # Método - converterParaDTOComSummary
    // # Converte Paciente para DTO usando dados pré-carregados em batch
    // ==============================================
    private PacienteDTO converterParaDTOComSummary(Paciente paciente, Map<Long, Object[]> summaryMap) {
        PacienteDTO dto = new PacienteDTO();
        dto.setId(paciente.getId());
        dto.setNomeCompleto(paciente.getNomeCompleto());
        dto.setCpf(paciente.getCpf());
        dto.setDataNascimento(paciente.getDataNascimento());
        dto.setTelefoneWhatsapp(paciente.getTelefoneWhatsapp());
        dto.setEmail(paciente.getEmail());
        dto.setSexo(paciente.getSexo());
        dto.setProntuario(paciente.getProntuario());

        Object[] summary = summaryMap.get(paciente.getId());
        if (summary != null) {
            dto.setTotalConsultas(((Long) summary[1]).intValue());
            dto.setUltimaConsulta((LocalDateTime) summary[2]);
        } else {
            dto.setTotalConsultas(0);
        }

        return dto;
    }
}