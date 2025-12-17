package com.noticias.web.servicios;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServicio {

    private final JavaMailSender mailSender;

    public EmailServicio(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmail(String destino, String asunto, String cuerpoHtml) {
        if (destino == null || asunto == null || cuerpoHtml == null) {
            System.err.println("EmailService skipped: null parameters");
            return;
        }
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(destino);
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true); // true = es HTML

            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar email", e);
        }
    }
}
