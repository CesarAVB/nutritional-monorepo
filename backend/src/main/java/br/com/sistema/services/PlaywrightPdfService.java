package br.com.sistema.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Margin;
import com.microsoft.playwright.options.WaitUntilState;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Gera PDFs usando Playwright (Chromium headless) para reportes que exigem
 * CSS moderno (flexbox/grid) e JavaScript (Chart.js para graficos).
 * Utilizado exclusivamente pelo relatorio comparativo evolutivo.
 *
 * Os demais relatorios continuam usando OpenHTMLtoPDF via RelatorioService.
 * Em Docker/Linux: defina CHROMIUM_PATH=/usr/bin/chromium para evitar
 * download automatico de ~300MB. Em Windows: o browser eh baixado na
 * primeira execucao e cacheado em %USERPROFILE%\AppData\Local\ms-playwright\.
 *
 * Se o Chromium nao estiver disponivel, o servico marca isAvailable()=false
 * e permite fallback graceful para o chamador.
 */
@Service
public class PlaywrightPdfService {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightPdfService.class);
    private static final String DEFAULT_RUNNING_FOOTER =
            "Andre Reis | Nutricao Clinica e Esportiva | Av. Dr. Mario Guimaraes, 318, Sala 1001 | Nova Iguacu, RJ | CEP 26255-230";

    private Playwright playwright;
    private Browser browser;
    private volatile boolean available;

    /**
     * Inicializa Playwright e Chromium em modo headless.
     * Tenta usar Chromium do sistema (via CHROMIUM_PATH ou caminhos comuns)
     * antes do binario gerenciado pelo Playwright. Trata gracefully falhas
     * de executavel indisponivel ou wrapper snap nao funcional.
     */
    @PostConstruct
    public void init() {
        try {
            log.info("Inicializando Playwright Chromium...");

            playwright = Playwright.create();

            BrowserType.LaunchOptions baseOptions = buildLaunchOptions();

            String chromiumPath = System.getenv("CHROMIUM_PATH");
            Path chromiumExecutable = resolveChromiumExecutable(chromiumPath);

            BrowserType.LaunchOptions options = buildLaunchOptions();
            if (chromiumExecutable != null) {
                options.setExecutablePath(chromiumExecutable);
                log.info("Playwright usando Chromium do sistema: {}", chromiumExecutable);
            } else if (chromiumPath != null && !chromiumPath.isBlank()) {
                log.warn("CHROMIUM_PATH configurado, mas arquivo nao encontrado em {}. Tentando launcher padrao do Playwright.", chromiumPath);
            }

            try {
                browser = playwright.chromium().launch(options);
            } catch (Exception firstLaunchError) {
                if (chromiumExecutable != null && isSnapChromiumFailure(firstLaunchError)) {
                    log.warn("Chromium do sistema parece ser wrapper snap e nao funciona neste ambiente. Tentando Chromium gerenciado pelo Playwright.");
                    browser = playwright.chromium().launch(baseOptions);
                } else {
                    throw firstLaunchError;
                }
            }

            available = true;
            log.info("Playwright Chromium pronto.");
        } catch (Exception e) {
            available = false;
            if (isMissingExecutableFailure(e) || isSnapChromiumFailure(e)) {
                log.warn("Playwright indisponivel por falta de executavel Chromium: {}", e.getMessage());
            } else {
                log.error("Falha ao inicializar Playwright: {}", e.getMessage(), e);
            }
            closeResources();
            log.warn("Playwright desabilitado; o sistema seguira com fallback de PDF.");
        }
    }

    /**
     * Libera recursos do browser e playwright.
     */
    @PreDestroy
    public void destroy() {
        closeResources();
        available = false;
        log.info("Playwright encerrado.");
    }

    /**
     * Indica se o servico esta operacional e pode gerar PDFs.
     *
     * @return true se Chromium foi inicializado com sucesso
     */
    public boolean isAvailable() {
        return available && browser != null;
    }

    /**
     * Gera PDF a partir de HTML completo usando Chromium headless.
     * Aguarda sinal JavaScript de que Chart.js terminou de renderizar
     * antes de converter (timeout de 12s; gera mesmo assim se CDN offline).
     *
     * @param html conteudo HTML completo da pagina
     * @return bytes do PDF gerado em formato A4
     */
    public byte[] generatePdf(String html) {
        if (!isAvailable()) {
            throw new IllegalStateException("Playwright indisponivel para geracao de PDF");
        }
        BrowserContext ctx = browser.newContext();
        try {
            Page page = ctx.newPage();

            // Aguarda Chart.js CDN ser baixado
            page.setContent(html, new Page.SetContentOptions()
                    .setWaitUntil(WaitUntilState.NETWORKIDLE));

            // Sinal de que todos os charts terminou de renderizar
            try {
                page.waitForFunction("() => window.__chartsReady === true",
                        new Page.WaitForFunctionOptions().setTimeout(12_000));
            } catch (Exception e) {
                log.warn("Timeout aguardando charts - PDF sera gerado sem alguns graficos: {}", e.getMessage());
            }

            return page.pdf(new Page.PdfOptions()
                    .setFormat("A4")
                    .setPreferCSSPageSize(true)
                    .setDisplayHeaderFooter(true)
                    .setHeaderTemplate("<div></div>")
                    .setFooterTemplate("<div style='width:100%; font-size:10px; color:#0b6b57; text-align:center; padding:0 10mm; font-family:Segoe UI, Arial, sans-serif; font-weight:600;'>"
                        + DEFAULT_RUNNING_FOOTER
                        + "</div>")
                    .setPrintBackground(true)
                    .setMargin(new Margin()
                            .setTop("0mm")
                            .setRight("0mm")
                            .setBottom("0mm")
                            .setLeft("0mm")));

        } finally {
            ctx.close();
        }
    }

    /**
     * Resolve caminho do executavel Chromium considerando variavel
     * de ambiente e caminhos comuns no Linux.
     *
     * @param configuredPath valor de CHROMIUM_PATH
     * @return Path do executavel valido ou null
     */
    private Path resolveChromiumExecutable(String configuredPath) {
        if (configuredPath != null && !configuredPath.isBlank()) {
            Path configured = Paths.get(configuredPath);
            if (Files.isExecutable(configured) && isUsableChromiumExecutable(configured)) {
                return configured;
            }
        }

        List<String> candidates = List.of(
                "/usr/bin/chromium",
                "/usr/bin/chromium-browser",
                "/snap/bin/chromium"
        );

        for (String candidate : candidates) {
            Path path = Paths.get(candidate);
            if (Files.isExecutable(path) && isUsableChromiumExecutable(path)) {
                return path;
            }
        }
        return null;
    }

    /**
     * Constroi opcoes de launch para Chromium headless com args
     * de seguranca para ambientes containerizados.
     *
     * @return configuracao de launch
     */
    private BrowserType.LaunchOptions buildLaunchOptions() {
        return new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of(
                        "--no-sandbox",
                        "--disable-setuid-sandbox",
                        "--disable-gpu",
                        "--disable-dev-shm-usage"
                ));
    }

    /**
     * Verifica se o executavel Chromium funciona realmente (executa
     * --version e checa exit code e saida).
     *
     * @param executable path do executavel
     * @return true se executavel responde corretamente
     */
    private boolean isUsableChromiumExecutable(Path executable) {
        try {
            Process process = new ProcessBuilder(executable.toString(), "--version")
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(Duration.ofSeconds(3).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            String output = new String(process.getInputStream().readAllBytes());
            return process.exitValue() == 0 && !output.toLowerCase().contains("requires the chromium snap");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Detecta erro por falta de arquivo executavel.
     *
     * @param e excecao
     * @return true se for erro de executavel ausente
     */
    private boolean isMissingExecutableFailure(Exception e) {
        return e.getMessage() != null && e.getMessage().contains("executable doesn't exist");
    }

    /**
     * Detecta erro de Chromium snap wrapper nao funcional.
     *
     * @param e excecao
     * @return true se for erro de snap
     */
    private boolean isSnapChromiumFailure(Exception e) {
        return e.getMessage() != null && e.getMessage().toLowerCase().contains("requires the chromium snap");
    }

    /**
     * Fecha browser e playwright com tratamento de excecoes
     * para garantir cleanup mesmo em falhas parciais.
     */
    private void closeResources() {
        if (browser != null) {
            try {
                browser.close();
            } catch (Exception ignored) {
            } finally {
                browser = null;
            }
        }
        if (playwright != null) {
            try {
                playwright.close();
            } catch (Exception ignored) {
            } finally {
                playwright = null;
            }
        }
    }
}
