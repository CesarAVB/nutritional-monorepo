package br.com.sistema.configurations;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Gestão Nutricional - API")
                        .version("1.0.0")
                        .description("Microserviço REST para gestão de consultas nutricionais, \" +\r\n"
                        		+ " \"controle de pacientes, avaliações físicas e acompanhamento evolutivo. \" +\r\n"
                        		+ " \"Desenvolvido com Spring Boot 3, integrado com Flyway, Log4j2 e boas práticas de arquitetura de microserviços.")
                        .contact(new Contact()
                                .name("César Augusto")
                                .email("cesar.augusto.rj1@gmail.com")
                                .url("https://portfolio.cesaravb.com.br/"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api-sysnutritional.cesaravb.com.br/")
                                .description("Servidor de Produção")
                ));
    }
}
