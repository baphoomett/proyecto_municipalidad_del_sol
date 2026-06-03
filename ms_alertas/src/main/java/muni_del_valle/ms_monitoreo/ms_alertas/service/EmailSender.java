package muni_del_valle.ms_monitoreo.ms_alertas.service;

import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailSender {
    private final Logger log = LoggerFactory.getLogger(EmailSender.class);
    private final JavaMailSender mailSender;

    public EmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAlert(CreateAlertRequest req) {
        // Basic SMTP send using configured JavaMailSender (SendGrid via SMTP)
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo("community@example.org");
            msg.setSubject("Alerta: nuevo foco detectado");
            msg.setText("Reporte: " + req.getReportId() + "\nDescripción: " + req.getDescription());
            mailSender.send(msg);
            log.info("[EmailSender] Email sent for report {}", req.getReportId());
        } catch (Exception ex) {
            log.error("[EmailSender] Email send failed: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
