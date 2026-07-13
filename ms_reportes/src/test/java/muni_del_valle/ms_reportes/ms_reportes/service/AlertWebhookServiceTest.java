package muni_del_valle.ms_reportes.ms_reportes.service;

import muni_del_valle.ms_reportes.ms_reportes.model.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertWebhookServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private AlertWebhookService alertWebhookService;

    @BeforeEach
    void setUp() {
        alertWebhookService = new AlertWebhookService(restTemplate);
        ReflectionTestUtils.setField(alertWebhookService, "alertasUrl", "http://localhost:8083");
    }

    private Report buildReport(boolean withCoords) {
        Report r = new Report();
        r.setId(1L);
        r.setDescription("Incendio forestal");
        r.setSeverity("HIGH");
        r.setIncidentType("INCENDIO");
        if (withCoords) {
            r.setLatitude(-36.8201);
            r.setLongitude(-73.0444);
        }
        return r;
    }

    @Test
    void notifyAlertas_conCoordenadas_deberiaEnviarConGeometria() {
        Report report = buildReport(true);
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        alertWebhookService.notifyAlertas(report);

        verify(restTemplate, times(1)).postForEntity(
                eq("http://localhost:8083/api/alerts/webhook"), any(), eq(String.class));
    }

    @Test
    void notifyAlertas_sinCoordenadas_deberiaEnviarConGeometriaNull() {
        Report report = buildReport(false);
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        alertWebhookService.notifyAlertas(report);

        verify(restTemplate, times(1)).postForEntity(
                eq("http://localhost:8083/api/alerts/webhook"), any(), eq(String.class));
    }

    @Test
    void notifyAlertas_cuandoRestTemplateFalla_noDeberiaPropagarExcepcion() {
        Report report = buildReport(true);
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        alertWebhookService.notifyAlertas(report);

        verify(restTemplate, times(1)).postForEntity(any(String.class), any(), eq(String.class));
    }

    @Test
    void notifyAlertas_conLatitudNull_deberiaEnviarConGeometriaNull() {
        Report report = buildReport(false);
        report.setLongitude(-73.0444);
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        alertWebhookService.notifyAlertas(report);

        verify(restTemplate).postForEntity(any(String.class), any(), eq(String.class));
    }
}
