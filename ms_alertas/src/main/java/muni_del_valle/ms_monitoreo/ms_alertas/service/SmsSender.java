package muni_del_valle.ms_monitoreo.ms_alertas.service;

import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SmsSender {
    private final Logger log = LoggerFactory.getLogger(SmsSender.class);

    public SmsSender() {
        // No external provider required; SMS are simulated locally.
    }

    /**
     * Simula el envío de SMS localmente: no lanza excepciones ni depende de proveedores.
     * Mantiene la firma `sendAlert(CreateAlertRequest)` para compatibilidad.
     */
    public void sendAlert(CreateAlertRequest req) {
        String destination = (req != null && req.getSeverity() != null) ? req.getSeverity().toString() : "unknown";
        String desc = (req != null && req.getDescription() != null) ? req.getDescription() : "(sin descripción)";
        // Formato de log simple y legible para simular SMS
        log.info("SMS SIMULADO\nDestino: {}\nMensaje: {}", destination, desc);
    }
}
