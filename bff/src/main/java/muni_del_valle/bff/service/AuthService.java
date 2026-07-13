package muni_del_valle.bff.service;

import muni_del_valle.bff.dto.AuthDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Service
public class AuthService {

    private final RestTemplate restTemplate;
    private final String gatewayUrl;

    public AuthService(RestTemplate restTemplate, @Qualifier("gatewayUrl") String gatewayUrl) {
        this.restTemplate = restTemplate;
        this.gatewayUrl = gatewayUrl;
    }

    public ResponseEntity<?> login(AuthDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthDto> request = new HttpEntity<>(dto, headers);
        ResponseEntity<Object> upstream = restTemplate.postForEntity(gatewayUrl + "/api/auth/login", request, Object.class);
        return ResponseEntity.status(upstream.getStatusCode()).body(upstream.getBody());
    }

    public ResponseEntity<?> register(AuthDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthDto> request = new HttpEntity<>(dto, headers);
        ResponseEntity<Object> upstream = restTemplate.postForEntity(gatewayUrl + "/api/auth/register", request, Object.class);
        return ResponseEntity.status(upstream.getStatusCode()).body(upstream.getBody());
    }
}