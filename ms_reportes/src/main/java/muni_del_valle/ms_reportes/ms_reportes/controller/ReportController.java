package muni_del_valle.ms_reportes.ms_reportes.controller;

import muni_del_valle.ms_reportes.ms_reportes.dto.CreateEventRequest;
import muni_del_valle.ms_reportes.ms_reportes.dto.CreateReportRequest;
import muni_del_valle.ms_reportes.ms_reportes.model.Event;
import muni_del_valle.ms_reportes.ms_reportes.model.Report;
import muni_del_valle.ms_reportes.ms_reportes.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final muni_del_valle.ms_reportes.ms_reportes.service.UploadService uploadService;

    public ReportController(ReportService reportService, muni_del_valle.ms_reportes.ms_reportes.service.UploadService uploadService) {
        this.reportService = reportService;
        this.uploadService = uploadService;
    }

    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody CreateReportRequest req) {
        Report r = reportService.createReport(req);
        return ResponseEntity.created(URI.create("/api/reports/" + r.getId())).body(r);
    }

    @PostMapping(path = "/form", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createReportForm(@RequestParam(required = false) String reporterEmail,
                                              @RequestParam(required = false) Double latitude,
                                              @RequestParam(required = false) Double longitude,
                                              @RequestParam(required = false) String description,
                                              @RequestParam(required = false) MultipartFile[] files) {
        try {
            java.util.List<String> mediaUrls = new java.util.ArrayList<>();
            if (files != null && files.length > 0) {
                // delegate to UploadService
                mediaUrls = uploadService.saveAll(files);
            }
            CreateReportRequest req = new CreateReportRequest();
            req.setReporterEmail(reporterEmail);
            req.setLatitude(latitude);
            req.setLongitude(longitude);
            req.setDescription(description);
            req.setMediaUrls(mediaUrls);
            Report r = reportService.createReport(req);
            return ResponseEntity.created(URI.create("/api/reports/" + r.getId())).body(r);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listReports(@RequestParam Optional<String> status,
                                         @RequestParam Optional<Integer> page,
                                         @RequestParam Optional<Integer> size) {
        int p = page.orElse(0);
        int s = size.orElse(20);
        Pageable pageable = PageRequest.of(p, s);
        Page<Report> reports = reportService.listReports(status, pageable);
        return ResponseEntity.ok(reports);
    }

    @PostMapping("/{id}/events")
    public ResponseEntity<?> addEvent(@PathVariable Long id, @RequestBody CreateEventRequest req) {
        Optional<Event> oe = reportService.addEvent(id, req);
        return oe.map(e -> ResponseEntity.ok(e)).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
