package muni_del_valle.ms_reportes.ms_reportes.service;

import muni_del_valle.ms_reportes.ms_reportes.dto.CreateEventRequest;
import muni_del_valle.ms_reportes.ms_reportes.dto.CreateReportRequest;
import muni_del_valle.ms_reportes.ms_reportes.model.Event;
import muni_del_valle.ms_reportes.ms_reportes.model.EventType;
import muni_del_valle.ms_reportes.ms_reportes.model.Report;
import muni_del_valle.ms_reportes.ms_reportes.model.ReportStatus;
import muni_del_valle.ms_reportes.ms_reportes.repository.EventRepository;
import muni_del_valle.ms_reportes.ms_reportes.repository.ReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final EventRepository eventRepository;
    private final WebhookService webhookService;
    private final AlertWebhookService alertWebhookService;

    public ReportService(ReportRepository reportRepository, EventRepository eventRepository, 
                         WebhookService webhookService, AlertWebhookService alertWebhookService) {
        this.reportRepository = reportRepository;
        this.eventRepository = eventRepository;
        this.webhookService = webhookService;
        this.alertWebhookService = alertWebhookService;
    }

    @Transactional
    public Report createReport(CreateReportRequest req) {
        Report r = ReportFactory.createReport(req);
        Report saved = reportRepository.save(r);

        // create initial CREATED event
        CreateEventRequest evReq = new CreateEventRequest();
        evReq.setType(EventType.CREATED.name());
        evReq.setPayload("Report created");
        Event e = ReportFactory.createEvent(saved, evReq);
        eventRepository.save(e);

        // notify ms_monitoreo asynchronously
        try {
            webhookService.notifyMonitor(saved);
        } catch (Exception ex) {
            // swallow to avoid breaking report creation
        }

        // notify ms_alertas to generate alert + notifications
        try {
            alertWebhookService.notifyAlertas(saved);
        } catch (Exception ex) {
            // swallow to avoid breaking report creation
        }

        return saved;
    }

    public Page<Report> listReports(Optional<String> statusOpt, Pageable pageable) {
        if (statusOpt.isPresent()) {
            try {
                ReportStatus st = ReportStatus.valueOf(statusOpt.get());
                return reportRepository.findByStatus(st, pageable);
            } catch (Exception ex) {
                return reportRepository.findAll(pageable);
            }
        }
        return reportRepository.findAll(pageable);
    }

    @Transactional
    public Optional<Event> addEvent(Long reportId, CreateEventRequest req) {
        Optional<Report> or = reportRepository.findById(reportId);
        if (or.isEmpty()) return Optional.empty();
        Report r = or.get();
        Event e = ReportFactory.createEvent(r, req);
        Event saved = eventRepository.save(e);

        // update report status if event type maps to status
        try {
            EventType et = EventType.valueOf(req.getType());
            if (et == EventType.DISPATCHED) r.setStatus(ReportStatus.IN_PROGRESS);
            if (et == EventType.CLOSED) r.setStatus(ReportStatus.CLOSED);
            reportRepository.save(r);
        } catch (Exception ignored) {}

        return Optional.of(saved);
    }
}
