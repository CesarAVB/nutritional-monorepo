package br.com.sistema.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistema.dtos.AvaliacaoFisicaDTO;
import br.com.sistema.dtos.ComparativoConsultasDTO;
import br.com.sistema.dtos.ConsultaAtualizacaoDTO;
import br.com.sistema.dtos.ConsultaDetalhadaDTO;
import br.com.sistema.dtos.ConsultaListagemDTO;
import br.com.sistema.dtos.ConsultaResumoDTO;
import br.com.sistema.dtos.DiferencasDTO;
import br.com.sistema.dtos.QuestionarioEstiloVidaDTO;
import br.com.sistema.dtos.RegistroFotograficoDTO;
import br.com.sistema.exceptions.BusinessException;
import br.com.sistema.exceptions.ResourceNotFoundException;
import br.com.sistema.models.AvaliacaoFisica;
import br.com.sistema.models.Consulta;
import br.com.sistema.models.Paciente;
import br.com.sistema.models.QuestionarioEstiloVida;
import br.com.sistema.models.RegistroFotografico;
import br.com.sistema.repositories.AvaliacaoFisicaRepository;
import br.com.sistema.repositories.ConsultaRepository;
import br.com.sistema.repositories.PacienteRepository;
import br.com.sistema.repositories.QuestionarioEstiloVidaRepository;
import br.com.sistema.repositories.RegistroFotograficoRepository;
import lombok.RequiredArgsConstructor;

/**
 * Orquestra operacoes de consulta nutrologica: criacao, listagem, detalhamento, atualizacao e remocao.
 * Coordena avaliacao fisica, questionario de estilo de vida e registro fotografico vinculados a cada consulta.
 */
@Service
@RequiredArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final AvaliacaoFisicaRepository avaliacaoFisicaRepository;
    private final QuestionarioEstiloVidaRepository questionarioRepository;
    private final RegistroFotograficoRepository registroFotograficoRepository;

    /**
     * Cria uma nova consulta vinculada a um paciente existente.
     * Inicializa a data da consulta com o momento atual.
     *
     * @param pacienteId ID do paciente
     * @return Resumo da consulta criada
     */
    @Transactional
    public ConsultaResumoDTO criarConsulta(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente nao encontrado"));

        Consulta consulta = new Consulta();
        consulta.setPaciente(paciente);
        consulta.setDataConsulta(LocalDateTime.now());

        Consulta saved = consultaRepository.save(consulta);

        ConsultaResumoDTO dto = new ConsultaResumoDTO();
        dto.setId(saved.getId());
        dto.setPacienteId(saved.getPaciente().getId());
        dto.setNomePaciente(saved.getPaciente().getNomeCompleto());
        dto.setDataConsulta(saved.getDataConsulta());
        dto.setTemAvaliacaoFisica(false);
        dto.setTemQuestionario(false);
        dto.setTemFotos(false);

        return dto;
    }

    /**
     * Lista o historico de consultas de um paciente em ordem cronologica decrescente.
     * Inclui flags indicando existencia de avaliacao fisica, questionario e fotos.
     *
     * @param pacienteId ID do paciente
     * @return Lista de resumos de consultas
     */
    @Transactional(readOnly = true)
    public List<ConsultaResumoDTO> listarConsultasPorPaciente(Long pacienteId) {
        List<Consulta> consultas = consultaRepository.findByPacienteIdOrderByDataConsultaDesc(pacienteId);
        if (consultas.isEmpty()) return List.of();

        List<Long> ids = consultas.stream().map(Consulta::getId).toList();
        Set<Long> comAvaliacao = new HashSet<>(avaliacaoFisicaRepository.findConsultaIdsComAvaliacao(ids));
        Set<Long> comQuestionario = new HashSet<>(questionarioRepository.findConsultaIdsComQuestionario(ids));
        Set<Long> comFotos = new HashSet<>(registroFotograficoRepository.findConsultaIdsComFotos(ids));

        return consultas.stream().map(consulta -> {
            ConsultaResumoDTO dto = new ConsultaResumoDTO();
            dto.setId(consulta.getId());
            dto.setPacienteId(consulta.getPaciente().getId());
            dto.setNomePaciente(consulta.getPaciente().getNomeCompleto());
            dto.setDataConsulta(consulta.getDataConsulta());
            dto.setTemAvaliacaoFisica(comAvaliacao.contains(consulta.getId()));
            dto.setTemQuestionario(comQuestionario.contains(consulta.getId()));
            dto.setTemFotos(comFotos.contains(consulta.getId()));
            return dto;
        }).toList();
    }

    /**
     * Busca a ultima consulta de um paciente para pre-preenchimento de nova consulta.
     * Copia avaliacao fisica, questionario e registro fotografico para o novo rascunho.
     * Limpa IDs para evitar duplicacao de dados.
     *
     * @param pacienteId ID do paciente
     * @return DTO detalhado com dados da ultima consulta
     */
    @Transactional(readOnly = true)
    public Optional<ConsultaDetalhadaDTO> buscarRascunhoNovaConsulta(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente nao encontrado");
        }

        List<Consulta> consultas = consultaRepository.findByPacienteIdOrderByDataConsultaDesc(pacienteId);
        if (consultas.isEmpty()) {
            return Optional.empty();
        }

        Optional<Consulta> ultimaOpt = consultas.stream()
                .filter(c -> c.getAvaliacaoFisica() != null || c.getQuestionarioEstiloVida() != null || c.getRegistroFotografico() != null)
                .findFirst();

        if (ultimaOpt.isEmpty()) {
            return Optional.empty();
        }

        Consulta ultima = ultimaOpt.get();
        ConsultaDetalhadaDTO dto = new ConsultaDetalhadaDTO();

        if (ultima.getAvaliacaoFisica() != null) {
            AvaliacaoFisicaDTO avaliacaoDTO = converterAvaliacaoParaDTO(ultima.getAvaliacaoFisica());
            avaliacaoDTO.setId(null);
            avaliacaoDTO.setConsultaId(null);
            dto.setAvaliacaoFisica(avaliacaoDTO);
        }

        if (ultima.getQuestionarioEstiloVida() != null) {
            QuestionarioEstiloVidaDTO questionarioDTO = converterQuestionarioParaDTO(ultima.getQuestionarioEstiloVida());
            questionarioDTO.setId(null);
            questionarioDTO.setConsultaId(null);
            dto.setQuestionarioEstiloVida(questionarioDTO);
        }

        if (ultima.getRegistroFotografico() != null) {
            RegistroFotograficoDTO registroDTO = converterRegistroParaDTO(ultima.getRegistroFotografico());
            registroDTO.setId(null);
            registroDTO.setConsultaId(null);
            dto.setRegistroFotografico(registroDTO);
        }

        return Optional.of(dto);
    }

    /**
     * Lista todas as consultas para gerenciamento administrativo.
     * Ordenadas por data de consulta decrescente.
     *
     * @param pageable Configuracao de paginacao
     * @return Pagina de consultas para listagem
     */
    @Transactional(readOnly = true)
    public Page<ConsultaListagemDTO> listarConsultas(Pageable pageable) {
        return consultaRepository.findAll(pageable).map(this::converterParaListagemDTO);
    }

    /**
     * Busca detalhes completos de uma consulta incluindo avaliacao fisica,
     * questionario de estilo de vida e registro fotografico.
     *
     * @param id ID da consulta
     * @return DTO detalhado com todos os dados da consulta
     */
    @Transactional(readOnly = true)
    public ConsultaDetalhadaDTO buscarDetalhada(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta nao encontrada"));

        ConsultaDetalhadaDTO dto = new ConsultaDetalhadaDTO();
        dto.setId(consulta.getId());
        dto.setPacienteId(consulta.getPaciente().getId());
        dto.setNomePaciente(consulta.getPaciente().getNomeCompleto());
        dto.setDataConsulta(consulta.getDataConsulta());

        if (consulta.getAvaliacaoFisica() != null) {
            dto.setAvaliacaoFisica(converterAvaliacaoParaDTO(consulta.getAvaliacaoFisica()));
        }

        if (consulta.getQuestionarioEstiloVida() != null) {
            dto.setQuestionarioEstiloVida(converterQuestionarioParaDTO(consulta.getQuestionarioEstiloVida()));
        }

        if (consulta.getRegistroFotografico() != null) {
            dto.setRegistroFotografico(converterRegistroParaDTO(consulta.getRegistroFotografico()));
        }

        return dto;
    }

    /**
     * Atualiza a data de uma consulta ja existente.
     *
     * @param id ID da consulta
     * @param dto Dados da atualizacao
     * @return Consulta atualizada
     */
    @Transactional
    public Consulta atualizarConsulta(Long id, ConsultaAtualizacaoDTO dto) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta nao encontrada"));

        if (dto.getDataConsulta() != null) {
            consulta.setDataConsulta(dto.getDataConsulta());
        }

        return consultaRepository.save(consulta);
    }

    /**
     * Gera comparativo de medidas entre duas consultas de um paciente.
     * Calcula diferencas absolutas de peso, IMC, percentual de gordura e medidas corporais.
     * Lanca excecao se alguma das consultas nao possuir avaliacao fisica.
     *
     * @param pacienteId ID do paciente
     * @param idConsulta1 ID da primeira consulta
     * @param idConsulta2 ID da segunda consulta
     * @return Comparativo com diferencas entre as consultas
     */
    @Transactional(readOnly = true)
    public ComparativoConsultasDTO gerarComparativo(Long pacienteId, Long idConsulta1, Long idConsulta2) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente nao encontrado");
        }

        AvaliacaoFisica avaliacao1 = avaliacaoFisicaRepository.findByConsultaId(idConsulta1)
                .orElseThrow(() -> new BusinessException("Avaliacao fisica nao encontrada na primeira consulta"));

        AvaliacaoFisica avaliacao2 = avaliacaoFisicaRepository.findByConsultaId(idConsulta2)
                .orElseThrow(() -> new BusinessException("Avaliacao fisica nao encontrada na segunda consulta"));

        ComparativoConsultasDTO comparativo = new ComparativoConsultasDTO();
        comparativo.setIdConsulta1(idConsulta1);
        comparativo.setIdConsulta2(idConsulta2);
        comparativo.setDataConsulta1(avaliacao1.getConsulta().getDataConsulta());
        comparativo.setDataConsulta2(avaliacao2.getConsulta().getDataConsulta());

        DiferencasDTO diferencas = new DiferencasDTO();
        diferencas.setPeso(atualMenosAnterior(avaliacao2.getPesoAtual(), avaliacao1.getPesoAtual()));
        diferencas.setImc(atualMenosAnterior(avaliacao2.getImc(), avaliacao1.getImc()));
        diferencas.setPercentualGordura(atualMenosAnterior(avaliacao2.getPercentualGordura(), avaliacao1.getPercentualGordura()));
        diferencas.setMassaGorda(atualMenosAnterior(avaliacao2.getMassaGorda(), avaliacao1.getMassaGorda()));
        diferencas.setMassaMagra(atualMenosAnterior(avaliacao2.getMassaMagra(), avaliacao1.getMassaMagra()));

        comparativo.setDiferencas(diferencas);

        Map<String, Map<String, Double>> medidas = new HashMap<>();
        medidas.put("perimetroOmbro", criarMapa(avaliacao1.getPerimetroOmbro(), avaliacao2.getPerimetroOmbro()));
        medidas.put("perimetroTorax", criarMapa(avaliacao1.getPerimetroTorax(), avaliacao2.getPerimetroTorax()));
        medidas.put("perimetroCintura", criarMapa(avaliacao1.getPerimetroCintura(), avaliacao2.getPerimetroCintura()));
        medidas.put("perimetroAbdominal", criarMapa(avaliacao1.getPerimetroAbdominal(), avaliacao2.getPerimetroAbdominal()));
        medidas.put("perimetroQuadril", criarMapa(avaliacao1.getPerimetroQuadril(), avaliacao2.getPerimetroQuadril()));
        medidas.put("perimetroBracoDireitoRelax", criarMapa(avaliacao1.getPerimetroBracoDireitoRelax(), avaliacao2.getPerimetroBracoDireitoRelax()));
        medidas.put("perimetroBracoEsquerdoRelax", criarMapa(avaliacao1.getPerimetroBracoEsquerdoRelax(), avaliacao2.getPerimetroBracoEsquerdoRelax()));
        medidas.put("perimetroCoxaDireita", criarMapa(avaliacao1.getPerimetroCoxaDireita(), avaliacao2.getPerimetroCoxaDireita()));
        medidas.put("perimetroCoxaEsquerda", criarMapa(avaliacao1.getPerimetroCoxaEsquerda(), avaliacao2.getPerimetroCoxaEsquerda()));
        medidas.put("perimetroPanturrilhaDireita", criarMapa(avaliacao1.getPerimetroPanturrilhaDireita(), avaliacao2.getPerimetroPanturrilhaDireita()));
        medidas.put("perimetroPanturrilhaEsquerda", criarMapa(avaliacao1.getPerimetroPanturrilhaEsquerda(), avaliacao2.getPerimetroPanturrilhaEsquerda()));

        comparativo.setMedidasCorporais(medidas);

        return comparativo;
    }

    /**
     * Remove uma consulta e todos os dados relacionados (avaliacao, questionario, fotos).
     * Realiza remocao em cascata via repository para manter integridade referencial.
     *
     * @param id ID da consulta
     */
    @Transactional
    public void deletarConsulta(Long id) {
        if (!consultaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Consulta nao encontrada");
        }

        avaliacaoFisicaRepository.deleteByConsultaId(id);
        questionarioRepository.deleteByConsultaId(id);
        registroFotograficoRepository.deleteByConsultaId(id);
        consultaRepository.deleteById(id);
    }

    /**
     * Converte entidade AvaliacaoFisica para DTO.
     */
    private AvaliacaoFisicaDTO converterAvaliacaoParaDTO(AvaliacaoFisica avaliacao) {
        AvaliacaoFisicaDTO dto = new AvaliacaoFisicaDTO();
        dto.setId(avaliacao.getId());
        dto.setConsultaId(avaliacao.getConsulta().getId());
        dto.setAltura(avaliacao.getAltura());
        dto.setPerimetroOmbro(avaliacao.getPerimetroOmbro());
        dto.setPerimetroTorax(avaliacao.getPerimetroTorax());
        dto.setPerimetroCintura(avaliacao.getPerimetroCintura());
        dto.setPerimetroAbdominal(avaliacao.getPerimetroAbdominal());
        dto.setPerimetroQuadril(avaliacao.getPerimetroQuadril());
        dto.setPerimetroBracoDireitoRelax(avaliacao.getPerimetroBracoDireitoRelax());
        dto.setPerimetroBracoDireitoContr(avaliacao.getPerimetroBracoDireitoContr());
        dto.setPerimetroBracoEsquerdoRelax(avaliacao.getPerimetroBracoEsquerdoRelax());
        dto.setPerimetroBracoEsquerdoContr(avaliacao.getPerimetroBracoEsquerdoContr());
        dto.setPerimetroAntebracoDireito(avaliacao.getPerimetroAntebracoDireito());
        dto.setPerimetroAntebracoEsquerdo(avaliacao.getPerimetroAntebracoEsquerdo());
        dto.setPerimetroCoxa(avaliacao.getPerimetroCoxa());
        dto.setPerimetroCoxaDireita(avaliacao.getPerimetroCoxaDireita());
        dto.setPerimetroCoxaEsquerda(avaliacao.getPerimetroCoxaEsquerda());
        dto.setPerimetroPanturrilhaDireita(avaliacao.getPerimetroPanturrilhaDireita());
        dto.setPerimetroPanturrilhaEsquerda(avaliacao.getPerimetroPanturrilhaEsquerda());
        dto.setDobraTriceps(avaliacao.getDobraTriceps());
        dto.setDobraPeito(avaliacao.getDobraPeito());
        dto.setDobraAxilarMedia(avaliacao.getDobraAxilarMedia());
        dto.setDobraSubescapular(avaliacao.getDobraSubescapular());
        dto.setDobraAbdominal(avaliacao.getDobraAbdominal());
        dto.setDobraSupraIliaca(avaliacao.getDobraSupraIliaca());
        dto.setDobraCoxa(avaliacao.getDobraCoxa());
        dto.setPesoAtual(avaliacao.getPesoAtual());
        dto.setMassaMagra(avaliacao.getMassaMagra());
        dto.setMassaGorda(avaliacao.getMassaGorda());
        dto.setPercentualGordura(avaliacao.getPercentualGordura());
        dto.setImc(avaliacao.getImc());
        return dto;
    }

    /**
     * Converte entidade QuestionarioEstiloVida para DTO.
     */
    private QuestionarioEstiloVidaDTO converterQuestionarioParaDTO(QuestionarioEstiloVida questionario) {
        QuestionarioEstiloVidaDTO dto = new QuestionarioEstiloVidaDTO();
        dto.setId(questionario.getId());
        dto.setConsultaId(questionario.getConsulta().getId());
        dto.setObjetivo(questionario.getObjetivo());
        dto.setFrequenciaTreino(questionario.getFrequenciaTreino());
        dto.setTempoTreino(questionario.getTempoTreino());
        dto.setCirurgias(questionario.getCirurgias());
        dto.setDoencas(questionario.getDoencas());
        dto.setHistoricoFamiliar(questionario.getHistoricoFamiliar());
        dto.setMedicamentos(questionario.getMedicamentos());
        dto.setSuplementos(questionario.getSuplementos());
        dto.setUsoAnabolizantes(questionario.getUsoAnabolizantes());
        dto.setCicloAnabolizantes(questionario.getCicloAnabolizantes());
        dto.setDuracaoAnabolizantes(questionario.getDuracaoAnabolizantes());
        dto.setFuma(questionario.getFuma());
        dto.setFrequenciaAlcool(questionario.getFrequenciaAlcool());
        dto.setFuncionamentoIntestino(questionario.getFuncionamentoIntestino());
        dto.setQualidadeSono(questionario.getQualidadeSono());
        dto.setIngestaoAguaDiaria(questionario.getIngestaoAguaDiaria());
        dto.setAlimentosNaoGosta(questionario.getAlimentosNaoGosta());
        dto.setFrutasPreferidas(questionario.getFrutasPreferidas());
        dto.setNumeroRefeicoesDesejadas(questionario.getNumeroRefeicoesDesejadas());
        dto.setHorarioMaiorFome(questionario.getHorarioMaiorFome());
        dto.setPressaoArterial(questionario.getPressaoArterial());
        dto.setIntolerancias(questionario.getIntolerancias());
        return dto;
    }

    /**
     * Converte entidade RegistroFotografico para DTO.
     */
    private RegistroFotograficoDTO converterRegistroParaDTO(RegistroFotografico registro) {
        RegistroFotograficoDTO dto = new RegistroFotograficoDTO();
        dto.setId(registro.getId());
        dto.setConsultaId(registro.getConsulta().getId());
        dto.setFotoAnterior(registro.getFotoAnterior());
        dto.setFotoPosterior(registro.getFotoPosterior());
        dto.setFotoLateralEsquerda(registro.getFotoLateralEsquerda());
        dto.setFotoLateralDireita(registro.getFotoLateralDireita());
        return dto;
    }

    /**
     * Converte entidade Consulta para DTO de listagem paginada.
     */
    private ConsultaListagemDTO converterParaListagemDTO(Consulta consulta) {
        ConsultaListagemDTO dto = new ConsultaListagemDTO();
        dto.setId(consulta.getId());
        dto.setPacienteId(consulta.getPaciente().getId());
        dto.setNomePaciente(consulta.getPaciente().getNomeCompleto());
        dto.setDataConsulta(consulta.getDataConsulta());

        avaliacaoFisicaRepository.findByConsultaId(consulta.getId()).ifPresent(a -> dto.setTemAvaliacaoFisica(true));
        questionarioRepository.findByConsultaId(consulta.getId()).ifPresent(q -> dto.setTemQuestionario(true));
        registroFotograficoRepository.findByConsultaId(consulta.getId()).ifPresent(r -> dto.setTemFotos(true));

        return dto;
    }

    /**
     * Calcula diferenca entre valor atual e anterior.
     * Trata valores nulos retornando null.
     */
    private Double atualMenosAnterior(Double atual, Double anterior) {
        if (atual == null || anterior == null) return null;
        return Math.round((atual - anterior) * 100.0) / 100.0;
    }

    /**
     * Cria mapa com valores anterior e diferenca de uma medida.
     */
    private Map<String, Double> criarMapa(Double anterior, Double atual) {
        Map<String, Double> mapa = new HashMap<>();
        mapa.put("anterior", anterior);
        mapa.put("atual", atual);
        mapa.put("diferenca", atualMenosAnterior(atual, anterior));
        return mapa;
    }
}
