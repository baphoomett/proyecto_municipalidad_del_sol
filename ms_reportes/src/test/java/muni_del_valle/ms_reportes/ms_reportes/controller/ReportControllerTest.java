package muni_del_valle.ms_reportes.ms_reportes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni_del_valle.ms_reportes.ms_reportes.dto.CreateEventRequest;
import muni_del_valle.ms_reportes.ms_reportes.dto.CreateReportRequest;
import muni_del_valle.ms_reportes.ms_reportes.dto.UpdateStatusRequest;
import muni_del_valle.ms_reportes.ms_reportes.model.Event;
import muni_del_valle.ms_reportes.ms_reportes.model.Report;
import muni_del_valle.ms_reportes.ms_reportes.model.ReportStatus;
import muni_del_valle.ms_reportes.ms_reportes.service.ReportService;
import muni_del_valle.ms_reportes.ms_reportes.service.UploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private UploadService uploadService;

    private Report buildReport(Long id) {
        Report r = new Report();
        r.setId(id);
        r.setReporterEmail("test@municipalidad.cl");
        r.setLatitude(-36.8201);
        r.setLongitude(-73.0444);
        r.setDescription("Foco de incendio detectado");
        r.setStatus(ReportStatus.ACTIVO);
        return r;
    }

    @Test
    void createReport_conDatosValidos_deberiaRetornar201YElReporteCreado() throws Exception {
        CreateReportRequest req = new CreateReportRequest();
        req.setReporterEmail("test@municipalidad.cl");
        req.setLatitude(-36.8201);
        req.setLongitude(-73.0444);
        req.setDescription("Foco de incendio detectado");

        Report saved = buildReport(1L);
        when(reportService.createReport(any(CreateReportRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/reports")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/reports/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVO"));
    }

    @Test
    void listReports_sinFiltro_deberiaRetornar200ConPaginaDeReportes() throws Exception {
        Page<Report> page = new PageImpl<>(List.of(buildReport(1L), buildReport(2L)));
        when(reportService.listReports(any(Optional.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void addEvent_reporteExistente_deberiaRetornar200ConElEvento() throws Exception {
        CreateEventRequest req = new CreateEventRequest();
        req.setType("DISPATCHED");
        req.setPayload("Equipo despachado");

        Event event = new Event();
        event.setId(10L);
        when(reportService.addEvent(eq(5L), any(CreateEventRequest.class))).thenReturn(Optional.of(event));

        mockMvc.perform(post("/api/reports/5/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void addEvent_reporteInexistente_deberiaRetornar404() throws Exception {
        CreateEventRequest req = new CreateEventRequest();
        req.setType("DISPATCHED");
        when(reportService.addEvent(eq(99L), any(CreateEventRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/reports/99/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_estadoValido_deberiaRetornar200ConElReporteActualizado() throws Exception {
        UpdateStatusRequest req = new UpdateStatusRequest();
        req.setStatus("CONTROLADO");

        Report updated = buildReport(7L);
        updated.setStatus(ReportStatus.CONTROLADO);
        when(reportService.updateStatus(eq(7L), eq("CONTROLADO"))).thenReturn(Optional.of(updated));

        mockMvc.perform(patch("/api/reports/7/status")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONTROLADO"));
    }

    @Test
    void updateStatus_estadoInvalidoOReporteInexistente_deberiaRetornar404() throws Exception {
        UpdateStatusRequest req = new UpdateStatusRequest();
        req.setStatus("ESTADO_QUE_NO_EXISTE");

        when(reportService.updateStatus(eq(8L), eq("ESTADO_QUE_NO_EXISTE"))).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/reports/8/status")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReportForm_sinArchivos_deberiaRetornar201() throws Exception {
        Report saved = buildReport(10L);
        when(reportService.createReport(any(CreateReportRequest.class))).thenReturn(saved);

        mockMvc.perform(multipart("/api/reports/form")
                        .param("reporterEmail", "form@test.cl")
                        .param("description", "Reporte por formulario"))
                .andExpect(status().isCreated());
    }

    @Test
    void createReportForm_conArchivos_deberiaRetornar201() throws Exception {
        Report saved = buildReport(11L);
        MockMultipartFile file = new MockMultipartFile("files", "foto.jpg",
                "image/jpeg", "contenido".getBytes());
        when(uploadService.saveAll(any())).thenReturn(java.util.List.of("http://media/foto.jpg"));
        when(reportService.createReport(any(CreateReportRequest.class))).thenReturn(saved);

        mockMvc.perform(multipart("/api/reports/form").file(file)
                        .param("description", "Reporte con foto"))
                .andExpect(status().isCreated());
    }

    @Test
    void createReportForm_cuandoServiceLanzaIllegalArgument_deberiaRetornar400() throws Exception {
        when(reportService.createReport(any(CreateReportRequest.class)))
                .thenThrow(new IllegalArgumentException("Datos inválidos"));

        mockMvc.perform(multipart("/api/reports/form")
                        .param("description", "Reporte malo"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReportForm_cuandoServiceLanzaExcepcionGenerica_deberiaRetornar500() throws Exception {
        when(reportService.createReport(any(CreateReportRequest.class)))
                .thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(multipart("/api/reports/form")
                        .param("description", "Reporte con error"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteReport_existente_deberiaRetornar204() throws Exception {
        when(reportService.deleteReport(3L)).thenReturn(true);

        mockMvc.perform(delete("/api/reports/3"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReport_noExistente_deberiaRetornar404() throws Exception {
        when(reportService.deleteReport(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/reports/99"))
                .andExpect(status().isNotFound());
    }

}
