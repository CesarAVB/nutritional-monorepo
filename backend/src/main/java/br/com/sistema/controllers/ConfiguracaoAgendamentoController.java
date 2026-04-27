package br.com.sistema.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.dtos.ConfiguracaoAgendamentoRequestDto;
import br.com.sistema.dtos.ConfiguracaoAgendamentoResponseDto;
import br.com.sistema.services.ConfiguracaoAgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador responsavel por gerenciar as configuracoes do sistema de agendamento de consultas.
 * Permite consultar e atualizar parametros de funcionamento (horarios, dias de atendimento,
 * lembretes automaticos) e credenciais de integracao com Evolution API para envio de
 * notificacoes via WhatsApp.
 *
 * <p>O teste de conexao utiliza chamada sincrona para validar a instancia Evolution configurada
 * antes de permitir uso efetivo dos lembretes automaticos.</p>
 */
@RestController
@RequestMapping("/api/v1/configuracoes-agendamento")
@RequiredArgsConstructor
@Tag(name = "Configuracoes de Agendamento", description = "Endpoints para configuracao do sistema de agendamento de consultas")
public class ConfiguracaoAgendamentoController {

    private final ConfiguracaoAgendamentoService configuracaoAgendamentoService;

    /**
     * Recupera a configuracao atual do sistema de agendamento.
     * A API key da Evolution e retornada mascarada por seguranca.
     *
     * @return configuracao completa com parametros de funcionamento e credenciais (API key mascarada)
     */
    @GetMapping
    @Operation(summary = "Buscar configuracao de agendamento", description = "Retorna a configuracao atual com API key mascarada")
    public ResponseEntity<ConfiguracaoAgendamentoResponseDto> buscar() {
        return ResponseEntity.ok(configuracaoAgendamentoService.buscar());
    }

    /**
     * Atualiza a configuracao do sistema de agendamento incluindo horarios de atendimento,
     * dias da semana, duracao padrao de consulta, parametros de lembretes automaticos e
     * credenciais de integracao com Evolution API.
     *
     * @param request novos valores de configuracao
     * @return configuracao atualizada e persistida
     */
    @PutMapping
    @Operation(summary = "Salvar configuracao de agendamento", description = "Atualiza parametros de funcionamento, horarios, lembretes e credenciais Evolution")
    public ResponseEntity<ConfiguracaoAgendamentoResponseDto> salvar(@Valid @RequestBody ConfiguracaoAgendamentoRequestDto request) {
        return ResponseEntity.ok(configuracaoAgendamentoService.salvar(request));
    }

    /**
     * Executa uma chamada minima para validar a conexao com a instancia Evolution API configurada.
     *
     * @return resultado do teste contendo status de conexao e possiveis mensagens de erro
     */
    @PostMapping("/testar-whatsapp")
    @Operation(summary = "Testar conexao com WhatsApp", description = "Faz uma chamada minima para validar a conexao com a instancia Evolution API configurada")
    public ResponseEntity<Map<String, Object>> testarConexaoWhatsapp() {
        return ResponseEntity.ok(configuracaoAgendamentoService.testarConexaoWhatsapp());
    }
}
