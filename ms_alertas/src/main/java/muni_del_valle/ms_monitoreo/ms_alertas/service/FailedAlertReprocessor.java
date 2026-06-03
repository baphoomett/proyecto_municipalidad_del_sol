package muni_del_valle.ms_monitoreo.ms_alertas.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni_del_valle.ms_monitoreo.ms_alertas.model.FailedAlert;
import muni_del_valle.ms_monitoreo.ms_alertas.repository.FailedAlertRepository;
import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class FailedAlertReprocessor {
    private final Logger log = LoggerFactory.getLogger(FailedAlertReprocessor.class);
    private final FailedAlertRepository repo;
    private final AlertService alertService;
    private final ObjectMapper objectMapper;

    @Value("${alerts.reprocessor.max-attempts:5}")
    private int maxAttempts;

    public FailedAlertReprocessor(FailedAlertRepository repo, AlertService alertService, ObjectMapper objectMapper) {
        this.repo = repo;
        this.alertService = alertService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${alerts.reprocessor.delay:60000}")
    @Transactional
    public void reprocess() {
        try {
            List<FailedAlert> list = repo.findByAttemptsLessThan(maxAttempts);
            for (FailedAlert fa : list) {
                try {
                    CreateAlertRequest req = objectMapper.readValue(fa.getPayload(), CreateAlertRequest.class);
                    alertService.handleNewFocus(req);
                    repo.delete(fa);
                    log.info("Reprocessed failed alert id={} reportId={}", fa.getId(), fa.getReportId());
                } catch (Exception ex) {
                    fa.setAttempts(fa.getAttempts() + 1);
                    fa.setError(ex.getMessage());
                    repo.save(fa);
                    log.warn("Reprocess attempt failed for id={} attempts={} error={}", fa.getId(), fa.getAttempts(), ex.getMessage());
                }
            }
        } catch (Exception ex) {
            log.error("FailedAlertReprocessor outer error: {}", ex.getMessage());
        }
    }
}
