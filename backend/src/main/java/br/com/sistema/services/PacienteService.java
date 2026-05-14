package br.com.sistema.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistema.dtos.PacienteDTO;
import br.com.sistema.exceptions.BusinessException;
import br.com.sistema.exceptions.ResourceNotFoundException;
import br.com.sistema.models.Paciente;
import br.com.sistema.repositories.AgendamentoRepository;
import br.com.sistema.repositories.ConsultaRepository;
import br.com.sistema.repositories.PacienteRepository;
import lombok.RequiredArgsConstructor;

/**
 * Gerencia o cadastro e consulta de pacientes.
 * Opera sobre a entidade Paciente oferecendo CRUD completo, buscas por
 * nome/CPF, listagens ordenadas e paginadas. Inclui calculo de campos
 * derivados como total de consultas e data da ultima consulta.
 */
@Service
@RequiredArgsConstructor
public class PacienteService {
    
    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;
    private final AgendamentoRepository agendamentoRepository;
    
    /**
     * Cadastra novo paciente validando unicidade de CPF e obrigatoriedade
     * do sexo (constraint do banco). Campos nao informados permanecem nulos.
     *
     * @param dto dados do paciente a cadastrar
     * @return paciente criado com ID gerado
     */
    @Transactional
    public PacienteDTO cadastrarPaciente(PacienteDTO dto) {
        if (pacienteRepository.existsByCpf(dto.getCpf())) {
            throw new BusinessException("CPF j� cadastrado no sistema");
        }
        
        if (dto.getSexo() == null) {
            throw new BusinessException("Sexo � obrigat�rio");
        }
        
        Paciente paciente = new Paciente();
        paciente.setNomeCompleto(dto.getNomeCompleto());
        paciente.setCpf(dto.getCpf());
        paciente.setDataNascimento(dto.getDataNascimento());
        paciente.setTelefoneWhatsapp(dto.getTelefoneWhatsapp());
        paciente.setEmail(dto.getEmail());
        paciente.setSexo(dto.getSexo());
        paciente.setProntuario(dto.getProntuario());
        paciente.setCadastroIncompleto(false);
        paciente.setOrigemAgendamento(false);
        Paciente saved = pacienteRepository.save(paciente);
        return converterParaDTO(saved);
    }
    
    /**
     * Busca paciente por ID retornando DTO com campos calculados
     * (total de consultas e ultima consulta).
     *
     * @param id ID do paciente
     * @return dados do paciente
     */
    @Transactional(readOnly = true)
    public PacienteDTO buscarPorId(Long id) {
        Paciente paciente = pacienteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Paciente n�o encontrado"));
        return converterParaDTO(paciente);
    }
    
    /**
     * Busca paciente por CPF para operacoes de lookup rapido
     * (login, identificacao em tela de consulta).
     *
     * @param cpf CPF do paciente
     * @return dados do paciente
     */
    @Transactional(readOnly = true)
    public PacienteDTO buscarPorCpf(String cpf) {
        Paciente paciente = pacienteRepository.findByCpf(cpf).orElseThrow(() -> new ResourceNotFoundException("Paciente n�o encontrado"));
        return converterParaDTO(paciente);
    }
    
    /**
     * Lista todos os pacientes ordenados por ID decrescente (mais
     * recentes primeiro). Utiliza ordenacao em memoria para evitar
     * impacto em tabelas grandes; preferrir listarPaginado.
     *
     * @return lista de pacientes
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> listarTodos() {
        return pacienteRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(this::converterParaDTO).toList();
    }

    /**
     * Lista pacientes de forma paginada com summary de consultas
     * carregado em batch para evitar N+1 queries.
     *
     * @param pageable configuracao de pagina e ordenacao
     * @return pagina de pacientes com total e ultima consulta
     */
    @Transactional(readOnly = true)
    public Page<PacienteDTO> listarTodosPaginado(Pageable pageable) {
        Page<Paciente> page = pacienteRepository.findAll(pageable);
        Map<Long, Object[]> summaryMap = buildConsultaSummaryMap(
                page.getContent().stream().map(Paciente::getId).toList());
        return page.map(p -> converterParaDTOComSummary(p, summaryMap));
    }

    /**
     * Busca pacientes por nome utilizando busca case-insensitive
     * (LIKE %nome%). Ordenado por ID decrescente.
     *
     * @param nome fragmento do nome a buscar
     * @return pacientes que contem o nome
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> buscarPorNome(String nome) {
        return pacienteRepository.findByNomeCompletoContainingIgnoreCase(nome).stream().map(this::converterParaDTO).toList();
    }

    /**
     * Busca pacientes por nome de forma paginada carregando summary
     * de consultas em batch para otimizar performance.
     *
     * @param nome fragmento do nome a buscar
     * @param pageable configuracao de pagina
     * @return pagina de pacientes
     */
    @Transactional(readOnly = true)
    public Page<PacienteDTO> buscarPorNomePaginado(String nome, Pageable pageable) {
        Page<Paciente> page = pacienteRepository.findByNomeCompletoContainingIgnoreCase(nome, pageable);
        Map<Long, Object[]> summaryMap = buildConsultaSummaryMap(
                page.getContent().stream().map(Paciente::getId).toList());
        return page.map(p -> converterParaDTOComSummary(p, summaryMap));
    }
    
    /**
     * Atualiza campos do paciente de forma parcial (updates condicionais).
     * Sexo e prontuario sao atualizados apenas quando o DTO fornece
     * valor, evitando sobrescrever com null.
     *
     * @param id ID do paciente
     * @param dto novos valores (campos nulos sao ignorados)
     * @return paciente atualizado
     */
    @Transactional
    public PacienteDTO atualizarPaciente(Long id, PacienteDTO dto) {
        Paciente paciente = pacienteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Paciente n�o encontrado"));
        paciente.setNomeCompleto(dto.getNomeCompleto());
        atualizarCpfQuandoInformado(paciente, dto.getCpf());
        paciente.setDataNascimento(dto.getDataNascimento());
        paciente.setTelefoneWhatsapp(dto.getTelefoneWhatsapp());
        paciente.setEmail(dto.getEmail());
        if (dto.getSexo() != null) {
            paciente.setSexo(dto.getSexo());
        }
        if (dto.getProntuario() != null) {
            paciente.setProntuario(dto.getProntuario());
        }
        paciente.setCadastroIncompleto(temCamposObrigatoriosPendentes(paciente));
        Paciente updated = pacienteRepository.save(paciente);
        return converterParaDTO(updated);
    }
    
    /**
     * Remove paciente por ID. Lanca excecao se nao existir.
     *
     * @param id ID do paciente a remover
     */
    @Transactional
    public void deletarPaciente(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paciente n�o encontrado");
        }
        pacienteRepository.deleteById(id);
    }
    
    /**
     * Converte entidade Paciente para DTO com campos calculados
     * (total de consultas e ultima consulta via consultas individuais).
     * Usado em operacoes de entidade unica onde performance de
     * queries extras eh aceitavel.
     *
     * @param paciente entidade
     * @return DTO com informacoes basicas e derivadas
     */
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
        dto.setCadastroIncompleto(isCadastroIncompleto(paciente));
        dto.setOrigemAgendamento(paciente.isOrigemAgendamento());

        Long totalConsultas = consultaRepository.countByPacienteId(paciente.getId());
        dto.setTotalConsultas(totalConsultas.intValue());

        LocalDateTime ultimaConsulta = consultaRepository
                .findFirstByPacienteIdOrderByDataConsultaDesc(paciente.getId())
                .map(c -> c.getDataConsulta())
                .orElse(null);

        LocalDateTime ultimoAgendamento = agendamentoRepository
                .findUltimaVisita(paciente.getId())
                .map(a -> a.getDataHoraInicio())
                .orElse(null);

        if (ultimaConsulta != null && ultimoAgendamento != null) {
            dto.setUltimaConsulta(ultimaConsulta.isAfter(ultimoAgendamento) ? ultimaConsulta : ultimoAgendamento);
        } else if (ultimoAgendamento != null) {
            dto.setUltimaConsulta(ultimoAgendamento);
        } else if (ultimaConsulta != null) {
            dto.setUltimaConsulta(ultimaConsulta);
        }

        return dto;
    }

    /**
     * Carrega summary de consultas (count + ultima data) para uma lista
     * de pacientes em uma unica query usando batch query.
     *
     * @param pacienteIds lista de IDs de pacientes
     * @return mapa de pacienteId para [pacienteId, totalConsultas, ultimaData]
     */
    private Map<Long, Object[]> buildConsultaSummaryMap(List<Long> pacienteIds) {
        Map<Long, Object[]> map = new HashMap<>();
        if (!pacienteIds.isEmpty()) {
            consultaRepository.findResumoByPacienteIds(pacienteIds)
                    .forEach(row -> map.put((Long) row[0], row));
        }
        return map;
    }

    /**
     * Converte Paciente para DTO usando dados de summary pre-carregados
     * em batch para evitar N+1 queries em listagens.
     *
     * @param paciente entidade
     * @param summaryMap mapa com total e ultima consulta por pacienteId
     * @return DTO com informacoes basicas e derivadas do batch
     */
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
        dto.setCadastroIncompleto(isCadastroIncompleto(paciente));
        dto.setOrigemAgendamento(paciente.isOrigemAgendamento());

        Object[] summary = summaryMap.get(paciente.getId());
        if (summary != null) {
            dto.setTotalConsultas(((Long) summary[1]).intValue());
            dto.setUltimaConsulta((LocalDateTime) summary[2]);
        } else {
            dto.setTotalConsultas(0);
        }

        return dto;
    }

    /**
     * Atualiza CPF apenas quando o valor muda, validando unicidade.
     */
    private void atualizarCpfQuandoInformado(Paciente paciente, String novoCpf) {
        if (Objects.equals(paciente.getCpf(), novoCpf)) {
            return;
        }

        if (novoCpf != null && pacienteRepository.existsByCpf(novoCpf)) {
            throw new BusinessException("CPF jï¿½ cadastrado no sistema");
        }

        paciente.setCpf(novoCpf);
    }

    private boolean isCadastroIncompleto(Paciente paciente) {
        return paciente.isCadastroIncompleto()
                || temCamposObrigatoriosPendentes(paciente);
    }

    private boolean temCamposObrigatoriosPendentes(Paciente paciente) {
        return paciente.getCpf() == null
                || paciente.getCpf().isBlank()
                || paciente.getDataNascimento() == null
                || paciente.getSexo() == null;
    }
}
