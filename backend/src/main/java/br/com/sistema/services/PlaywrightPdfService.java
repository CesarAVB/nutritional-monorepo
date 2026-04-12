package br.com.sistema.services;

import java.nio.file.Paths;
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
 * Serviço de geração de PDF usando Playwright (Chromium headless).
 * Usado exclusivamente para o relatório comparativo evolutivo, que exige
 * CSS moderno (flexbox/grid) e JavaScript (Chart.js) para os gráficos.
 *
 * Os demais relatórios continuam usando OpenHTMLtoPDF via RelatorioService.
 *
 * Em Docker/Linux: defina a variável de ambiente CHROMIUM_PATH=/usr/bin/chromium
 * para usar o Chromium do sistema (evita o download automático de ~300MB).
 * Em Windows local: o browser é baixado automaticamente pelo Playwright na
 * primeira execução e cacheado em %USERPROFILE%\AppData\Local\ms-playwright\.
 */
@Service
public class PlaywrightPdfService {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightPdfService.class);
        private static final String DEFAULT_RUNNING_FOOTER =
            "Andre Reis | Nutricao Clinica e Esportiva | Av. Dr. Mario Guimaraes, 318, Sala 1001 | Nova Iguacu, RJ | CEP 26255-230";

    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        try {
            log.info("Inicializando Playwright Chromium...");
            playwright = Playwright.create();

            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(List.of(
                            "--no-sandbox",
                            "--disable-setuid-sandbox",
                            "--disable-gpu",
                            "--disable-dev-shm-usage"
                    ));

            // Em Docker/Linux: CHROMIUM_PATH=/usr/bin/chromium
            // Em Windows local: Playwright baixa automaticamente
            String chromiumPath = System.getenv("CHROMIUM_PATH");
            if (chromiumPath != null && !chromiumPath.isBlank()) {
                options.setExecutablePath(Paths.get(chromiumPath));
                log.info("Playwright usando Chromium do sistema: {}", chromiumPath);
            }

            browser = playwright.chromium().launch(options);
            log.info("Playwright Chromium pronto.");
        } catch (Exception e) {
            log.error("Falha ao inicializar Playwright: {}", e.getMessage(), e);
            throw new RuntimeException("Não foi possível inicializar o Playwright/Chromium", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (browser != null)     { try { browser.close();     } catch (Exception ignored) {} }
        if (playwright != null)  { try { playwright.close();  } catch (Exception ignored) {} }
        log.info("Playwright encerrado.");
    }

    /**
     * Gera um PDF a partir do HTML fornecido usando Chromium headless.
     * O HTML pode conter CSS moderno, JavaScript e bibliotecas como Chart.js.
     *
     * @param html conteúdo HTML completo da página
     * @return bytes do PDF gerado em formato A4
     */
    public byte[] generatePdf(String html) {
        BrowserContext ctx = browser.newContext();
        try {
            Page page = ctx.newPage();

            // Carrega o HTML; NETWORKIDLE aguarda Chart.js CDN ser baixado
            page.setContent(html, new Page.SetContentOptions()
                    .setWaitUntil(WaitUntilState.NETWORKIDLE));

            // Aguarda o sinal JS de que todos os gráficos terminaram de renderizar.
            // O template seta window.__chartsReady = true após todos os charts.
            // Timeout de 12s; se expirar (ex: CDN offline), gera o PDF mesmo assim.
            try {
                page.waitForFunction("() => window.__chartsReady === true",
                        new Page.WaitForFunctionOptions().setTimeout(12_000));
            } catch (Exception e) {
                log.warn("Timeout aguardando charts — PDF será gerado sem alguns gráficos: {}", e.getMessage());
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
}
