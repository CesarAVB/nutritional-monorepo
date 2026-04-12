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

@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pacientes", description = "Endpoints para gestão de pacientes")
public class PacienteController {
    
    private final PacienteService pacienteService;
    private final AuditProducerService auditProducerService;
    private final ObjectMapper objectMapper;
    
    // ==============================================
    // # Método - cadastrar
    // # Cadastra novo paciente e envia evento de auditoria
    // ==============================================
    @PostMapping
    @Operation(summary = "Cadastrar novo paciente", description = "Cria um novo paciente no sistema")
    public ResponseEntity<PacienteDTO> cadastrar(@Valid @RequestBody PacienteDTO dto, HttpServletRequest request) {
        
        PacienteDTO saved = pacienteService.cadastrarPaciente(dto);
        
        try {
            String detailsJson = objectMapper.writeValueAsString(saved);					// Converte o DTO salvo para uma string JSON para os detalhes do evento

            AuditEventMessage auditEvent = AuditEventMessage.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .eventType("PACIENTE_CADASTRADO") 										// Tipo de evento claro
                    .userId(saved.getId() != null ? saved.getId().toString() : "N/A") 		// ID do paciente cadastrado
                    .performedBy("USUARIO_LOGADO_OU_SISTEMA") 								// Quem realizou a ação (ex: ID do usuário logado, "sistema", "admin")
                    .ipAddress(request.getRemoteAddr()) 									// IP do cliente que fez a requisição
                    .details(detailsJson) 													// Detalhes completos do paciente cadastrado
                    .build();

            auditProducerService.sendAuditEvent(auditEvent);
            log.info("Evento de auditoria 'PACIENTE_CADASTRADO' enviado para o paciente ID: {}", saved.getId());

        } catch (JsonProcessingException e) {
            // Loga o erro, mas não impede o fluxo principal da aplicação
            // É importante que a falha na auditoria não cause falha na operação de negócio
            System.err.println("Erro ao serializar PacienteDTO para JSON para auditoria: " + e.getMessage());
            log.error("Erro ao serializar PacienteDTO para JSON para auditoria: {}", e.getMessage(), e);
            
        } catch (Exception e) {
            System.err.println("Erro inesperado ao enviar evento de auditoria: " + e.getMessage());
            log.error("Erro inesperado ao enviar evento de auditoria: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    // ==============================================
    // # Método - buscarPorId
    // # Busca paciente por ID
    // ==============================================
    @GetMapping("/{id}")
    @Operation(summary = "Buscar paciente por ID")
    public ResponseEntity<PacienteDTO> buscarPorId(@PathVariable Long id) {
        PacienteDTO paciente = pacienteService.buscarPorId(id);
        return ResponseEntity.ok(paciente);
    }
    
    // ==============================================
    // # Método - buscarPorCpf
    // # Busca paciente por CPF
    // ==============================================
    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar paciente por CPF")
    public ResponseEntity<PacienteDTO> buscarPorCpf(@PathVariable String cpf) {
        PacienteDTO paciente = pacienteService.buscarPorCpf(cpf);
        return ResponseEntity.ok(paciente);
    }
    
    // ==============================================
    // # Método - listarTodos
    // # Lista todos os pacientes
    // ==============================================
    @GetMapping
    @Operation(summary = "Listar todos os pacientes")
    public ResponseEntity<List<PacienteDTO>> listarTodos() {
        List<PacienteDTO> pacientes = pacienteService.listarTodos();
        return ResponseEntity.ok(pacientes);
    }

    // ==============================================
    // # Metodo - listarTodosPaginado
    // # Lista pacientes paginados
    // ==============================================
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
    
    // ==============================================
    // # Método - buscarPorNome
    // # Busca pacientes por nome
    // ==============================================
    @GetMapping("/buscar")
    @Operation(summary = "Buscar pacientes por nome")
    public ResponseEntity<List<PacienteDTO>> buscarPorNome(@RequestParam String nome) {
        List<PacienteDTO> pacientes = pacienteService.buscarPorNome(nome);
        return ResponseEntity.ok(pacientes);
    }

    // ==============================================
    // # Metodo - buscarPorNomePaginado
    // # Busca pacientes por nome com paginacao
    // ==============================================
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
    
    // ==============================================
    // # Método - atualizar
    // # Atualiza dados do paciente
    // ==============================================
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do paciente")
    public ResponseEntity<PacienteDTO> atualizar(@PathVariable Long id, @Valid @RequestBody PacienteDTO dto) {
        PacienteDTO updated = pacienteService.atualizarPaciente(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    // ==============================================
    // # Método - deletar
    // # Deleta um paciente por ID
    // ==============================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar paciente")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        pacienteService.deletarPaciente(id);
        return ResponseEntity.noContent().build();
    }
}