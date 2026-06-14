package muni_del_valle.ms_monitoreo.ms_alertas.service;

import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailSender {
    private final Logger log = LoggerFactory.getLogger(EmailSender.class);
    private final JavaMailSender mailSender;

    @Value("${alert.email.to:community@example.org}")
    private String alertEmailTo;

    public EmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAlert(CreateAlertRequest req) {
    try {
        String locationText = "No disponible";
        String mapsLink = "";
        if (req.getGeometry() != null && req.getGeometry().startsWith("POINT(")) {
            String coords = req.getGeometry().replace("POINT(", "").replace(")", "");
            String[] parts = coords.split(" ");
            if (parts.length == 2) {
                String lon = parts[0];
                String lat = parts[1];
                locationText = "Latitud: " + lat + ", Longitud: " + lon;
                mapsLink = "https://www.google.com/maps?q=" + lat + "," + lon;
            }
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(alertEmailTo.split(","));
        msg.setSubject("🔥 Alerta: nuevo foco detectado - Municipalidad Valle del Sol");
        msg.setText(
            "Se ha detectado un nuevo foco de incendio.\n\n" +
            "Reporte ID: " + req.getReportId() + "\n" +
            "Descripción: " + req.getDescription() + "\n" +
            "Severidad: " + req.getSeverity() + "\n" +
            "Ubicación: " + locationText + "\n" +
            (mapsLink.isEmpty() ? "" : "Ver en mapa: " + mapsLink + "\n") +
            "\nPor favor tome las medidas necesarias.\n" +
            "Municipalidad Valle del Sol - Sistema de Emergencias"
        );
        mailSender.send(msg);
        log.info("[EmailSender] Email sent for report {}", req.getReportId());
    } catch (Exception ex) {
        log.error("[EmailSender] Email send failed: {}", ex.getMessage());
        throw new RuntimeException(ex);
        }
    }
}
