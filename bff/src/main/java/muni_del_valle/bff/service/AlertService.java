package muni_del_valle.bff.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AlertService {

    private final RestTemplate restTemplate;
    private final String gatewayUrl;

    public AlertService(RestTemplate restTemplate, @Qualifier("gatewayUrl") String gatewayUrl) {
        this.restTemplate = restTemplate;
        this.gatewayUrl = gatewayUrl;
    }

    public ResponseEntity<?> getAlerts(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<Object> up = restTemplate.exchange(gatewayUrl + "/api/alerts", HttpMethod.GET, request, Object.class);
        return ResponseEntity.status(up.getStatusCode()).body(up.getBody());
    }
}