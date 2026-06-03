package muni_del_valle.ms_reportes.ms_reportes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import muni_del_valle.ms_reportes.ms_reportes.model.Report;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Date;

@Service
public class WebhookService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ms.monitor.url:http://localhost:8082}")
    private String monitorUrl;

    @Value("${jwt.secret:changeitchangethis}")
    private String jwtSecret;

    public WebhookService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void notifyMonitor(Report report) {
        try {
            String url = monitorUrl + "/api/monitor/focus";
            // build minimal payload with geometry as GeoJSON Point
            String geom = null;
            if (report.getLatitude() != null && report.getLongitude() != null) {
                // send WKT POINT(lon lat) to simplify parsing in monitor service
                geom = String.format("POINT(%s %s)", report.getLongitude(), report.getLatitude());
            }
            var payload = new java.util.HashMap<String,Object>();
            payload.put("reportId", report.getId());
            payload.put("geometry", geom);
            payload.put("description", report.getDescription());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(generateServiceToken());
            String body = objectMapper.writeValueAsString(payload);
            HttpEntity<String> ent = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, ent, String.class);
        } catch (Exception ex) {
            // log and continue (non-fatal)
            System.err.println("Failed to notify monitor: " + ex.getMessage());
        }
    }

    private String generateServiceToken() {
        try {
            // Ensure key length is sufficient by deriving a 256-bit key from the secret
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Key key = Keys.hmacShaKeyFor(keyBytes);
            Date now = new Date();
            Date exp = new Date(now.getTime() + 3600_000);
            return Jwts.builder().setSubject("ms_reportes").setIssuedAt(now).setExpiration(exp).signWith(key).compact();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate service JWT: " + ex.getMessage(), ex);
        }
    }
}
