package muni_del_valle.ms_reportes.ms_reportes.service;

import muni_del_valle.ms_reportes.ms_reportes.dto.CreateEventRequest;
import muni_del_valle.ms_reportes.ms_reportes.dto.CreateReportRequest;
import muni_del_valle.ms_reportes.ms_reportes.model.Event;
import muni_del_valle.ms_reportes.ms_reportes.model.EventType;
import muni_del_valle.ms_reportes.ms_reportes.model.Report;
import muni_del_valle.ms_reportes.ms_reportes.model.ReportStatus;
import muni_del_valle.ms_reportes.ms_reportes.repository.EventRepository;
import muni_del_valle.ms_reportes.ms_reportes.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private WebhookService webhookService;

    @Mock
    private AlertWebhookService alertWebhookService;

    @InjectMocks
    private ReportService reportService;

    private CreateReportRequest buildRequest() {
        CreateReportRequest req = new CreateReportRequest();
        req.setReporterEmail("test@municipalidad.cl");
        req.setLatitude(-36.8201);
        req.setLongitude(-73.0444);
        req.setDescription("Foco de incendio detectado");
        req.setSeverity("ALTA");
        req.setIncidentType("FORESTAL");
        return req;
    }

    @Test
    void createReport_deberiaGuardarReporteYEventoInicial() {
        CreateReportRequest req = buildRequest();

        Report saved = ReportFactory.createReport(req);
        saved.setId(1L);
        when(reportRepository.save(any(Report.class))).thenReturn(saved);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        Report result = reportService.createReport(req);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(ReportStatus.ACTIVO);
        assertThat(result.getSeverity()).isEqualTo("ALTA");
        assertThat(result.getIncidentType()).isEqualTo("FORESTAL");

        verify(reportRepository, times(1)).save(any(Report.class));
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void createReport_deberiaNotificarAMonitoreoYAlertas() {
        CreateReportRequest req = buildRequest();
        Report saved = ReportFactory.createReport(req);
        saved.setId(2L);
        when(reportRepository.save(any(Report.class))).thenReturn(saved);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        reportService.createReport(req);

        verify(webhookService, times(1)).notifyMonitor(any(Report.class));
        verify(alertWebhookService, times(1)).notifyAlertas(any(Report.class));
    }

    @Test
    void createReport_noDeberiaFallarSiNotificacionMonitorLanzaExcepcion() {
        CreateReportRequest req = buildRequest();
        Report saved = ReportFactory.createReport(req);
        saved.setId(3L);
        when(reportRepository.save(any(Report.class))).thenReturn(saved);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());
        doThrow(new RuntimeException("ms_monitoreo no disponible"))
                .when(webhookService).notifyMonitor(any(Report.class));

        Report result = reportService.createReport(req);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
    }

    @Test
    void createReport_noDeberiaFallarSiNotificacionAlertasLanzaExcepcion() {
        CreateReportRequest req = buildRequest();
        Report saved = ReportFactory.createReport(req);
        saved.setId(4L);
        when(reportRepository.save(any(Report.class))).thenReturn(saved);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());
        doThrow(new RuntimeException("ms_alertas no disponible"))
                .when(alertWebhookService).notifyAlertas(any(Report.class));

        Report result = reportService.createReport(req);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(4L);
    }

    @Test
    void listReports_sinFiltro_deberiaRetornarTodos() {
        Pageable pageable = mock(Pageable.class);
        Page<Report> page = new PageImpl<>(List.of(new Report(), new Report()));
        when(reportRepository.findAll(pageable)).thenReturn(page);

        Page<Report> result = reportService.listReports(Optional.empty(), pageable);

        assertThat(result.getContent()).hasSize(2);
        verify(reportRepository, times(1)).findAll(pageable);
        verify(reportRepository, never()).findByStatus(any(), any());
    }

    @Test
    void listReports_conFiltroValido_deberiaFiltrarPorStatus() {
        Pageable pageable = mock(Pageable.class);
        Page<Report> page = new PageImpl<>(List.of(new Report()));
        when(reportRepository.findByStatus(ReportStatus.ACTIVO, pageable)).thenReturn(page);

        Page<Report> result = reportService.listReports(Optional.of("ACTIVO"), pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(reportRepository, times(1)).findByStatus(ReportStatus.ACTIVO, pageable);
    }

    @Test
    void listReports_conFiltroInvalido_deberiaRetornarTodosSinFallar() {
        Pageable pageable = mock(Pageable.class);
        Page<Report> page = new PageImpl<>(List.of(new Report()));
        when(reportRepository.findAll(pageable)).thenReturn(page);

        Page<Report> result = reportService.listReports(Optional.of("ESTADO_INEXISTENTE"), pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(reportRepository, times(1)).findAll(pageable);
    }

    @Test
    void addEvent_reporteExistente_deberiaGuardarEventoYActualizarStatusADespachado() {
        Report report = new Report();
        report.setId(5L);
        report.setStatus(ReportStatus.ACTIVO);

        CreateEventRequest req = new CreateEventRequest();
        req.setType("DISPATCHED");
        req.setPayload("Equipo despachado al lugar");

        when(reportRepository.findById(5L)).thenReturn(Optional.of(report));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Event> result = reportService.addEvent(5L, req);

        assertThat(result).isPresent();
        assertThat(report.getStatus()).isEqualTo(ReportStatus.EN_COMBATE);
        verify(reportRepository, times(1)).save(report);
    }

    @Test
    void addEvent_eventoClosed_deberiaActualizarStatusAExtinguido() {
        Report report = new Report();
        report.setId(6L);
        report.setStatus(ReportStatus.EN_COMBATE);

        CreateEventRequest req = new CreateEventRequest();
        req.setType("CLOSED");

        when(reportRepository.findById(6L)).thenReturn(Optional.of(report));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        reportService.addEvent(6L, req);

        assertThat(report.getStatus()).isEqualTo(ReportStatus.EXTINGUIDO);
    }

    @Test
    void addEvent_reporteInexistente_deberiaRetornarOptionalVacio() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Event> result = reportService.addEvent(99L, new CreateEventRequest());

        assertThat(result).isEmpty();
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateStatus_reporteExistenteYEstadoValido_deberiaActualizar() {
        Report report = new Report();
        report.setId(7L);
        report.setStatus(ReportStatus.ACTIVO);

        when(reportRepository.findById(7L)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Report> result = reportService.updateStatus(7L, "CONTROLADO");

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(ReportStatus.CONTROLADO);
    }

    @Test
    void updateStatus_estadoInvalido_deberiaRetornarOptionalVacio() {
        Report report = new Report();
        report.setId(8L);
        report.setStatus(ReportStatus.ACTIVO);

        when(reportRepository.findById(8L)).thenReturn(Optional.of(report));

        Optional<Report> result = reportService.updateStatus(8L, "ESTADO_QUE_NO_EXISTE");

        assertThat(result).isEmpty();
        verify(reportRepository, never()).save(any());
    }

    @Test
    void updateStatus_reporteInexistente_deberiaRetornarOptionalVacio() {
        when(reportRepository.findById(100L)).thenReturn(Optional.empty());

        Optional<Report> result = reportService.updateStatus(100L, "ACTIVO");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteReport_reporteExistente_deberiaEliminarYRetornarTrue() {
        Report report = new Report();
        report.setId(20L);
        when(reportRepository.findById(20L)).thenReturn(Optional.of(report));

        boolean result = reportService.deleteReport(20L);

        assertThat(result).isTrue();
        verify(eventRepository).deleteByReportId(20L);
        verify(reportRepository).delete(report);
    }

    @Test
    void deleteReport_reporteInexistente_deberiaRetornarFalse() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = reportService.deleteReport(999L);

        assertThat(result).isFalse();
        verify(eventRepository, never()).deleteByReportId(any());
        verify(reportRepository, never()).delete(any(Report.class));
    }

    @Test
    void addEvent_conTipoEventoInvalido_deberiaGuardarEventoSinActualizarStatus() {
        Report report = new Report();
        report.setId(10L);
        report.setStatus(ReportStatus.ACTIVO);

        CreateEventRequest req = new CreateEventRequest();
        req.setType("TIPO_QUE_NO_EXISTE");

        when(reportRepository.findById(10L)).thenReturn(Optional.of(report));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Event> result = reportService.addEvent(10L, req);

        assertThat(result).isPresent();
        assertThat(report.getStatus()).isEqualTo(ReportStatus.ACTIVO);
    }
}
