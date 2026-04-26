package br.com.sistema.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistema.dtos.AvaliacaoFisicaDTO;
import br.com.sistema.exceptions.BusinessException;
import br.com.sistema.exceptions.ResourceNotFoundException;
import br.com.sistema.models.AvaliacaoFisica;
import br.com.sistema.models.Consulta;
import br.com.sistema.models.Paciente;
import br.com.sistema.repositories.AvaliacaoFisicaRepository;
import br.com.sistema.repositories.ConsultaRepository;
import br.com.sistema.utils.CalculosNutricionais;
import lombok.RequiredArgsConstructor;

/**
 * Gerencia avaliacoes fisicas vinculadas a consultas.
 * Calcula automaticamente IMC, percentual de gordura, massa magra e massa gorda
 * com base nas medidas preenchidas pelo nutricionista.
 */
@Service
@RequiredArgsConstructor
public class AvaliacaoFisicaService {

    private final AvaliacaoFisicaRepository avaliacaoFisicaRepository;
    private final ConsultaRepository consultaRepository;

    /**
     * Salva uma nova avaliacao fisica para uma consulta.
     * Calcula automaticamente IMC, percentual de gordura e composicao corporal.
     * Lanca BusinessException se ja existir avaliacao para a consulta.
     *
     * @param consultaId ID da consulta
     * @param dto Dados da avaliacao fisica
     * @return Avaliacao fisica salva com campos calculados
     */
    @Transactional
    public AvaliacaoFisicaDTO salvarAvaliacao(Long consultaId, AvaliacaoFisicaDTO dto) {
        Consulta consulta = consultaRepository.findById(consultaId).orElseThrow(() -> new ResourceNotFoundException("Consulta nao encontrada"));

        if (avaliacaoFisicaRepository.existsByConsultaId(consultaId)) {
            throw new BusinessException("Ja existe uma avaliacao fisica para esta consulta");
        }

        AvaliacaoFisica avaliacao = new AvaliacaoFisica();
        avaliacao.setConsulta(consulta);
        mapearDTOParaEntidade(dto, avaliacao);
        calcularDadosAutomaticos(avaliacao, consulta.getPaciente());
        AvaliacaoFisica saved = avaliacaoFisicaRepository.save(avaliacao);
        return converterParaDTO(saved);
    }

    /**
     * Atualiza uma avaliacao fisica existente e recalcula dados automaticos.
     * Usado quando o nutricionista altera medidas ou parametros.
     *
     * @param consultaId ID da consulta
     * @param dto Novos dados da avaliacao
     * @return Avaliacao atualizada com campos recalculados
     */
    @Transactional
    public AvaliacaoFisicaDTO atualizarAvaliacao(Long consultaId, AvaliacaoFisicaDTO dto) {
        AvaliacaoFisica avaliacao = avaliacaoFisicaRepository.findByConsultaId(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliacao fisica nao encontrada"));
        mapearDTOParaEntidade(dto, avaliacao);
        calcularDadosAutomaticos(avaliacao, avaliacao.getConsulta().getPaciente());
        AvaliacaoFisica updated = avaliacaoFisicaRepository.save(avaliacao);
        return converterParaDTO(updated);
    }

    /**
     * Busca avaliacao fisica vinculada a uma consulta.
     *
     * @param consultaId ID da consulta
     * @return Dados da avaliacao fisica
     */
    @Transactional(readOnly = true)
    public AvaliacaoFisicaDTO buscarPorConsulta(Long consultaId) {
        AvaliacaoFisica avaliacao = avaliacaoFisicaRepository.findByConsultaId(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliacao fisica nao encontrada"));
        return converterParaDTO(avaliacao);
    }

    /**
     * Remove avaliacao fisica vinculada a uma consulta.
     *
     * @param consultaId ID da consulta
     */
    @Transactional
    public void deletarAvaliacao(Long consultaId) {
        if (!avaliacaoFisicaRepository.existsByConsultaId(consultaId)) {
            throw new ResourceNotFoundException("Avaliacao fisica nao encontrada");
        }
        avaliacaoFisicaRepository.deleteByConsultaId(consultaId);
    }

    /**
     * Copia campos do DTO para a entidade, ignorando valores nulos.
     * Permite atualizacao parcial sem sobrescrever dados existentes.
     */
    private void mapearDTOParaEntidade(AvaliacaoFisicaDTO dto, AvaliacaoFisica entidade) {
        if (dto.getAltura() != null) entidade.setAltura(dto.getAltura());
        if (dto.getPerimetroOmbro() != null) entidade.setPerimetroOmbro(dto.getPerimetroOmbro());
        if (dto.getPerimetroTorax() != null) entidade.setPerimetroTorax(dto.getPerimetroTorax());
        if (dto.getPerimetroCintura() != null) entidade.setPerimetroCintura(dto.getPerimetroCintura());
        if (dto.getPerimetroAbdominal() != null) entidade.setPerimetroAbdominal(dto.getPerimetroAbdominal());
        if (dto.getPerimetroQuadril() != null) entidade.setPerimetroQuadril(dto.getPerimetroQuadril());
        if (dto.getPerimetroBracoDireitoRelax() != null) entidade.setPerimetroBracoDireitoRelax(dto.getPerimetroBracoDireitoRelax());
        if (dto.getPerimetroBracoDireitoContr() != null) entidade.setPerimetroBracoDireitoContr(dto.getPerimetroBracoDireitoContr());
        if (dto.getPerimetroBracoEsquerdoRelax() != null) entidade.setPerimetroBracoEsquerdoRelax(dto.getPerimetroBracoEsquerdoRelax());
        if (dto.getPerimetroBracoEsquerdoContr() != null) entidade.setPerimetroBracoEsquerdoContr(dto.getPerimetroBracoEsquerdoContr());
        if (dto.getPerimetroAntebracoDireito() != null) entidade.setPerimetroAntebracoDireito(dto.getPerimetroAntebracoDireito());
        if (dto.getPerimetroAntebracoEsquerdo() != null) entidade.setPerimetroAntebracoEsquerdo(dto.getPerimetroAntebracoEsquerdo());
        if (dto.getPerimetroCoxa() != null) entidade.setPerimetroCoxa(dto.getPerimetroCoxa());
        if (dto.getPerimetroCoxaDireita() != null) entidade.setPerimetroCoxaDireita(dto.getPerimetroCoxaDireita());
        if (dto.getPerimetroCoxaEsquerda() != null) entidade.setPerimetroCoxaEsquerda(dto.getPerimetroCoxaEsquerda());
        if (dto.getPerimetroPanturrilhaDireita() != null) entidade.setPerimetroPanturrilhaDireita(dto.getPerimetroPanturrilhaDireita());
        if (dto.getPerimetroPanturrilhaEsquerda() != null) entidade.setPerimetroPanturrilhaEsquerda(dto.getPerimetroPanturrilhaEsquerda());
        if (dto.getDobraTriceps() != null) entidade.setDobraTriceps(dto.getDobraTriceps());
        if (dto.getDobraPeito() != null) entidade.setDobraPeito(dto.getDobraPeito());
        if (dto.getDobraAxilarMedia() != null) entidade.setDobraAxilarMedia(dto.getDobraAxilarMedia());
        if (dto.getDobraSubescapular() != null) entidade.setDobraSubescapular(dto.getDobraSubescapular());
        if (dto.getDobraAbdominal() != null) entidade.setDobraAbdominal(dto.getDobraAbdominal());
        if (dto.getDobraSupraIliaca() != null) entidade.setDobraSupraIliaca(dto.getDobraSupraIliaca());
        if (dto.getDobraCoxa() != null) entidade.setDobraCoxa(dto.getDobraCoxa());
        if (dto.getPesoAtual() != null) entidade.setPesoAtual(dto.getPesoAtual());
    }

    /**
     * Converte entidade AvaliacaoFisica para DTO.
     */
    private AvaliacaoFisicaDTO converterParaDTO(AvaliacaoFisica avaliacao) {
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
     * Calcula automaticamente IMC, percentual de gordura, massa gorda e massa magra.
     * Usa todas as 7 dobras cutaneas para calcular o % de gordura via protocolo Pollock.
     * Alturas em centimetros sao convertidas para metros antes do calculo do IMC.
     */
    private void calcularDadosAutomaticos(AvaliacaoFisica avaliacao, Paciente paciente) {
        if (avaliacao.getPesoAtual() != null && avaliacao.getAltura() != null) {
            double alturaOriginal = avaliacao.getAltura();
            double alturaParaCalculo = alturaOriginal > 10 ? alturaOriginal / 100.0 : alturaOriginal;
            Double imc = CalculosNutricionais.calcularIMC(avaliacao.getPesoAtual(), alturaParaCalculo);
            if (imc != null) {
                avaliacao.setImc(imc);
            }
        }

        if (todasDobrasPreenchidas(avaliacao)) {
            Integer idade = CalculosNutricionais.calcularIdade(paciente.getDataNascimento());
            Double percentualGordura = CalculosNutricionais.calcularPercentualGordura(
                    paciente.getSexo(),
                    idade,
                    avaliacao.getDobraTriceps(),
                    avaliacao.getDobraPeito(),
                    avaliacao.getDobraAxilarMedia(),
                    avaliacao.getDobraSubescapular(),
                    avaliacao.getDobraAbdominal(),
                    avaliacao.getDobraSupraIliaca(),
                    avaliacao.getDobraCoxa()
            );

            if (percentualGordura != null) {
                avaliacao.setPercentualGordura(percentualGordura);
            }

            if (percentualGordura != null && avaliacao.getPesoAtual() != null) {
                Double massaGorda = CalculosNutricionais.calcularMassaGorda(avaliacao.getPesoAtual(), percentualGordura);
                if (massaGorda != null) {
                    avaliacao.setMassaGorda(massaGorda);
                }

                Double massaMagra = CalculosNutricionais.calcularMassaMagra(avaliacao.getPesoAtual(), massaGorda);
                if (massaMagra != null) {
                    avaliacao.setMassaMagra(massaMagra);
                }
            }
        }
    }

    /**
     * Verifica se todas as 7 dobras cutaneas do protocolo Pollock estao preenchidas.
     */
    private boolean todasDobrasPreenchidas(AvaliacaoFisica avaliacao) {
        return avaliacao.getDobraTriceps() != null
                && avaliacao.getDobraPeito() != null
                && avaliacao.getDobraAxilarMedia() != null
                && avaliacao.getDobraSubescapular() != null
                && avaliacao.getDobraAbdominal() != null
                && avaliacao.getDobraSupraIliaca() != null
                && avaliacao.getDobraCoxa() != null;
    }
}
