package muni_del_valle.ms_monitoreo.ms_alertas.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import muni_del_valle.ms_monitoreo.ms_alertas.model.FailedAlert;
import muni_del_valle.ms_monitoreo.ms_alertas.repository.FailedAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FailedAlertReprocessorTest {

    @Mock
    private FailedAlertRepository repo;

    @Mock
    private AlertService alertService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private FailedAlertReprocessor reprocessor;

    @BeforeEach
    void setUp() {
        reprocessor = new FailedAlertReprocessor(repo, alertService, objectMapper);
        ReflectionTestUtils.setField(reprocessor, "maxAttempts", 5);
    }

    private FailedAlert buildFailed(long reportId, String payload) {
        FailedAlert fa = new FailedAlert();
        fa.setId(1L);
        fa.setReportId(reportId);
        fa.setPayload(payload);
        fa.setAttempts(0);
        return fa;
    }

    private String validPayload() throws Exception {
        CreateAlertRequest req = new CreateAlertRequest();
        req.setReportId(7L);
        req.setGeometry("POINT(-73.0444 -36.8201)");
        req.setDescription("Foco activo");
        req.setSeverity("HIGH");
        return objectMapper.writeValueAsString(req);
    }

    @Test
    void reprocess_cuandoAlertServiceExitoso_eliminaFailedAlert() throws Exception {
        FailedAlert fa = buildFailed(7L, validPayload());
        when(repo.findByAttemptsLessThan(5)).thenReturn(List.of(fa));

        reprocessor.reprocess();

        verify(alertService).handleNewFocus(any(CreateAlertRequest.class));
        verify(repo).delete(fa);
    }

    @Test
    void reprocess_cuandoAlertServiceFalla_incrementaAttemptsYGuarda() throws Exception {
        FailedAlert fa = buildFailed(7L, validPayload());
        fa.setAttempts(2);
        when(repo.findByAttemptsLessThan(5)).thenReturn(List.of(fa));
        doThrow(new RuntimeException("SMTP down")).when(alertService).handleNewFocus(any());

        reprocessor.reprocess();

        assertThat(fa.getAttempts()).isEqualTo(3);
        assertThat(fa.getError()).isEqualTo("SMTP down");
        verify(repo).save(fa);
        verify(repo, never()).delete(any());
    }

    @Test
    void reprocess_listaVacia_noInteractuaConAlertService() {
        when(repo.findByAttemptsLessThan(5)).thenReturn(List.of());

        reprocessor.reprocess();

        verify(alertService, never()).handleNewFocus(any());
    }

    @Test
    void reprocess_cuandoPayloadJsonInvalido_incrementaAttempts() {
        FailedAlert fa = buildFailed(99L, "PAYLOAD_INVALIDO_NO_JSON");
        when(repo.findByAttemptsLessThan(5)).thenReturn(List.of(fa));

        reprocessor.reprocess();

        assertThat(fa.getAttempts()).isEqualTo(1);
        verify(repo).save(fa);
    }

    @Test
    void reprocess_cuandoFindByLanzaExcepcion_noPropagarError() {
        when(repo.findByAttemptsLessThan(5)).thenThrow(new RuntimeException("DB connection lost"));

        assertThatNoException().isThrownBy(() -> reprocessor.reprocess());
    }

    @Test
    void reprocess_procesaMultiplesFailedAlerts() throws Exception {
        FailedAlert fa1 = buildFailed(1L, validPayload());
        FailedAlert fa2 = buildFailed(2L, validPayload());
        when(repo.findByAttemptsLessThan(5)).thenReturn(List.of(fa1, fa2));

        reprocessor.reprocess();

        verify(alertService, times(2)).handleNewFocus(any());
        verify(repo).delete(fa1);
        verify(repo).delete(fa2);
    }
}
