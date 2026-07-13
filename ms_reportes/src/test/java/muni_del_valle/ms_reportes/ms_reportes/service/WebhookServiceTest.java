package muni_del_valle.ms_reportes.ms_reportes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni_del_valle.ms_reportes.ms_reportes.model.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private WebhookService webhookService;

    private static final String SECRET =
            "changeitchangethischangethisisverysecretkeyforjwthmacsha256algorithm";

    @BeforeEach
    void setUp() {
        webhookService = new WebhookService(restTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(webhookService, "monitorUrl", "http://monitor:8082");
        ReflectionTestUtils.setField(webhookService, "jwtSecret", SECRET);
    }

    private Report buildReport(Double lat, Double lon) {
        Report r = new Report();
        r.setId(1L);
        r.setDescription("Foco detectado");
        r.setLatitude(lat);
        r.setLongitude(lon);
        return r;
    }

    @Test
    void notifyMonitor_conLatLon_enviaRequestAlMonitor() {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));

        webhookService.notifyMonitor(buildReport(-36.82, -73.05));

        verify(restTemplate).postForEntity(
                eq("http://monitor:8082/api/monitor/focus"), any(), eq(String.class));
    }

    @Test
    void notifyMonitor_sinLatLon_enviaRequestSinGeometria() {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));

        webhookService.notifyMonitor(buildReport(null, null));

        verify(restTemplate).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void notifyMonitor_cuandoRestTemplateFalla_noPropagarExcepcion() {
        doThrow(new RuntimeException("Connection refused"))
                .when(restTemplate).postForEntity(anyString(), any(), eq(String.class));

        assertThatNoException().isThrownBy(() -> webhookService.notifyMonitor(buildReport(-36.82, -73.05)));
    }

    @Test
    void notifyMonitor_cuandoMonitorRetorna500_noPropagarExcepcion() {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.status(500).body("error"));

        assertThatNoException().isThrownBy(() -> webhookService.notifyMonitor(buildReport(-36.82, -73.05)));
    }
}
