package muni_del_valle.bff.service;

import muni_del_valle.bff.dto.ReportDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ReportService {

    private final RestTemplate restTemplate;
    private final String gatewayUrl;

    public ReportService(RestTemplate restTemplate, @Qualifier("gatewayUrl") String gatewayUrl) {
        this.restTemplate = restTemplate;
        this.gatewayUrl = gatewayUrl;
    }

    public ResponseEntity<?> createReport(ReportDto dto, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<ReportDto> request = new HttpEntity<>(dto, headers);
        return restTemplate.postForEntity(gatewayUrl + "/api/reports", request, Object.class);
    }

    public ResponseEntity<?> getReports(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> request = new HttpEntity<>(headers);
        return restTemplate.exchange(gatewayUrl + "/api/reports", HttpMethod.GET, request, Object.class);
    }

    public ResponseEntity<?> updateStatus(Long id, String status, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("status", status);
        HttpEntity<java.util.Map<String, String>> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(gatewayUrl + "/api/reports/" + id + "/status", HttpMethod.PATCH, request, Object.class);
    }
}