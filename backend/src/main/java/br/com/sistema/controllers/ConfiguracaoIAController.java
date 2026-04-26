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

@RestController
@RequestMapping("/api/v1/configuracoes-ia")
@RequiredArgsConstructor
@Tag(name = "Configurações de IA", description = "Endpoints para configuração do assistente de IA")
public class ConfiguracaoIAController {

    private final ConfiguracaoIAService configuracaoIAService;

    @GetMapping
    @Operation(summary = "Buscar configuração de IA", description = "Retorna a configuração atual com API key mascarada")
    public ResponseEntity<ConfiguracaoIAResponse> buscar() {
        return ResponseEntity.ok(configuracaoIAService.buscar());
    }

    @PutMapping
    @Operation(summary = "Salvar configuração de IA", description = "Atualiza provedor, modelo, API key, prompt do sistema e temperatura")
    public ResponseEntity<ConfiguracaoIAResponse> salvar(@Valid @RequestBody ConfiguracaoIARequest request) {
        return ResponseEntity.ok(configuracaoIAService.salvar(request));
    }

    @PostMapping("/testar")
    @Operation(summary = "Testar conexão com a IA", description = "Faz uma chamada mínima para validar a API key e o modelo configurados")
    public ResponseEntity<Map<String, Object>> testarConexao() {
        return ResponseEntity.ok(configuracaoIAService.testarConexao());
    }
}
