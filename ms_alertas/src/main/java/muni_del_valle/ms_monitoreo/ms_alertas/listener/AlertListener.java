package muni_del_valle.ms_monitoreo.ms_alertas.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni_del_valle.ms_monitoreo.ms_alertas.config.RabbitConfig;
import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import muni_del_valle.ms_monitoreo.ms_alertas.model.FailedAlert;
import muni_del_valle.ms_monitoreo.ms_alertas.repository.FailedAlertRepository;
import muni_del_valle.ms_monitoreo.ms_alertas.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class AlertListener {
    private final Logger log = LoggerFactory.getLogger(AlertListener.class);
    private final AlertService alertService;
    private final FailedAlertRepository failedAlertRepository;
    private final ObjectMapper objectMapper;

    public AlertListener(AlertService alertService, FailedAlertRepository failedAlertRepository, ObjectMapper objectMapper) {
        this.alertService = alertService;
        this.failedAlertRepository = failedAlertRepository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
public void onMessage(@Payload CreateAlertRequest req) {
    try {
        if (req == null) {
            log.warn("Received null payload in AlertListener");
            return;
        }
        alertService.handleNewFocus(req);
    } catch (Exception ex) {
        log.error("Alert processing failed, saving to failed_alert table: {}", ex.getMessage());
        try {
            FailedAlert fa = new FailedAlert();
            fa.setReportId(req != null ? req.getReportId() : null);
            try { fa.setPayload(objectMapper.writeValueAsString(req)); } catch (Exception e) { fa.setPayload(String.valueOf(req)); }
            fa.setError(ex.getMessage());
            failedAlertRepository.save(fa);
        } catch (Exception saveEx) {
            log.error("Failed to persist failed alert: {}", saveEx.getMessage());
            }
        }
    }
}
