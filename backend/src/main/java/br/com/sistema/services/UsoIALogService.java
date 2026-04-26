package br.com.sistema.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistema.dtos.UsoIALogResponse;
import br.com.sistema.dtos.UsoIAResumoResponse;
import br.com.sistema.dtos.ia.OpenAIResponse;
import br.com.sistema.models.ConfiguracaoIA;
import br.com.sistema.models.UsoIALog;
import br.com.sistema.repositories.UsoIALogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsoIALogService {

    private static final BigDecimal MILHAO = BigDecimal.valueOf(1_000_000);

    private final UsoIALogRepository repository;

    /**
     * Registra o uso de uma chamada à API de IA com tokens e custo estimado.
     * Executado em transação independente para não comprometer rollback da dieta.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(ConfiguracaoIA config, OpenAIResponse response, Long pacienteId, boolean sucesso) {
        int entrada = 0;
        int saida = 0;

        if (sucesso && response != null && response.getUsage() != null) {
            entrada = response.getUsage().getPromptTokens();
            saida = response.getUsage().getCompletionTokens();
        }

        BigDecimal custo = calcularCusto(config.getModelo(), entrada, saida);

        UsoIALog log = new UsoIALog();
        log.setDataHora(LocalDateTime.now());
        log.setProvedor(config.getProvedor() != null ? config.getProvedor().name() : "OPENAI");
        log.setModelo(config.getModelo());
        log.setTokensEntrada(entrada);
        log.setTokensSaida(saida);
        log.setCustoUsd(custo);
        log.setSucesso(sucesso);
        log.setPacienteId(pacienteId);

        repository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<UsoIALogResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UsoIAResumoResponse resumo() {
        long totalChamadas = repository.count();
        long totalTokens = repository.sumTokensTotal();
        BigDecimal custoTotal = repository.sumCustoTotal();
        BigDecimal custo30Dias = repository.sumCustoDesde(LocalDateTime.now().minusDays(30));

        return new UsoIAResumoResponse(totalChamadas, totalTokens, custoTotal, custo30Dias);
    }

    /**
     * Estima custo em USD com base nos preços públicos dos provedores.
     * Preços por 1M de tokens (entrada / saída):
     *   gpt-4o           : $2.50 / $10.00
     *   gpt-4o-mini      : $0.15 / $0.60
     *   gpt-3.5-turbo    : $0.50 / $1.50
     *   anthropic/claude-*: $3.00 / $15.00 (estimativa conservadora)
     *   google/gemini-*  : $0.35 / $1.05
     *   outros           : $1.00 / $3.00 (estimativa genérica)
     */
    private BigDecimal calcularCusto(String modelo, int tokensEntrada, int tokensSaida) {
        if (modelo == null) return BigDecimal.ZERO;

        String m = modelo.toLowerCase();

        BigDecimal precoEntrada;
        BigDecimal precoSaida;

        if (m.contains("gpt-4o-mini")) {
            precoEntrada = new BigDecimal("0.15");
            precoSaida = new BigDecimal("0.60");
        } else if (m.contains("gpt-4o")) {
            precoEntrada = new BigDecimal("2.50");
            precoSaida = new BigDecimal("10.00");
        } else if (m.contains("gpt-3.5-turbo")) {
            precoEntrada = new BigDecimal("0.50");
            precoSaida = new BigDecimal("1.50");
        } else if (m.contains("anthropic/") || m.contains("claude-")) {
            precoEntrada = new BigDecimal("3.00");
            precoSaida = new BigDecimal("15.00");
        } else if (m.contains("google/") || m.contains("gemini")) {
            precoEntrada = new BigDecimal("0.35");
            precoSaida = new BigDecimal("1.05");
        } else {
            precoEntrada = new BigDecimal("1.00");
            precoSaida = new BigDecimal("3.00");
        }

        BigDecimal custoEntrada = precoEntrada.multiply(BigDecimal.valueOf(tokensEntrada)).divide(MILHAO, 6, RoundingMode.HALF_UP);
        BigDecimal custoSaida = precoSaida.multiply(BigDecimal.valueOf(tokensSaida)).divide(MILHAO, 6, RoundingMode.HALF_UP);

        return custoEntrada.add(custoSaida);
    }

    private UsoIALogResponse toResponse(UsoIALog log) {
        return new UsoIALogResponse(
                log.getId(),
                log.getDataHora(),
                log.getProvedor(),
                log.getModelo(),
                log.getTokensEntrada(),
                log.getTokensSaida(),
                log.getCustoUsd(),
                log.isSucesso(),
                log.getPacienteId()
        );
    }
}
