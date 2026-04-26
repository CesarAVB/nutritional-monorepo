package br.com.sistema.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.sistema.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * Abstração para operações de armazenamento de arquivos no S3/MinIO.
 * Gerencia upload, exclusão e geração de URLs temporárias (presigned) para objetos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * Faz upload de um arquivo MultipartFile para o bucket S3, organizando por pasta.
     * Valida tipo e tamanho antes do envio. Retorna a key do objeto no bucket.
     *
     * @param file   Arquivo a ser enviado (imagem)
     * @param folder Pasta de destino no bucket
     * @return Key do objeto criado no S3
     * @throws BusinessException se o arquivo for inválido ou o upload falhar
     */
    public String uploadFile(MultipartFile file, String folder) {
        validateFile(file);
        String fileName = generateFileName(file.getOriginalFilename());
        String key = folder + "/" + fileName;

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

            log.info("Arquivo enviado com sucesso: {}", key);
            return key;

        } catch (S3Exception e) {
            log.error("Erro ao fazer upload no S3: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao fazer upload da imagem: " + e.awsErrorDetails().errorMessage());
        } catch (IOException e) {
            log.error("Erro ao ler arquivo: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao processar arquivo");
        }
    }

    /**
     * Faz upload de um array de bytes para o bucket S3, útil para arquivos gerados em memória.
     *
     * @param bytes        Conteúdo do arquivo em bytes
     * @param fileName     Nome original do arquivo (usado para extrair extensão)
     * @param contentType  Tipo MIME do conteúdo
     * @param folder       Pasta de destino no bucket
     * @return Key do objeto criado no S3
     * @throws BusinessException se o upload falhar
     */
    public String uploadBytes(byte[] bytes, String fileName, String contentType, String folder) {
        String key = folder + "/" + generateFileName(fileName);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) bytes.length)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(new ByteArrayInputStream(bytes), bytes.length));

            log.info("Bytes enviados com sucesso: {}", key);
            return key;

        } catch (S3Exception e) {
            log.error("Erro ao fazer upload no S3: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao fazer upload da imagem");
        }
    }

    /**
     * Remove um objeto do bucket a partir da URL completa ou key do arquivo.
     *
     * @param fileUrl URL completa ou key do arquivo no S3
     * @throws BusinessException se a URL for inválida ou a exclusão falhar
     */
    public void deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Arquivo deletado com sucesso: {}", key);

        } catch (S3Exception e) {
            log.error("Erro ao deletar arquivo no S3: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao deletar arquivo");
        }
    }

    /**
     * Gera uma URL presigned para acesso temporário a um objeto no S3.
     * Útil para conceder acesso ????????? sem expor credenciais.
     *
     * @param key      Key do objeto no bucket
     * @param duration Tempo de validade da URL
     * @return URL presigned para download do objeto
     * @throws BusinessException se a geração da URL falhar
     */
    public String generatePresignedUrl(String key, Duration duration) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (S3Exception e) {
            log.error("Erro ao gerar presigned URL: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao gerar link da imagem");
        }
    }

    /**
     * Extrai a key do objeto a partir de uma URL completa do S3/MinIO.
     * Usa o endpoint configurado e nome do bucket para remover o prefixo.
     */
    private String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new BusinessException("URL do arquivo inválida");
        }

        String baseUrl = String.format("%s/%s/", endpoint, bucketName);
        return fileUrl.replace(baseUrl, "");
    }

    /**
     * Gera um nome de arquivo único usando UUID, preservando a extensão original.
     * Evita colisões de nomes no bucket.
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Valida arquivo quanto a tamanho (máx 5MB) e tipo (apenas imagens).
     * @throws BusinessException se o arquivo não atender aos critérios
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo não pode ser vazio");
        }

        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new BusinessException("Arquivo muito grande. Tamanho máximo: 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("Apenas arquivos de imagem são permitidos");
        }
    }

    /**
     * Retorna o nome do bucket configurado para operações de armazenamento.
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Retorna o endpoint do S3/MinIO configurado.
     */
    public String getEndpoint() {
        return endpoint;
    }
}

