package muni_del_valle.ms_reportes.ms_reportes.service;

import muni_del_valle.ms_reportes.ms_reportes.dto.CreateReportRequest;
import muni_del_valle.ms_reportes.ms_reportes.dto.CreateEventRequest;
import muni_del_valle.ms_reportes.ms_reportes.model.Event;
import muni_del_valle.ms_reportes.ms_reportes.model.EventType;
import muni_del_valle.ms_reportes.ms_reportes.model.Report;
import muni_del_valle.ms_reportes.ms_reportes.model.ReportStatus;

public class ReportFactory {

    public static Report createReport(CreateReportRequest req) {
        Report r = new Report();
        r.setReporterEmail(req.getReporterEmail());
        r.setLatitude(req.getLatitude());
        r.setLongitude(req.getLongitude());
        r.setDescription(req.getDescription());
        if (req.getMediaUrls() != null) r.setMediaUrls(req.getMediaUrls());
        r.setStatus(ReportStatus.NEW);
        return r;
    }

    public static Event createEvent(Report report, CreateEventRequest req) {
        Event e = new Event();
        e.setReport(report);
        try {
            e.setType(EventType.valueOf(req.getType()));
        } catch (Exception ex) {
            e.setType(EventType.CREATED);
        }
        e.setPayload(req.getPayload());
        return e;
    }
}
