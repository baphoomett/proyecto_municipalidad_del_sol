package muni_del_valle.ms_monitoreo.ms_alertas.controller;

import jakarta.validation.Valid;
import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import muni_del_valle.ms_monitoreo.ms_alertas.model.Alert;
import muni_del_valle.ms_monitoreo.ms_alertas.repository.AlertRepository;
import muni_del_valle.ms_monitoreo.ms_alertas.service.AlertService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AmqpTemplate amqpTemplate;
    private final AlertRepository alertRepository;

    public AlertController(AmqpTemplate amqpTemplate, AlertRepository alertRepository) {
        this.amqpTemplate = amqpTemplate;
        this.alertRepository = alertRepository;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> receiveWebhook(@Valid @RequestBody CreateAlertRequest req) {
        Alert alert = new Alert();
        alert.setReportId(req.getReportId());
        alert.setSeverity(req.getSeverity());
        alert.setDescription(req.getDescription());
        alert.setIncidentType(req.getIncidentType());
        alertRepository.save(alert);
        amqpTemplate.convertAndSend("alerts.exchange", "alerts.new", req);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public ResponseEntity<List<Alert>> getAlerts() {
        return ResponseEntity.ok(alertRepository.findAll());
    }

    @DeleteMapping("/by-report/{reportId}")
    public ResponseEntity<?> deleteByReportId(@PathVariable Long reportId) {
        alertRepository.deleteByReportId(reportId);
        return ResponseEntity.noContent().build();
    }
}
