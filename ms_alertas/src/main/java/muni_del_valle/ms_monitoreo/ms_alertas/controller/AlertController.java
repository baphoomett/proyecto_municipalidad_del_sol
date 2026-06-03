package muni_del_valle.ms_monitoreo.ms_alertas.controller;

import jakarta.validation.Valid;
import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import muni_del_valle.ms_monitoreo.ms_alertas.service.AlertService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AmqpTemplate amqpTemplate;

    public AlertController(AmqpTemplate amqpTemplate) { this.amqpTemplate = amqpTemplate; }

    @PostMapping("/webhook")
    public ResponseEntity<?> receiveWebhook(@Valid @RequestBody CreateAlertRequest req) {
        // publish to RabbitMQ and return 202 immediately
        amqpTemplate.convertAndSend("alerts.exchange", "alerts.new", req);
        return ResponseEntity.accepted().build();
    }
}
