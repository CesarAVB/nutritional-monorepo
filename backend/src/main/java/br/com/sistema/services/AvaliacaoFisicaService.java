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

@Service
@RequiredArgsConstructor
public class AvaliacaoFisicaService {
    
    private final AvaliacaoFisicaRepository avaliacaoFisicaRepository;
    private final ConsultaRepository consultaRepository;
    
    // ==============================================
    // # Método - salvarAvaliacao
    // # Salva uma nova avaliação física vinculada a uma consulta
    // ==============================================
    @Transactional
    public AvaliacaoFisicaDTO salvarAvaliacao(Long consultaId, AvaliacaoFisicaDTO dto) {
        Consulta consulta = consultaRepository.findById(consultaId).orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));
        
        if (avaliacaoFisicaRepository.existsByConsultaId(consultaId)) {
            throw new BusinessException("Já existe uma avaliação física para esta consulta");
        }
        
        AvaliacaoFisica avaliacao = new AvaliacaoFisica();
        avaliacao.setConsulta(consulta);
        mapearDTOParaEntidade(dto, avaliacao);
        calcularDadosAutomaticos(avaliacao, consulta.getPaciente());
        System.out.println("Avaliacao antes de salvar (entidade): " + avaliacao);
        AvaliacaoFisica saved = avaliacaoFisicaRepository.save(avaliacao);
        System.out.println("Avaliacao após salvar (entidade): " + saved);
        return converterParaDTO(saved);
    }
    
    // ==============================================
    // # Método - atualizarAvaliacao
    // # Atualiza uma avaliação física existente e recalcula dados
    // ==============================================
    @Transactional
    public AvaliacaoFisicaDTO atualizarAvaliacao(Long consultaId, AvaliacaoFisicaDTO dto) {
    	System.err.println("Atualizando avaliação física para consulta ID: " + consultaId);
        AvaliacaoFisica avaliacao = avaliacaoFisicaRepository.findByConsultaId(consultaId).orElseThrow(() -> new ResourceNotFoundException("Avaliação física não encontrada"));
        mapearDTOParaEntidade(dto, avaliacao);
        // Recalcula os dados automáticos caso o frontend tenha alterado peso ou dobras
        calcularDadosAutomaticos(avaliacao, avaliacao.getConsulta().getPaciente());
        AvaliacaoFisica updated = avaliacaoFisicaRepository.save(avaliacao);
        return converterParaDTO(updated);
    }
    
    // ==============================================
    // # Método - buscarPorConsulta
    // # Busca avaliação física por consulta
    // ==============================================
    @Transactional(readOnly = true)
    public AvaliacaoFisicaDTO buscarPorConsulta(Long consultaId) {
        AvaliacaoFisica avaliacao = avaliacaoFisicaRepository.findByConsultaId(consultaId).orElseThrow(() -> new ResourceNotFoundException("Avaliação física não encontrada"));
        return converterParaDTO(avaliacao);
    }
    
    // ==============================================
    // # Método - deletarAvaliacao
    // # Deleta avaliação física por consulta
    // ==============================================
    @Transactional
    public void deletarAvaliacao(Long consultaId) {
        if (!avaliacaoFisicaRepository.existsByConsultaId(consultaId)) {
            throw new ResourceNotFoundException("Avaliação física não encontrada");
        }
        avaliacaoFisicaRepository.deleteByConsultaId(consultaId);
    }
    
    // ==============================================
    // # Método - mapearDTOParaEntidade
    // # Mapear campos do DTO para a entidade sem sobrescrever nulos
    // ==============================================
    private void mapearDTOParaEntidade(AvaliacaoFisicaDTO dto, AvaliacaoFisica entidade) {
    	System.out.println("Mapeando DTO para Entidade: " + dto);
    	
    	// ATUALIZAR APENAS SE O CAMPO NÃO FOR NULL
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
        // NÃO mapear campos calculados (IMC, %gordura, massa gorda/magra) a partir do DTO
        // Eles serão recalculados por `calcularDadosAutomaticos` com base em peso, dobras e paciente
    }

    // ==============================================
    // # Método - converterParaDTO
    // # Converte AvaliacaoFisica para AvaliacaoFisicaDTO
    // ==============================================
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
    
    // ==============================================
    // # Método - calcularDadosAutomaticos
    // # Calcula IMC, %gordura, massa gorda e massa magra quando possível
    // ==============================================
    private void calcularDadosAutomaticos(AvaliacaoFisica avaliacao, Paciente paciente) {
       
        // 1. Calcular IMC
        if (avaliacao.getPesoAtual() != null && avaliacao.getAltura() != null) {
            double alturaOriginal = avaliacao.getAltura();
            double alturaParaCalculo = alturaOriginal;
            // Se a altura foi enviada em centímetros (ex: 175), converte para metros
            if (alturaOriginal > 10) { // valores >10 normalmente significam centímetros
                alturaParaCalculo = alturaOriginal / 100.0;
                System.out.println("Ajustando altura para cálculo de IMC (convertendo cm -> m): " + alturaOriginal + " -> " + alturaParaCalculo);
            }
            Double imc = CalculosNutricionais.calcularIMC(avaliacao.getPesoAtual(), alturaParaCalculo);
            if (imc != null) {
                avaliacao.setImc(imc);
            } else {
                System.out.println("IMC não calculado: peso=" + avaliacao.getPesoAtual() + ", alturaParaCalculo=" + alturaParaCalculo);
            }
        } else {
            System.out.println("IMC não calculado porque peso ou altura estão nulos; peso=" + avaliacao.getPesoAtual() + ", altura=" + avaliacao.getAltura());
        }
        
        // 2. Calcular % Gordura (se todas as 7 dobras estiverem preenchidas)
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
            } else {
                System.out.println("Percentual de gordura não calculado (resultado null)");
            }
            
            // 3. Calcular Massa Gorda e Massa Magra
            if (percentualGordura != null && avaliacao.getPesoAtual() != null) {
                Double massaGorda = CalculosNutricionais.calcularMassaGorda(avaliacao.getPesoAtual(), percentualGordura);
                if (massaGorda != null) {
                    avaliacao.setMassaGorda(massaGorda);
                }
                
                Double massaMagra = CalculosNutricionais.calcularMassaMagra(
                    avaliacao.getPesoAtual(), 
                    massaGorda
                );
                if (massaMagra != null) {
                    avaliacao.setMassaMagra(massaMagra);
                }
            }
        } else {
            System.out.println("Percentual de gordura não calculado: nem todas as 7 dobras estão preenchidas");
        }
    }
    
    // ==============================================
    // # Método - todasDobrasPreenchidas
    // # Verifica se todas as 7 dobras cutâneas estão preenchidas
    // ==============================================
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