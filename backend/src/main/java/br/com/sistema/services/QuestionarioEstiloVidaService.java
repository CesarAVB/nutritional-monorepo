package br.com.sistema.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistema.dtos.QuestionarioEstiloVidaDTO;
import br.com.sistema.exceptions.BusinessException;
import br.com.sistema.exceptions.ResourceNotFoundException;
import br.com.sistema.models.Consulta;
import br.com.sistema.models.QuestionarioEstiloVida;
import br.com.sistema.repositories.ConsultaRepository;
import br.com.sistema.repositories.QuestionarioEstiloVidaRepository;
import lombok.RequiredArgsConstructor;

/**
 * Gerencia o questionario de estilo de vida vinculado a consultas.
 * Cada consulta pode ter um questionario com dados de treinamento,
 * historico medico, preferencias alimentares e habitos de vida.
 * Implementa unicidade: apenas um questionario por consulta.
 */
@Service
@RequiredArgsConstructor
public class QuestionarioEstiloVidaService {
    
    private final QuestionarioEstiloVidaRepository questionarioRepository;
    private final ConsultaRepository consultaRepository;
    
    /**
     * Salva novo questionario para uma consulta. Lança BusinessException
     * se ja existir questionario para evitar duplicacao.
     *
     * @param consultaId ID da consulta
     * @param dto dados do questionario
     * @return questionario criado com ID gerado
     */
    @Transactional
    public QuestionarioEstiloVidaDTO salvarQuestionario(Long consultaId, QuestionarioEstiloVidaDTO dto) {
        Consulta consulta = consultaRepository.findById(consultaId).orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));
        
        var existe = questionarioRepository.findByConsultaId(consultaId);
        
        if (existe.isPresent()) {
            throw new BusinessException("Já existe um questionário para esta consulta");
        }

        QuestionarioEstiloVida questionario = new QuestionarioEstiloVida();
        questionario.setConsulta(consulta);
        mapearDTOParaEntidade(dto, questionario);

        QuestionarioEstiloVida saved = questionarioRepository.save(questionario);
        return converterParaDTO(saved);
    }
    
    /**
     * Atualiza questionario existente para uma consulta.
     * Nao permite substituicao de questionario (apenas atualizacao).
     *
     * @param consultaId ID da consulta
     * @param dto novos dados (campos nulos sao ignorados)
     * @return questionario atualizado
     */
    @Transactional
    public QuestionarioEstiloVidaDTO atualizarQuestionario(Long consultaId, QuestionarioEstiloVidaDTO dto) {
        QuestionarioEstiloVida questionario = questionarioRepository.findByConsultaId(consultaId).orElseThrow(() -> new ResourceNotFoundException("Questionário não encontrado"));
        
        mapearDTOParaEntidade(dto, questionario);
        
        QuestionarioEstiloVida updated = questionarioRepository.save(questionario);
        return converterParaDTO(updated);
    }
    
    /**
     * Busca questionario vinculado a uma consulta.
     *
     * @param consultaId ID da consulta
     * @return dados do questionario
     */
    @Transactional(readOnly = true)
    public QuestionarioEstiloVidaDTO buscarPorConsulta(Long consultaId) {
        QuestionarioEstiloVida questionario = questionarioRepository.findByConsultaId(consultaId).orElseThrow(() -> new ResourceNotFoundException("Questionário não encontrado"));
        return converterParaDTO(questionario);
    }
    
    /**
     * Remove questionario vinculado a uma consulta.
     *
     * @param consultaId ID da consulta
     */
    @Transactional
    public void deletarQuestionario(Long consultaId) {
        if (!questionarioRepository.findByConsultaId(consultaId).isPresent()) {
            throw new ResourceNotFoundException("Questionário não encontrado");
        }
        questionarioRepository.deleteByConsultaId(consultaId);
    }
    
    /**
     * Mapeia campos do DTO para a entidade, atualizando apenas
     * campos nao nulos no DTO para permitir updates parciais.
     *
     * @param dto DTO com novos valores
     * @param entidade entidade a atualizar
     */
    private void mapearDTOParaEntidade(QuestionarioEstiloVidaDTO dto, QuestionarioEstiloVida entidade) {
        if (dto.getObjetivo() != null) entidade.setObjetivo(dto.getObjetivo());
        if (dto.getFrequenciaTreino() != null) entidade.setFrequenciaTreino(dto.getFrequenciaTreino());
        if (dto.getTempoTreino() != null) entidade.setTempoTreino(dto.getTempoTreino());
        if (dto.getCirurgias() != null) entidade.setCirurgias(dto.getCirurgias());
        if (dto.getDoencas() != null) entidade.setDoencas(dto.getDoencas());
        if (dto.getHistoricoFamiliar() != null) entidade.setHistoricoFamiliar(dto.getHistoricoFamiliar());
        if (dto.getMedicamentos() != null) entidade.setMedicamentos(dto.getMedicamentos());
        if (dto.getSuplementos() != null) entidade.setSuplementos(dto.getSuplementos());
        if (dto.getUsoAnabolizantes() != null) entidade.setUsoAnabolizantes(dto.getUsoAnabolizantes());
        if (dto.getCicloAnabolizantes() != null) entidade.setCicloAnabolizantes(dto.getCicloAnabolizantes());
        if (dto.getDuracaoAnabolizantes() != null) entidade.setDuracaoAnabolizantes(dto.getDuracaoAnabolizantes());
        if (dto.getFuma() != null) entidade.setFuma(dto.getFuma());
        if (dto.getFrequenciaAlcool() != null) entidade.setFrequenciaAlcool(dto.getFrequenciaAlcool());
        if (dto.getFuncionamentoIntestino() != null) entidade.setFuncionamentoIntestino(dto.getFuncionamentoIntestino());
        if (dto.getQualidadeSono() != null) entidade.setQualidadeSono(dto.getQualidadeSono());
        if (dto.getIngestaoAguaDiaria() != null) entidade.setIngestaoAguaDiaria(dto.getIngestaoAguaDiaria());
        if (dto.getAlimentosNaoGosta() != null) entidade.setAlimentosNaoGosta(dto.getAlimentosNaoGosta());
        if (dto.getFrutasPreferidas() != null) entidade.setFrutasPreferidas(dto.getFrutasPreferidas());
        if (dto.getNumeroRefeicoesDesejadas() != null) entidade.setNumeroRefeicoesDesejadas(dto.getNumeroRefeicoesDesejadas());
        if (dto.getHorarioMaiorFome() != null) entidade.setHorarioMaiorFome(dto.getHorarioMaiorFome());
        if (dto.getPressaoArterial() != null) entidade.setPressaoArterial(dto.getPressaoArterial());
        if (dto.getIntolerancias() != null) entidade.setIntolerancias(dto.getIntolerancias());
    }
    
    /**
     * Converte entidade QuestionarioEstiloVida para DTO.
     *
     * @param questionario entidade
     * @return DTO com todos os campos
     */
    private QuestionarioEstiloVidaDTO converterParaDTO(QuestionarioEstiloVida questionario) {
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
}
