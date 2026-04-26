package br.com.sistema.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistema.dtos.ConfiguracaoIARequest;
import br.com.sistema.dtos.ConfiguracaoIAResponse;
import br.com.sistema.services.ConfiguracaoIAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador responsavel por gerenciar as configuracoes do assistente de IA.
 * Permite consultar e atualizar o provedor, modelo, credenciais e parametros
 * que controlam o comportamento do servico de geracao de dietas inteligentes.
 *
 * <p>O teste de conexao utiliza chamada sincrona para validar a API key e modelo
 * configurados antes de permitir uso efetivo da funcionalidade de IA.</p>
 */
@RestController
@RequestMapping("/api/v1/configuracoes-ia")
@RequiredArgsConstructor
@Tag(name = "Configuracoes de IA", description = "Endpoints para configuracao do assistente de IA")
public class ConfiguracaoIAController {

    private final ConfiguracaoIAService configuracaoIAService;

    /**
     * Recupera a configuracao atual do sistema de IA.
     * A API key e retornada mascarada por seguranca.
     *
     * @return configuracao completa com provedor, modelo e demais parametros (API key mascarada)
     */
    @GetMapping
    @Operation(summary = "Buscar configuracao de IA", description = "Retorna a configuracao atual com API key mascarada")
    public ResponseEntity<ConfiguracaoIAResponse> buscar() {
        return ResponseEntity.ok(configuracaoIAService.buscar());
    }

    /**
     * Atualiza a configuracao do sistema de IA incluindo provedor, modelo,
     * API key, prompt do sistema e temperatura de criatividade.
     *
     * @param request novos valores de configuracao
     * @return configuracao atualizada e persistida
     */
    @PutMapping
    @Operation(summary = "Salvar configuracao de IA", description = "Atualiza provedor, modelo, API key, prompt do sistema e temperatura")
    public ResponseEntity<ConfiguracaoIAResponse> salvar(@Valid @RequestBody ConfiguracaoIARequest request) {
        return ResponseEntity.ok(configuracaoIAService.salvar(request));
    }

    /**
     * Executa uma chamada minima para validar a API key e modelo configurados.
     *
     * @return resultado do teste contendo status de conexao e possiveis mensagens de erro
     */
    @PostMapping("/testar")
    @Operation(summary = "Testar conexao com a IA", description = "Faz uma chamada minima para validar a API key e o modelo configurados")
    public ResponseEntity<Map<String, Object>> testarConexao() {
        return ResponseEntity.ok(configuracaoIAService.testarConexao());
    }
}
