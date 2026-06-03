package muni_del_valle.ms_monitoreo.ms_alertas.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AlertService {
    private final Logger log = LoggerFactory.getLogger(AlertService.class);
    private final EmailSender emailSender;
    private final SmsSender smsSender;

    public AlertService(EmailSender emailSender, SmsSender smsSender) {
        this.emailSender = emailSender;
        this.smsSender = smsSender;
    }

    @Retry(name = "alertsRetry")
    @CircuitBreaker(name = "alertsCircuit")
    public void handleNewFocus(CreateAlertRequest req) {
        log.info("Received new focus for alerts: reportId={} severity={}", req.getReportId(), req.getSeverity());
        // send notifications in parallel; let exceptions bubble to caller/listener so they can be persisted
        emailSender.sendAlert(req);
        smsSender.sendAlert(req);
    }
}
