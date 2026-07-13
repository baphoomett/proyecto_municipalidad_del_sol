package muni_del_valle.ms_reportes.ms_reportes.service;

import muni_del_valle.ms_reportes.ms_reportes.dto.CreateEventRequest;
import muni_del_valle.ms_reportes.ms_reportes.dto.CreateReportRequest;
import muni_del_valle.ms_reportes.ms_reportes.model.Event;
import muni_del_valle.ms_reportes.ms_reportes.model.EventType;
import muni_del_valle.ms_reportes.ms_reportes.model.Report;
import muni_del_valle.ms_reportes.ms_reportes.model.ReportStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportFactoryTest {

    private CreateReportRequest buildRequest(String severity, String incidentType) {
        CreateReportRequest req = new CreateReportRequest();
        req.setReporterEmail("reporter@test.com");
        req.setLatitude(-36.82);
        req.setLongitude(-73.05);
        req.setDescription("Foco de incendio detectado");
        req.setSeverity(severity);
        req.setIncidentType(incidentType);
        return req;
    }

    @Test
    void createReport_conTodosLosCampos_mapeaCorrectamente() {
        CreateReportRequest req = buildRequest("ALTA", "INCENDIO");
        req.setMediaUrls(List.of("http://img.test/foto.png"));

        Report report = ReportFactory.createReport(req);

        assertThat(report.getReporterEmail()).isEqualTo("reporter@test.com");
        assertThat(report.getLatitude()).isEqualTo(-36.82);
        assertThat(report.getLongitude()).isEqualTo(-73.05);
        assertThat(report.getDescription()).isEqualTo("Foco de incendio detectado");
        assertThat(report.getStatus()).isEqualTo(ReportStatus.ACTIVO);
        assertThat(report.getSeverity()).isEqualTo("ALTA");
        assertThat(report.getIncidentType()).isEqualTo("INCENDIO");
        assertThat(report.getMediaUrls()).contains("http://img.test/foto.png");
    }

    @Test
    void createReport_sinSeveridad_usaMEDIAPorDefecto() {
        CreateReportRequest req = buildRequest(null, "INCENDIO");

        Report report = ReportFactory.createReport(req);

        assertThat(report.getSeverity()).isEqualTo("MEDIA");
    }

    @Test
    void createReport_sinIncidentType_usaOTROPorDefecto() {
        CreateReportRequest req = buildRequest("ALTA", null);

        Report report = ReportFactory.createReport(req);

        assertThat(report.getIncidentType()).isEqualTo("OTRO");
    }

    @Test
    void createReport_sinMediaUrls_usaListaVacia() {
        CreateReportRequest req = buildRequest("ALTA", "INCENDIO");
        req.setMediaUrls(null);

        Report report = ReportFactory.createReport(req);

        assertThat(report.getStatus()).isEqualTo(ReportStatus.ACTIVO);
    }

    @Test
    void createReport_siempreAsignaStatusACTIVO() {
        Report report = ReportFactory.createReport(buildRequest("ALTA", "INCENDIO"));

        assertThat(report.getStatus()).isEqualTo(ReportStatus.ACTIVO);
    }

    @Test
    void createEvent_conTipoValido_mapeaEventType() {
        Report report = new Report();
        report.setId(1L);
        CreateEventRequest req = new CreateEventRequest();
        req.setType("DISPATCHED");
        req.setPayload("Brigada despachada al sector");

        Event event = ReportFactory.createEvent(report, req);

        assertThat(event.getType()).isEqualTo(EventType.DISPATCHED);
        assertThat(event.getPayload()).isEqualTo("Brigada despachada al sector");
        assertThat(event.getReport()).isEqualTo(report);
    }

    @Test
    void createEvent_conTipoCREATED_mapeaCorrectamente() {
        Report report = new Report();
        CreateEventRequest req = new CreateEventRequest();
        req.setType("CREATED");
        req.setPayload("Reporte creado");

        Event event = ReportFactory.createEvent(report, req);

        assertThat(event.getType()).isEqualTo(EventType.CREATED);
    }

    @Test
    void createEvent_conTipoInvalido_usaCREATEDPorDefecto() {
        Report report = new Report();
        CreateEventRequest req = new CreateEventRequest();
        req.setType("TIPO_INVALIDO");
        req.setPayload("payload");

        Event event = ReportFactory.createEvent(report, req);

        assertThat(event.getType()).isEqualTo(EventType.CREATED);
    }

    @Test
    void createEvent_conTipoCLOSED_mapeaCorrectamente() {
        Report report = new Report();
        CreateEventRequest req = new CreateEventRequest();
        req.setType("CLOSED");

        Event event = ReportFactory.createEvent(report, req);

        assertThat(event.getType()).isEqualTo(EventType.CLOSED);
    }
}
