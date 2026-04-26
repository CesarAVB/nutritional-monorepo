package br.com.sistema.services;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.sistema.dtos.RegistroFotograficoDTO;
import br.com.sistema.exceptions.BusinessException;
import br.com.sistema.exceptions.ResourceNotFoundException;
import br.com.sistema.models.Consulta;
import br.com.sistema.models.RegistroFotografico;
import br.com.sistema.repositories.ConsultaRepository;
import br.com.sistema.repositories.RegistroFotograficoRepository;
import lombok.RequiredArgsConstructor;

/**
 * Gerencia registros fotográficos de consultas nutricionais.
 * Utiliza S3Service para persistir imagens e gerar URLs temporárias (presigned).
 */
@Service
@RequiredArgsConstructor
public class RegistroFotograficoService {

    private final RegistroFotograficoRepository registroFotograficoRepository;
    private final ConsultaRepository consultaRepository;
    private final S3Service s3Service;
    private static final String FOLDER_FOTOS = "fotos-consultas";

    /**
     * Cria um novo registro fotográfico para uma consulta, fazendo upload das fotos no S3.
     * Exige que a consulta exista e que não haja registro prévio para evitar duplicação.
     *
     * @param consultaId              ID da consulta associada
     * @param fotoAnterior            Foto anterior do paciente (opcional)
     * @param fotoPosterior           Foto posterior do paciente (opcional)
     * @param fotoLateralEsquerda     Foto lateral esquerda (opcional)
     * @param fotoLateralDireita      Foto lateral direita (opcional)
     * @return DTO com as chaves S3 das fotos salvas
     * @throws ResourceNotFoundException se a consulta não for encontrada
     * @throws BusinessException        se já existir registro fotográfico para esta consulta
     */
    @Transactional
    public RegistroFotograficoDTO salvarRegistro(Long consultaId, MultipartFile fotoAnterior, MultipartFile fotoPosterior, MultipartFile fotoLateralEsquerda, MultipartFile fotoLateralDireita) {
        Consulta consulta = consultaRepository.findById(consultaId).orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));

        if (registroFotograficoRepository.existsByConsultaId(consultaId)) {
            throw new BusinessException("Já existe um registro fotográfico para esta consulta");
        }

        RegistroFotografico registro = new RegistroFotografico();
        registro.setConsulta(consulta);

        if (fotoAnterior != null && !fotoAnterior.isEmpty()) {
            registro.setFotoAnterior(s3Service.uploadFile(fotoAnterior, FOLDER_FOTOS));
        }
        if (fotoPosterior != null && !fotoPosterior.isEmpty()) {
            registro.setFotoPosterior(s3Service.uploadFile(fotoPosterior, FOLDER_FOTOS));
        }
        if (fotoLateralEsquerda != null && !fotoLateralEsquerda.isEmpty()) {
            registro.setFotoLateralEsquerda(s3Service.uploadFile(fotoLateralEsquerda, FOLDER_FOTOS));
        }
        if (fotoLateralDireita != null && !fotoLateralDireita.isEmpty()) {
            registro.setFotoLateralDireita(s3Service.uploadFile(fotoLateralDireita, FOLDER_FOTOS));
        }

        RegistroFotografico saved = registroFotograficoRepository.save(registro);
        return converterParaDTO(saved);
    }

    /**
     * Atualiza um registro fotográfico existente ou cria um novo (upsert) para a consulta.
     * Suporta substituição de imagens e remoção explícita via flags.
     *
     * @param consultaId                   ID da consulta associada
     * @param fotoAnterior                 Nova foto anterior (opcional)
     * @param fotoPosterior                Nova foto posterior (opcional)
     * @param fotoLateralEsquerda          Nova foto lateral esquerda (opcional)
     * @param fotoLateralDireita           Nova foto lateral direita (opcional)
     * @param removerFotoAnterior           Flag para remover foto anterior
     * @param removerFotoPosterior          Flag para remover foto posterior
     * @param removerFotoLateralEsquerda    Flag para remover foto lateral esquerda
     * @param removerFotoLateralDireita     Flag para remover foto lateral direita
     * @return DTO com as chaves S3 atualizadas
     */
    @Transactional
    public RegistroFotograficoDTO atualizarRegistro(Long consultaId, MultipartFile fotoAnterior, MultipartFile fotoPosterior, MultipartFile fotoLateralEsquerda, MultipartFile fotoLateralDireita,        
            Boolean removerFotoAnterior, Boolean removerFotoPosterior, Boolean removerFotoLateralEsquerda, Boolean removerFotoLateralDireita) {
        
        // Se não existir, cria. Se existir, atualiza (UPSERT)
        RegistroFotografico registro = registroFotograficoRepository.findByConsultaId(consultaId).orElseGet(() -> {
                Consulta consulta = consultaRepository.findById(consultaId).orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));
                RegistroFotografico novo = new RegistroFotografico();
                novo.setConsulta(consulta);
                return novo;
            });

        // Atualizar foto anterior
        if (fotoAnterior != null && !fotoAnterior.isEmpty()) {
            if (registro.getFotoAnterior() != null) {
                s3Service.deleteFile(registro.getFotoAnterior());
            }
            registro.setFotoAnterior(s3Service.uploadFile(fotoAnterior, FOLDER_FOTOS));
        } else if (removerFotoAnterior != null && removerFotoAnterior) {
            if (registro.getFotoAnterior() != null) {
                s3Service.deleteFile(registro.getFotoAnterior());
                registro.setFotoAnterior(null);
            }
        }

        // Atualizar foto posterior
        if (fotoPosterior != null && !fotoPosterior.isEmpty()) {
            if (registro.getFotoPosterior() != null) {
                s3Service.deleteFile(registro.getFotoPosterior());
            }
            registro.setFotoPosterior(s3Service.uploadFile(fotoPosterior, FOLDER_FOTOS));
        } else if (removerFotoPosterior != null && removerFotoPosterior) {
            if (registro.getFotoPosterior() != null) {
                s3Service.deleteFile(registro.getFotoPosterior());
                registro.setFotoPosterior(null);
            }
        }

        // Atualizar foto lateral esquerda
        if (fotoLateralEsquerda != null && !fotoLateralEsquerda.isEmpty()) {
            if (registro.getFotoLateralEsquerda() != null) {
                s3Service.deleteFile(registro.getFotoLateralEsquerda());
            }
            registro.setFotoLateralEsquerda(s3Service.uploadFile(fotoLateralEsquerda, FOLDER_FOTOS));
        } else if (removerFotoLateralEsquerda != null && removerFotoLateralEsquerda) {
            if (registro.getFotoLateralEsquerda() != null) {
                s3Service.deleteFile(registro.getFotoLateralEsquerda());
                registro.setFotoLateralEsquerda(null);
            }
        }

        // Atualizar foto lateral direita
        if (fotoLateralDireita != null && !fotoLateralDireita.isEmpty()) {
            if (registro.getFotoLateralDireita() != null) {
                s3Service.deleteFile(registro.getFotoLateralDireita());
            }
            registro.setFotoLateralDireita(s3Service.uploadFile(fotoLateralDireita, FOLDER_FOTOS));
        } else if (removerFotoLateralDireita != null && removerFotoLateralDireita) {
            if (registro.getFotoLateralDireita() != null) {
                s3Service.deleteFile(registro.getFotoLateralDireita());
                registro.setFotoLateralDireita(null);
            }
        }

        RegistroFotografico updated = registroFotograficoRepository.save(registro);
        return converterParaDTO(updated);
    }

    /**
     * Busca o registro fotográfico de uma consulta e retorna com URLs temporárias de acesso.
     * Gera presigned URLs válidas por 1 hora para segurança do arquivo.
     *
     * @param consultaId ID da consulta
     * @return DTO com URLs temporárias das fotos
     * @throws ResourceNotFoundException se o registro não for encontrado
     */
    @Transactional(readOnly = true)
    public RegistroFotograficoDTO buscarPorConsulta(Long consultaId) {
        RegistroFotografico registro = registroFotograficoRepository.findByConsultaId(consultaId).orElseThrow(() -> new ResourceNotFoundException("Registro fotográfico não encontrado"));
        return converterParaDTOComPresignedUrl(registro);
    }

    /**
     * Remove o registro fotográfico e todas as fotos associadas no S3.
     *
     * @param consultaId ID da consulta cujo registro será removido
     * @throws ResourceNotFoundException se o registro não for encontrado
     */
    @Transactional
    public void deletarRegistro(Long consultaId) {
        RegistroFotografico registro = registroFotograficoRepository.findByConsultaId(consultaId).orElseThrow(() -> new ResourceNotFoundException("Registro fotográfico não encontrado"));
        if (registro.getFotoAnterior() != null) s3Service.deleteFile(registro.getFotoAnterior());
        if (registro.getFotoPosterior() != null) s3Service.deleteFile(registro.getFotoPosterior());
        if (registro.getFotoLateralEsquerda() != null) s3Service.deleteFile(registro.getFotoLateralEsquerda());
        if (registro.getFotoLateralDireita() != null) s3Service.deleteFile(registro.getFotoLateralDireita());
        registroFotograficoRepository.deleteByConsultaId(consultaId);
    }

    /**
     * Converte a entidade para DTO retornando apenas as chaves S3 (sem URL temporária).
     * Usado após operações de salvar/atualizar onde a key é suficiente.
     */
    private RegistroFotograficoDTO converterParaDTO(RegistroFotografico registro) {
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
     * Converte a entidade para DTO gerando presigned URLs para acesso temporário.
     * URLs têm validade de 1 hora para garantir segurança no acesso às fotos.
     */
    private RegistroFotograficoDTO converterParaDTOComPresignedUrl(RegistroFotografico registro) {
        RegistroFotograficoDTO dto = new RegistroFotograficoDTO();
        dto.setId(registro.getId());
        dto.setConsultaId(registro.getConsulta().getId());

        if (registro.getFotoAnterior() != null) {
            String presigned = s3Service.generatePresignedUrl(registro.getFotoAnterior(), Duration.ofHours(1));
            dto.setFotoAnterior(presigned);
        }
        if (registro.getFotoPosterior() != null) {
            String presigned = s3Service.generatePresignedUrl(registro.getFotoPosterior(), Duration.ofHours(1));
            dto.setFotoPosterior(presigned);
        }
        if (registro.getFotoLateralEsquerda() != null) {
            String presigned = s3Service.generatePresignedUrl(registro.getFotoLateralEsquerda(), Duration.ofHours(1));
            dto.setFotoLateralEsquerda(presigned);
        }
        if (registro.getFotoLateralDireita() != null) {
            String presigned = s3Service.generatePresignedUrl(registro.getFotoLateralDireita(), Duration.ofHours(1));
            dto.setFotoLateralDireita(presigned);
        }
        return dto;
    }

}

