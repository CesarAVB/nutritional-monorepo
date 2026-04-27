package br.com.sistema.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para envio de emails simples via SMTP.
 * Utiliza configuração de SMTP definida no application.properties.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String remetenteEmail;

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
