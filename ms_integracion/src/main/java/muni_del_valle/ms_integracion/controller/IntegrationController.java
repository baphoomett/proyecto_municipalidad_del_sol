package muni_del_valle.ms_integracion.controller;

import muni_del_valle.ms_integracion.service.MinioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class IntegrationController {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationController.class);
    private final MinioService minioService;

    public IntegrationController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/minio/presigned")
    public ResponseEntity<?> presigned(@RequestBody Map<String, Object> body) {
        String objectName = (String) body.get("objectName");
        Integer expiry = body.get("expirySeconds") != null ? (Integer) body.get("expirySeconds") : 3600;
        if (objectName == null || objectName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "objectName is required"));
        }
        try {
            String url = minioService.generatePresignedUrl(objectName, expiry);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception ex) {
            logger.error("Error generating presigned URL", ex);
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
