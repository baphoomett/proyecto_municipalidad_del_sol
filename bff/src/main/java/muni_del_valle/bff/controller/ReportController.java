package muni_del_valle.bff.controller;

import muni_del_valle.bff.dto.ReportDto;
import muni_del_valle.bff.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bff/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
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
}