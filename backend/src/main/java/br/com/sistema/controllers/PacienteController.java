package br.com.sistema.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.sistema.dtos.PacienteDTO;
import br.com.sistema.models.AuditEventMessage;
import br.com.sistema.services.AuditProducerService;
import br.com.sistema.services.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador responsavel pelo gerenciamento de pacientes do sistema.
 * Gerencia o ciclo de vida completo: cadastro, consulta, atualizacao e remocao,
 * alem de integrar com o servico de auditoria para rastreabilidade de operacoes.
 *
 * <p>Todo novo cadastro gera evento de auditoria enviado para fila RabbitMQ,
 * permitindo rastreamento de quem/cuando/onde foi realizado o cadastro.</p>
 */
@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pacientes", description = "Endpoints para gestao de pacientes")
public class PacienteController {
    
    private final PacienteService pacienteService;
    private final AuditProducerService auditProducerService;
    private final ObjectMapper objectMapper;
    
    /**
     * Cadastra novo paciente no sistema e envia evento de auditoria.
     * Valida unicidade de CPF antes da persistencia. Falhas na auditoria
     * nao bloqueiam a operacao de negocio.
     *
     * @param dto Dados do paciente a ser cadastrado
     * @param request Contexto HTTP para captura de IP do cliente
     * @return Paciente criado com status HTTP 201
     */
    @PostMapping
    @Operation(summary = "Cadastrar novo paciente", description = "Cria um novo paciente no sistema")
    public ResponseEntity<PacienteDTO> cadastrar(@Valid @RequestBody PacienteDTO dto, HttpServletRequest request) {
        
        PacienteDTO saved = pacienteService.cadastrarPaciente(dto);
        
        try {
            String detailsJson = objectMapper.writeValueAsString(saved);

            AuditEventMessage auditEvent = AuditEventMessage.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .eventType("PACIENTE_CADASTRADO")
                    .userId(saved.getId() != null ? saved.getId().toString() : "N/A")
                    .performedBy("USUARIO_LOGADO_OU_SISTEMA")
                    .ipAddress(request.getRemoteAddr())
                    .details(detailsJson)
                    .build();

            auditProducerService.sendAuditEvent(auditEvent);
            log.info("Evento de auditoria PACIENTE_CADASTRADO enviado para paciente ID: {}", saved.getId());

        } catch (JsonProcessingException e) {
            System.err.println("Erro ao serializar PacienteDTO para JSON para auditoria: " + e.getMessage());
            log.error("Erro ao serializar PacienteDTO para JSON para auditoria: {}", e.getMessage(), e);
            
        } catch (Exception e) {
            System.err.println("Erro inesperado ao enviar evento de auditoria: " + e.getMessage());
            log.error("Erro inesperado ao enviar evento de auditoria: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    /**
     * Busca paciente especifico pelo seu identificador unico.
     *
     * @param id Identificador do paciente
     * @return Dados completos do paciente ou erro 404 se nao existir
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar paciente por ID")
    public ResponseEntity<PacienteDTO> buscarPorId(@PathVariable Long id) {
        PacienteDTO paciente = pacienteService.buscarPorId(id);
        return ResponseEntity.ok(paciente);
    }
    
    /**
     * Busca paciente pela matricula de CPF.
     * CPF deve estar cadastrado para retorna-lo.
     *
     * @param cpf CPF do paciente (formato: 000.000.000-00)
     * @return Dados do paciente ou erro 404 se nao existir
     */
    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar paciente por CPF")
    public ResponseEntity<PacienteDTO> buscarPorCpf(@PathVariable String cpf) {
        PacienteDTO paciente = pacienteService.buscarPorCpf(cpf);
        return ResponseEntity.ok(paciente);
    }
    
    /**
     * Lista todos os pacientes cadastrados no sistema.
     * Ordenados por ID decrescente para exibir mais recentes primeiro.
     *
     * @return Lista completa de pacientes
     */
    @GetMapping
    @Operation(summary = "Listar todos os pacientes")
    public ResponseEntity<List<PacienteDTO>> listarTodos() {
        List<PacienteDTO> pacientes = pacienteService.listarTodos();
        return ResponseEntity.ok(pacientes);
    }

    /**
     * Lista pacientes com suporte a paginacao e ordenacao.
     * Otimizado para interfaces com grandes volumes de dados.
     *
     * @param page Numero da pagina comeando em 0
     * @param size Quantidade de registros por pagina (default 10)
     * @param sort Campo para ordenacao (default "id")
     * @param direction Sentido da ordenacao: "asc" ou "desc" (default "desc")
     * @return Pagina de pacientes com metadados de navegacao
     */
    @GetMapping("/paginado")
    @Operation(summary = "Listar pacientes paginados")
    public ResponseEntity<Page<PacienteDTO>> listarTodosPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<PacienteDTO> pacientes = pacienteService.listarTodosPaginado(pageable);
        return ResponseEntity.ok(pacientes);
    }
    
    /**
     * Busca pacientes por nome usando busca fuzzy (case-insensitive).
     * Retorna todos que contenham o termo informado em qualquer parte do nome.
     *
     * @param nome Termo de busca
     * @return Lista de pacientes que contem o nome informado
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar pacientes por nome")
    public ResponseEntity<List<PacienteDTO>> buscarPorNome(@RequestParam String nome) {
        List<PacienteDTO> pacientes = pacienteService.buscarPorNome(nome);
        return ResponseEntity.ok(pacientes);
    }

    /**
     * Busca pacientes por nome com suporte a paginacao.
     * Combina busca fuzzy com otimizacao para grandes volumes.
     *
     * @param nome Termo de busca
     * @param page Numero da pagina comeando em 0
     * @param size Quantidade de registros por pagina (default 10)
     * @param sort Campo para ordenacao (default "nomeCompleto")
     * @param direction Sentido da ordenacao: "asc" ou "desc" (default "asc")
     * @return Pagina de pacientes que contem o nome informado
     */
    @GetMapping("/buscar/paginado")
    @Operation(summary = "Buscar pacientes por nome paginado")
    public ResponseEntity<Page<PacienteDTO>> buscarPorNomePaginado(
            @RequestParam String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nomeCompleto") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<PacienteDTO> pacientes = pacienteService.buscarPorNomePaginado(nome, pageable);
        return ResponseEntity.ok(pacientes);
    }
    
    /**
     * Atualiza dados do paciente existente.
     * Atualizacao e parcial: campos nulos no DTO nao modificam valores atuais.
     * Sexo e prontuario podem ser omitidos na atualizacao.
     *
     * @param id Identificador do paciente
     * @param dto Novos dados do paciente
     * @return Dados atualizados do paciente
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do paciente")
    public ResponseEntity<PacienteDTO> atualizar(@PathVariable Long id, @Valid @RequestBody PacienteDTO dto) {
        PacienteDTO updated = pacienteService.atualizarPaciente(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Remove paciente do sistema pelo seu identificador.
     * Operacao irreversivel que remove todos os dados associados.
     *
     * @param id Identificador do paciente a ser removido
     * @return HTTP 204 sem conteudo
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar paciente")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        pacienteService.deletarPaciente(id);
        return ResponseEntity.noContent().build();
    }
}
