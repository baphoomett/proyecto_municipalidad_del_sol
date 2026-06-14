package muni_del_valle.ms_reportes.ms_reportes.service;

import muni_del_valle.ms_reportes.ms_reportes.model.Report;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AlertWebhookService {

    private final RestTemplate restTemplate;

    @Value("${ms.alertas.url:http://ms_alertas:8083}")
    private String alertasUrl;

    public AlertWebhookService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void notifyAlertas(Report report) {
        try {
            String url = alertasUrl + "/api/alerts/webhook";
            Map<String, Object> payload = new HashMap<>();
            payload.put("reportId", report.getId());
            payload.put("description", report.getDescription());
            payload.put("severity", "HIGH");
            String geom = null;
            if (report.getLatitude() != null && report.getLongitude() != null) {
                geom = String.format("POINT(%s %s)", report.getLongitude(), report.getLatitude());
            }
            payload.put("geometry", geom);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception ex) {
            System.err.println("Failed to notify alertas: " + ex.getMessage());
        }
    }
}
