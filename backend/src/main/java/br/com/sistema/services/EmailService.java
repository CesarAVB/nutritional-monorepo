package br.com.sistema.services;

import br.com.sistema.models.ConfiguracaoInfraestrutura;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import java.util.Properties;

/**
 * Serviço para envio de emails simples via SMTP.
 * Configuração dinâmica via banco de dados.
 */
@Service
@Slf4j
public class EmailService {

    private JavaMailSenderImpl mailSender;
    private String remetenteEmail = "";

    /**
     * Reconfigura o serviço de email com base nas configurações de infraestrutura.
     * Se desabilitado, limpa a configuração existente.
     *
     * @param config configuração de infraestrutura do banco
     */
    public void reconfigurar(ConfiguracaoInfraestrutura config) {
        if (!Boolean.TRUE.equals(config.getEmailHabilitado())) {
            this.mailSender = null;
            this.remetenteEmail = "";
            log.info("EmailService: email desabilitado.");
            return;
        }
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getEmailHost());
        sender.setPort(config.getEmailPort() != null ? config.getEmailPort() : 587);
        sender.setUsername(config.getEmailUsername());
        sender.setPassword(config.getEmailPassword());
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        this.mailSender = sender;
        this.remetenteEmail = config.getEmailUsername() != null ? config.getEmailUsername() : "";
        log.info("EmailService reconfigurado - host: {}", config.getEmailHost());
    }

    /**
     * Envia email simples com remetente configurado no SMTP.
     * Falha silenciosa com log - não propaga exceção.
     *
     * @param destinatario endereço de email do destinatário
     * @param assunto assunto do email
     * @param corpo corpo do email em texto plano
     * @return true se enviado com sucesso, false em caso de falha
     */
    public boolean enviarEmail(String destinatario, String assunto, String corpo) {
        if (mailSender == null) {
            log.warn("EmailService: tentativa de envio sem configuração ativa.");
            return false;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(remetenteEmail);
            message.setTo(destinatario);
            message.setSubject(assunto);
            message.setText(corpo);
            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            log.warn("Falha ao enviar email para {}: {}", destinatario, ex.getMessage());
            return false;
        }
    }
}
