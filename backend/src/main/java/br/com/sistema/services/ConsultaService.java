package br.com.sistema.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

@Service
@RequiredArgsConstructor
public class ConsultaService {

	private final ConsultaRepository consultaRepository;
	private final PacienteRepository pacienteRepository;
	private final AvaliacaoFisicaRepository avaliacaoFisicaRepository;
	private final QuestionarioEstiloVidaRepository questionarioRepository;
	private final RegistroFotograficoRepository registroFotograficoRepository;

	
	// ==============================================
	// # Método - criarConsulta
	// # Cria uma nova consulta vinculada a um paciente
	// ==============================================
	@Transactional
	public ConsultaResumoDTO criarConsulta(Long pacienteId) {
		Paciente paciente = pacienteRepository.findById(pacienteId).orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

		Consulta consulta = new Consulta();
		consulta.setPaciente(paciente);
		consulta.setDataConsulta(LocalDateTime.now());

		Consulta saved = consultaRepository.save(consulta);

		// Início da conversão manual para DTO
		ConsultaResumoDTO dto = new ConsultaResumoDTO();
		dto.setId(saved.getId());
		dto.setPacienteId(saved.getPaciente().getId());
		dto.setNomePaciente(saved.getPaciente().getNomeCompleto());
		dto.setDataConsulta(saved.getDataConsulta());

		// Como a consulta acabou de ser criada, os campos abaixo
		// serão naturalmente falsos/nulos, mas mantemos a lógica por consistência:
		dto.setTemAvaliacaoFisica(false);
		dto.setTemQuestionario(false);
		dto.setTemFotos(false);

		return dto;
	}

	// ==============================================
	// # Método - listarConsultasPorPaciente
	// # Lista o histórico de consultas de um paciente
	// ==============================================
	@Transactional(readOnly = true)
	public List<ConsultaResumoDTO> listarConsultasPorPaciente(Long pacienteId) {
		return consultaRepository.findByPacienteIdOrderByDataConsultaDesc(pacienteId).stream().map(consulta -> {
			ConsultaResumoDTO dto = new ConsultaResumoDTO();
			dto.setId(consulta.getId());
			dto.setPacienteId(consulta.getPaciente().getId());
			dto.setNomePaciente(consulta.getPaciente().getNomeCompleto());
			dto.setDataConsulta(consulta.getDataConsulta());

			// Busca Avaliação Física
			avaliacaoFisicaRepository.findByConsultaId(consulta.getId()).ifPresent(avaliacao -> {
				dto.setPeso(avaliacao.getPesoAtual());
				dto.setPercentualGordura(avaliacao.getPercentualGordura());
			});
			dto.setTemAvaliacaoFisica(avaliacaoFisicaRepository.existsByConsultaId(consulta.getId()));

			// Busca Questionário
			questionarioRepository.findByConsultaId(consulta.getId()).ifPresentOrElse(q -> {
				dto.setObjetivo(q.getObjetivo());
				dto.setTemQuestionario(true);
			}, () -> dto.setTemQuestionario(false));

			// Busca Fotos
			dto.setTemFotos(registroFotograficoRepository.existsByConsultaId(consulta.getId()));

			return dto;
		}).toList();
	}

	// ==============================================
	// # Método - buscarRascunhoNovaConsulta
	// # Retorna os dados da última consulta para pré-preenchimento da nova
	// ==============================================
	@Transactional(readOnly = true)
	public Optional<ConsultaDetalhadaDTO> buscarRascunhoNovaConsulta(Long pacienteId) {
		if (!pacienteRepository.existsById(pacienteId)) {
			throw new ResourceNotFoundException("Paciente não encontrado");
		}

		return consultaRepository.findFirstByPacienteIdOrderByDataConsultaDesc(pacienteId)
				.map(this::converterParaDetalhadaDTO)
				.map(this::limparIdsParaRascunho);
	}

	// ==============================================
	// # Método - listarConsultasPorPacientePaginado
	// # Lista consultas de um paciente de forma paginada
	// ==============================================
	@Transactional(readOnly = true)
	public Page<ConsultaResumoDTO> listarConsultasPorPacientePaginado(Long pacienteId, Pageable pageable) {
		return consultaRepository.findByPacienteIdOrderByDataConsultaDesc(pacienteId, pageable).map(consulta -> {
			ConsultaResumoDTO dto = new ConsultaResumoDTO();
			dto.setId(consulta.getId());
			dto.setPacienteId(consulta.getPaciente().getId());
			dto.setNomePaciente(consulta.getPaciente().getNomeCompleto());
			dto.setDataConsulta(consulta.getDataConsulta());

			avaliacaoFisicaRepository.findByConsultaId(consulta.getId()).ifPresent(avaliacao -> {
				dto.setPeso(avaliacao.getPesoAtual());
				dto.setPercentualGordura(avaliacao.getPercentualGordura());
			});
			dto.setTemAvaliacaoFisica(avaliacaoFisicaRepository.existsByConsultaId(consulta.getId()));

			questionarioRepository.findByConsultaId(consulta.getId()).ifPresentOrElse(q -> {
				dto.setObjetivo(q.getObjetivo());
				dto.setTemQuestionario(true);
			}, () -> dto.setTemQuestionario(false));

			dto.setTemFotos(registroFotograficoRepository.existsByConsultaId(consulta.getId()));

			return dto;
		});
	}
	
	// ==============================================
	// # Método - buscarConsultaCompleta
	// # Retorna todos os detalhes relacionados a uma consulta
	// ==============================================
	@Transactional(readOnly = true)
	public ConsultaDetalhadaDTO buscarConsultaCompleta(Long consultaId) {
		Consulta consulta = consultaRepository.findById(consultaId).orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));
		return converterParaDetalhadaDTO(consulta);
	}

	// ==============================================
	// # Método - compararConsultas
	// # Compara duas consultas e retorna diferenças entre avaliações
	// ==============================================
	@Transactional(readOnly = true)
	public ComparativoConsultasDTO compararConsultas(Long pacienteId, Long consultaInicialId, Long consultaFinalId) {
		Consulta consultaInicial = consultaRepository.findById(consultaInicialId).orElseThrow(() -> new ResourceNotFoundException("Consulta inicial não encontrada"));

		Consulta consultaFinal = consultaRepository.findById(consultaFinalId).orElseThrow(() -> new ResourceNotFoundException("Consulta final não encontrada"));

		if (!consultaInicial.getPaciente().getId().equals(pacienteId)
				|| !consultaFinal.getPaciente().getId().equals(pacienteId)) {
			throw new BusinessException("Consultas não pertencem ao mesmo paciente");
		}

		ComparativoConsultasDTO comparativo = new ComparativoConsultasDTO();
		comparativo.setConsultaInicial(converterParaDetalhadaDTO(consultaInicial));
		comparativo.setConsultaFinal(converterParaDetalhadaDTO(consultaFinal));
		comparativo.setDiferencas(calcularDiferencas(consultaInicial.getId(), consultaFinal.getId()));

		return comparativo;
	}

	// ==============================================
	// # Método - deletarConsulta
	// # Deleta uma consulta e todas as entidades relacionadas necessárias
	// ==============================================
	@Transactional
	public void deletarConsulta(Long consultaId) {
		Consulta consulta = consultaRepository.findById(consultaId).orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));

		// Deletar entidades relacionadas primeiro (se necessário)
		if (avaliacaoFisicaRepository.existsByConsultaId(consultaId)) {
			avaliacaoFisicaRepository.deleteByConsultaId(consultaId);
		}

		if (registroFotograficoRepository.existsByConsultaId(consultaId)) {
			registroFotograficoRepository.deleteByConsultaId(consultaId);
		}

		// Questionário precisa de @Modifying no Repository
		consultaRepository.delete(consulta);
	}

	// ==============================================
	// # Método - listarTodasConsultas
	// # Retorna todas as consultas ordenadas por data
	// ==============================================
	@Transactional(readOnly = true)
	public List<ConsultaListagemDTO> listarTodasConsultas() {
		List<Consulta> consultas = consultaRepository.findAllByOrderByDataConsultaDesc();

		return consultas.stream().map(consulta -> {
			ConsultaListagemDTO dto = new ConsultaListagemDTO();
			dto.setId(consulta.getId());
			dto.setPacienteId(consulta.getPaciente().getId());
			dto.setNomePaciente(consulta.getPaciente().getNomeCompleto());
			dto.setDataConsulta(consulta.getDataConsulta());
			return dto;
		}).toList();
	}

	// ==============================================
	// # Metodo - listarTodasConsultasPaginado
	// # Retorna consultas do sistema de forma paginada
	// ==============================================
	@Transactional(readOnly = true)
	public Page<ConsultaListagemDTO> listarTodasConsultasPaginado(Pageable pageable) {
		return consultaRepository.findAllByOrderByDataConsultaDesc(pageable).map(consulta -> {
			ConsultaListagemDTO dto = new ConsultaListagemDTO();
			dto.setId(consulta.getId());
			dto.setPacienteId(consulta.getPaciente().getId());
			dto.setNomePaciente(consulta.getPaciente().getNomeCompleto());
			dto.setDataConsulta(consulta.getDataConsulta());
			return dto;
		});
	}

	// ==============================================
	// # Método - atualizarConsulta
	// # Atualiza campos básicos da consulta (ex: data)
	// ==============================================
	@Transactional
	public ConsultaDetalhadaDTO atualizarConsulta(Long id, ConsultaAtualizacaoDTO dados) {
		System.out.println("Atualizando consulta ID: " + id);
	    Consulta consulta = consultaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));

	    if (dados.getDataConsulta() != null) {
	        consulta.setDataConsulta(dados.getDataConsulta());
	    }

	    consultaRepository.save(consulta);
	    return mapearParaConsultaDetalhada(consulta);
	}

	// ==============================================
	// # Método - atualizarDataConsulta
	// # Atualiza apenas a data de uma consulta e retorna resumo
	// ==============================================
	@Transactional
	public ConsultaResumoDTO atualizarDataConsulta(Long consultaId, LocalDateTime novaData) {
	    Consulta consulta = consultaRepository.findById(consultaId)
	        .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));

	    if (novaData == null) {
	        throw new BusinessException("Data da consulta não pode ser nula");
	    }

	    consulta.setDataConsulta(novaData);
	    Consulta updated = consultaRepository.save(consulta);

	    // Converter para DTO
	    ConsultaResumoDTO dto = new ConsultaResumoDTO();
	    dto.setId(updated.getId());
	    dto.setPacienteId(updated.getPaciente().getId());
	    dto.setNomePaciente(updated.getPaciente().getNomeCompleto());
	    dto.setDataConsulta(updated.getDataConsulta());
	    dto.setTemAvaliacaoFisica(avaliacaoFisicaRepository.existsByConsultaId(updated.getId()));
	    dto.setTemQuestionario(questionarioRepository.findByConsultaId(updated.getId()).isPresent());
	    dto.setTemFotos(registroFotograficoRepository.existsByConsultaId(updated.getId()));

	    return dto;
	}

	// ============================
	// METODOS AUXILIARES
	// ============================

	// ==============================================
	// # Método - mapearParaConsultaDetalhada
	// # Converte Consulta para ConsultaDetalhadaDTO reutilizando o conversor
	// ==============================================
	private ConsultaDetalhadaDTO mapearParaConsultaDetalhada(Consulta consulta) {
		// Reutiliza o conversor existente para garantir comportamento consistente
		return converterParaDetalhadaDTO(consulta);
	}

	// ==============================================
	// # Método - calcularDiferencas
	// # Calcula diferenças entre avaliações de duas consultas
	// ==============================================
	private DiferencasDTO calcularDiferencas(Long consultaInicialId, Long consultaFinalId) {
		DiferencasDTO diferencas = new DiferencasDTO();

		AvaliacaoFisica avalInicial = avaliacaoFisicaRepository.findByConsultaId(consultaInicialId).orElse(null);
		AvaliacaoFisica avalFinal = avaliacaoFisicaRepository.findByConsultaId(consultaFinalId).orElse(null);

		if (avalInicial == null || avalFinal == null) {
			return diferencas; // Retorna vazio se não houver avaliações
		}

		// Composição Corporal
		diferencas.setDiferencaPeso(calcularDiferenca(avalFinal.getPesoAtual(), avalInicial.getPesoAtual()));
		diferencas.setDiferencaPercentualGordura(calcularDiferenca(avalFinal.getPercentualGordura(), avalInicial.getPercentualGordura()));
		diferencas.setDiferencaMassaMagra(calcularDiferenca(avalFinal.getMassaMagra(), avalInicial.getMassaMagra()));
		diferencas.setDiferencaMassaGorda(calcularDiferenca(avalFinal.getMassaGorda(), avalInicial.getMassaGorda()));
		diferencas.setDiferencaImc(calcularDiferenca(avalFinal.getImc(), avalInicial.getImc()));

		// Perímetros
		Map<String, Double> diferencasPerimetros = new HashMap<>();
		diferencasPerimetros.put("ombro", calcularDiferenca(avalFinal.getPerimetroOmbro(), avalInicial.getPerimetroOmbro()));
		diferencasPerimetros.put("torax", calcularDiferenca(avalFinal.getPerimetroTorax(), avalInicial.getPerimetroTorax()));
		diferencasPerimetros.put("cintura", calcularDiferenca(avalFinal.getPerimetroCintura(), avalInicial.getPerimetroCintura()));
		diferencasPerimetros.put("abdominal", calcularDiferenca(avalFinal.getPerimetroAbdominal(), avalInicial.getPerimetroAbdominal()));
		diferencasPerimetros.put("quadril", calcularDiferenca(avalFinal.getPerimetroQuadril(), avalInicial.getPerimetroQuadril()));
		diferencasPerimetros.put("bracoDireitoRelax", calcularDiferenca(avalFinal.getPerimetroBracoDireitoRelax(), avalInicial.getPerimetroBracoDireitoRelax()));
		diferencasPerimetros.put("bracoDireitoContr", calcularDiferenca(avalFinal.getPerimetroBracoDireitoContr(), avalInicial.getPerimetroBracoDireitoContr()));
		diferencasPerimetros.put("bracoEsquerdoRelax", calcularDiferenca(avalFinal.getPerimetroBracoEsquerdoRelax(), avalInicial.getPerimetroBracoEsquerdoRelax()));
		diferencasPerimetros.put("bracoEsquerdoContr", calcularDiferenca(avalFinal.getPerimetroBracoEsquerdoContr(), avalInicial.getPerimetroBracoEsquerdoContr()));
		diferencasPerimetros.put("antebracoDireito", calcularDiferenca(avalFinal.getPerimetroAntebracoDireito(), avalInicial.getPerimetroAntebracoDireito()));
		diferencasPerimetros.put("antebracoEsquerdo", calcularDiferenca(avalFinal.getPerimetroAntebracoEsquerdo(), avalInicial.getPerimetroAntebracoEsquerdo()));
		diferencasPerimetros.put("coxa", calcularDiferenca(avalFinal.getPerimetroCoxa(), avalInicial.getPerimetroCoxa()));
		diferencasPerimetros.put("coxaDireita", calcularDiferenca(avalFinal.getPerimetroCoxaDireita(), avalInicial.getPerimetroCoxaDireita()));
		diferencasPerimetros.put("coxaEsquerda", calcularDiferenca(avalFinal.getPerimetroCoxaEsquerda(), avalInicial.getPerimetroCoxaEsquerda()));
		diferencasPerimetros.put("panturrilhaDireita", calcularDiferenca(avalFinal.getPerimetroPanturrilhaDireita(), avalInicial.getPerimetroPanturrilhaDireita()));
		diferencasPerimetros.put("panturrilhaEsquerda", calcularDiferenca(avalFinal.getPerimetroPanturrilhaEsquerda(), avalInicial.getPerimetroPanturrilhaEsquerda()));
		diferencas.setDiferencasPerimetros(diferencasPerimetros);

		// Dobras Cutâneas
		Map<String, Double> diferencasDobras = new HashMap<>();
		diferencasDobras.put("triceps", calcularDiferenca(avalFinal.getDobraTriceps(), avalInicial.getDobraTriceps()));
		diferencasDobras.put("peito", calcularDiferenca(avalFinal.getDobraPeito(), avalInicial.getDobraPeito()));
		diferencasDobras.put("axilarMedia", calcularDiferenca(avalFinal.getDobraAxilarMedia(), avalInicial.getDobraAxilarMedia()));
		diferencasDobras.put("subescapular", calcularDiferenca(avalFinal.getDobraSubescapular(), avalInicial.getDobraSubescapular()));
		diferencasDobras.put("abdominal", calcularDiferenca(avalFinal.getDobraAbdominal(), avalInicial.getDobraAbdominal()));
		diferencasDobras.put("supraIliaca", calcularDiferenca(avalFinal.getDobraSupraIliaca(), avalInicial.getDobraSupraIliaca()));
		diferencasDobras.put("coxa", calcularDiferenca(avalFinal.getDobraCoxa(), avalInicial.getDobraCoxa()));
		diferencas.setDiferencasDobras(diferencasDobras);

		return diferencas;
	}

	// ==============================================
	// # Método - calcularDiferenca
	// # Calcula diferença entre dois Double (retorna null se algum for null)
	// ==============================================
	private Double calcularDiferenca(Double valorFinal, Double valorInicial) {
		if (valorFinal == null || valorInicial == null) {
			return null;
		}
		return valorFinal - valorInicial;
	}

	// ==============================================
	// # Método - converterParaDetalhadaDTO
	// # Converte Consulta para ConsultaDetalhadaDTO
	// ==============================================
	private ConsultaDetalhadaDTO converterParaDetalhadaDTO(Consulta consulta) {
		ConsultaDetalhadaDTO dto = new ConsultaDetalhadaDTO();
		dto.setId(consulta.getId());
		dto.setPacienteId(consulta.getPaciente().getId());
		dto.setNomePaciente(consulta.getPaciente().getNomeCompleto());
		dto.setDataConsulta(consulta.getDataConsulta());

		// Buscar avaliação física
		avaliacaoFisicaRepository.findByConsultaId(consulta.getId()).ifPresent(avaliacao -> {
			dto.setAvaliacaoFisica(converterAvaliacaoParaDTO(avaliacao));
		});

		// Buscar questionário
		questionarioRepository.findByConsultaId(consulta.getId()).ifPresent(questionario -> {
			dto.setQuestionario(converterQuestionarioParaDTO(questionario));
		});

		// Buscar fotos
		registroFotograficoRepository.findByConsultaId(consulta.getId()).ifPresent(registro -> {
			dto.setRegistroFotografico(converterRegistroParaDTO(registro));
		});

		return dto;
	}

	// ==============================================
	// # Método - limparIdsParaRascunho
	// # Remove IDs para evitar atualização acidental da consulta anterior
	// ==============================================
	private ConsultaDetalhadaDTO limparIdsParaRascunho(ConsultaDetalhadaDTO dto) {
		dto.setId(null);
		dto.setDataConsulta(LocalDateTime.now());

		if (dto.getAvaliacaoFisica() != null) {
			dto.getAvaliacaoFisica().setId(null);
			dto.getAvaliacaoFisica().setConsultaId(null);
		}

		if (dto.getQuestionario() != null) {
			dto.getQuestionario().setId(null);
			dto.getQuestionario().setConsultaId(null);
		}

		if (dto.getRegistroFotografico() != null) {
			dto.getRegistroFotografico().setId(null);
			dto.getRegistroFotografico().setConsultaId(null);
		}

		return dto;
	}

	// ==============================================
	// # Método - converterAvaliacaoParaDTO
	// # Converte AvaliacaoFisica para AvaliacaoFisicaDTO
	// ==============================================
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

	// ==============================================
	// # Método - converterQuestionarioParaDTO
	// # Converte QuestionarioEstiloVida para DTO
	// ==============================================
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

	// ==============================================
	// # Método - converterRegistroParaDTO
	// # Converte RegistroFotografico para DTO
	// ==============================================
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

}