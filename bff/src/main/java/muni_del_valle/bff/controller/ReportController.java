package muni_del_valle.bff.controller;

import muni_del_valle.bff.dto.ReportDto;
import muni_del_valle.bff.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import muni_del_valle.bff.security.JwtUtil;

@RestController
@RequestMapping("/bff/reports")
@CrossOrigin(origins = "http://localhost:5173")
public class ReportController {

    private final ReportService reportService;
    private final JwtUtil jwtUtil;

    public ReportController(ReportService reportService, JwtUtil jwtUtil) {
        this.reportService = reportService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody ReportDto dto,
                                           @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return reportService.createReport(dto, token);
    }

    @GetMapping
    public ResponseEntity<?> getReports(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return reportService.getReports(token);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                           @RequestBody java.util.Map<String, String> body,
                                           @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String role = jwtUtil.extractRole(token);
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol de administrador");
        }
        return reportService.updateStatus(id, body.get("status"), token);
    }

    @DeleteMapping("/{id}/extinguish")
    public ResponseEntity<?> extinguishReport(@PathVariable Long id,
                                               @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String role = jwtUtil.extractRole(token);
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Acceso denegado: se requiere rol de administrador");
        }
        return reportService.extinguishReport(id, token);
    }
}