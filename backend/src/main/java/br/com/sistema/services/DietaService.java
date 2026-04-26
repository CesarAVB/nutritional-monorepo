package br.com.sistema.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistema.dtos.AlimentoRefeicaoDTO;
import br.com.sistema.dtos.DietaContextoPacienteDTO;
import br.com.sistema.dtos.DietaRequest;
import br.com.sistema.dtos.DietaResponse;
import br.com.sistema.dtos.DietaResumoResponse;
import br.com.sistema.dtos.RefeicaoDTO;
import br.com.sistema.dtos.RefeicaoOpcaoDTO;
import br.com.sistema.dtos.SuplementoDietaDTO;
import br.com.sistema.exceptions.ResourceNotFoundException;
import br.com.sistema.models.AlimentoRefeicao;
import br.com.sistema.models.Dieta;
import br.com.sistema.models.Paciente;
import br.com.sistema.models.Refeicao;
import br.com.sistema.models.RefeicaoOpcao;
import br.com.sistema.models.SuplementoDieta;
import br.com.sistema.repositories.ConsultaRepository;
import br.com.sistema.repositories.DietaRepository;
import br.com.sistema.repositories.PacienteRepository;
import br.com.sistema.repositories.QuestionarioEstiloVidaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DietaService {

    private final DietaRepository dietaRepository;
    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;
    private final QuestionarioEstiloVidaRepository questionarioRepository;

    // ==============================================
    // # buscarContextoPaciente
    // # Retorna objetivo e nº de refeições da última consulta com questionário
    // ==============================================
    @Transactional(readOnly = true)
    public DietaContextoPacienteDTO buscarContextoPaciente(Long pacienteId) {
        pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        return consultaRepository.findFirstByPacienteIdOrderByDataConsultaDesc(pacienteId)
            .flatMap(c -> questionarioRepository.findByConsultaId(c.getId()))
            .map(q -> new DietaContextoPacienteDTO(q.getObjetivo(), q.getNumeroRefeicoesDesejadas()))
            .orElse(new DietaContextoPacienteDTO(null, null));
    }

    // ==============================================
    // # listarPorPaciente
    // # Lista resumo de todas as dietas de um paciente
    // ==============================================
    @Transactional(readOnly = true)
    public List<DietaResumoResponse> listarPorPaciente(Long pacienteId) {
        pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        return dietaRepository.findByPacienteIdOrderByDataCriacaoDesc(pacienteId)
            .stream()
            .map(this::toResumo)
            .collect(Collectors.toList());
    }

    // ==============================================
    // # buscarPorId
    // # Retorna dieta completa com refeições e suplementos
    // ==============================================
    @Transactional(readOnly = true)
    public DietaResponse buscarPorId(Long id) {
        Dieta dieta = dietaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dieta não encontrada"));
        return toResponse(dieta);
    }

    // ==============================================
    // # criar
    // # Cria nova dieta vinculada ao paciente
    // ==============================================
    @Transactional
    public DietaResponse criar(Long pacienteId, DietaRequest request) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        Dieta dieta = new Dieta();
        dieta.setPaciente(paciente);
        dieta.setDataCriacao(LocalDate.now());
        aplicarRequest(dieta, request);

        return toResponse(dietaRepository.save(dieta));
    }

    // ==============================================
    // # atualizar
    // # Atualiza todos os campos de uma dieta existente
    // ==============================================
    @Transactional
    public DietaResponse atualizar(Long id, DietaRequest request) {
        Dieta dieta = dietaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dieta não encontrada"));

        dieta.getRefeicoes().clear();
        dieta.getSuplementos().clear();
        aplicarRequest(dieta, request);

        return toResponse(dietaRepository.save(dieta));
    }

    // ==============================================
    // # deletar
    // # Remove uma dieta pelo ID
    // ==============================================
    @Transactional
    public void deletar(Long id) {
        Dieta dieta = dietaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dieta não encontrada"));
        dietaRepository.delete(dieta);
    }

    // ==============================================
    // # Métodos privados de mapeamento
    // ==============================================

    private void aplicarRequest(Dieta dieta, DietaRequest req) {
        dieta.setTitulo(req.getTitulo());
        dieta.setObjetivo(req.getObjetivo());
        dieta.setKcalTotal(req.getKcalTotal());
        dieta.setProteinasG(req.getProteinasG());
        dieta.setCarboidratosG(req.getCarboidratosG());
        dieta.setGordurasG(req.getGordurasG());
        dieta.setIngestaoAguaLitros(req.getIngestaoAguaLitros());
        dieta.setObservacoes(req.getObservacoes());

        if (req.getRefeicoes() != null) {
            for (DietaRequest.RefeicaoRequest rReq : req.getRefeicoes()) {
                Refeicao refeicao = new Refeicao();
                refeicao.setDieta(dieta);
                refeicao.setTipo(rReq.getTipo());
                refeicao.setOrdemExibicao(rReq.getOrdemExibicao() != null ? rReq.getOrdemExibicao() : 0);

                if (rReq.getOpcoes() != null) {
                    for (DietaRequest.OpcaoRequest oReq : rReq.getOpcoes()) {
                        RefeicaoOpcao opcao = new RefeicaoOpcao();
                        opcao.setRefeicao(refeicao);
                        opcao.setNumeroOpcao(oReq.getNumeroOpcao() != null ? oReq.getNumeroOpcao() : 0);

                        if (oReq.getAlimentos() != null) {
                            int ordem = 0;
                            for (DietaRequest.AlimentoRequest aReq : oReq.getAlimentos()) {
                                AlimentoRefeicao alimento = new AlimentoRefeicao();
                                alimento.setOpcao(opcao);
                                alimento.setNome(aReq.getNome());
                                alimento.setQuantidade(aReq.getQuantidade());
                                alimento.setUnidade(aReq.getUnidade());
                                alimento.setOrdem(aReq.getOrdem() != null ? aReq.getOrdem() : ordem++);
                                opcao.getAlimentos().add(alimento);
                            }
                        }
                        refeicao.getOpcoes().add(opcao);
                    }
                }
                dieta.getRefeicoes().add(refeicao);
            }
        }

        if (req.getSuplementos() != null) {
            for (DietaRequest.SuplementoRequest sReq : req.getSuplementos()) {
                SuplementoDieta suplemento = new SuplementoDieta();
                suplemento.setDieta(dieta);
                suplemento.setNome(sReq.getNome());
                suplemento.setDosagem(sReq.getDosagem());
                suplemento.setTiming(sReq.getTiming());
                dieta.getSuplementos().add(suplemento);
            }
        }
    }

    private DietaResumoResponse toResumo(Dieta dieta) {
        return new DietaResumoResponse(
            dieta.getId(),
            dieta.getTitulo(),
            dieta.getDataCriacao(),
            dieta.getObjetivo(),
            dieta.getKcalTotal(),
            dieta.getRefeicoes().size()
        );
    }

    private DietaResponse toResponse(Dieta dieta) {
        DietaResponse resp = new DietaResponse();
        resp.setId(dieta.getId());
        resp.setPacienteId(dieta.getPaciente().getId());
        resp.setNomePaciente(dieta.getPaciente().getNomeCompleto());
        resp.setTitulo(dieta.getTitulo());
        resp.setDataCriacao(dieta.getDataCriacao());
        resp.setObjetivo(dieta.getObjetivo());
        resp.setKcalTotal(dieta.getKcalTotal());
        resp.setProteinasG(dieta.getProteinasG());
        resp.setCarboidratosG(dieta.getCarboidratosG());
        resp.setGordurasG(dieta.getGordurasG());
        resp.setIngestaoAguaLitros(dieta.getIngestaoAguaLitros());
        resp.setObservacoes(dieta.getObservacoes());

        resp.setRefeicoes(dieta.getRefeicoes().stream().map(r -> {
            RefeicaoDTO rDTO = new RefeicaoDTO();
            rDTO.setId(r.getId());
            rDTO.setTipo(r.getTipo());
            rDTO.setOrdemExibicao(r.getOrdemExibicao());
            rDTO.setOpcoes(r.getOpcoes().stream().map(o -> {
                RefeicaoOpcaoDTO oDTO = new RefeicaoOpcaoDTO();
                oDTO.setId(o.getId());
                oDTO.setNumeroOpcao(o.getNumeroOpcao());
                oDTO.setAlimentos(o.getAlimentos().stream().map(a -> {
                    AlimentoRefeicaoDTO aDTO = new AlimentoRefeicaoDTO();
                    aDTO.setId(a.getId());
                    aDTO.setNome(a.getNome());
                    aDTO.setQuantidade(a.getQuantidade());
                    aDTO.setUnidade(a.getUnidade());
                    aDTO.setOrdem(a.getOrdem());
                    return aDTO;
                }).collect(Collectors.toList()));
                return oDTO;
            }).collect(Collectors.toList()));
            return rDTO;
        }).collect(Collectors.toList()));

        resp.setSuplementos(dieta.getSuplementos().stream().map(s -> {
            SuplementoDietaDTO sDTO = new SuplementoDietaDTO();
            sDTO.setId(s.getId());
            sDTO.setNome(s.getNome());
            sDTO.setDosagem(s.getDosagem());
            sDTO.setTiming(s.getTiming());
            return sDTO;
        }).collect(Collectors.toList()));

        return resp;
    }
}
