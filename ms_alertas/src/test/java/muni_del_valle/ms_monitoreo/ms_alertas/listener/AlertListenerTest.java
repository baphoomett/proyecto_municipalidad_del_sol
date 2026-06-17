package muni_del_valle.ms_monitoreo.ms_alertas.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import muni_del_valle.ms_monitoreo.ms_alertas.model.FailedAlert;
import muni_del_valle.ms_monitoreo.ms_alertas.repository.FailedAlertRepository;
import muni_del_valle.ms_monitoreo.ms_alertas.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertListenerTest {

    @Mock
    private AlertService alertService;

    @Mock
    private FailedAlertRepository failedAlertRepository;

    private AlertListener alertListener;

    @BeforeEach
    void setUp() {
        alertListener = new AlertListener(alertService, failedAlertRepository, new ObjectMapper());
    }

    private CreateAlertRequest buildRequest() {
        CreateAlertRequest req = new CreateAlertRequest();
        req.setReportId(7L);
        req.setGeometry("POINT(-73.0444 -36.8201)");
        req.setDescription("Foco activo");
        req.setSeverity("HIGH");
        return req;
    }

    @Test
    void onMessage_conPayloadValido_deberiaDelegarEnAlertService() {
        CreateAlertRequest req = buildRequest();

        alertListener.onMessage(req);

        verify(alertService, times(1)).handleNewFocus(req);
        verify(failedAlertRepository, never()).save(any());
    }

    @Test
    void onMessage_conPayloadNulo_deberiaIgnorarSinLlamarAlertService() {
        alertListener.onMessage(null);

        verify(alertService, never()).handleNewFocus(any());
        verify(failedAlertRepository, never()).save(any());
    }

    @Test
    void onMessage_siAlertServiceFalla_deberiaPersistirFailedAlertConElError() {
        CreateAlertRequest req = buildRequest();
        doThrow(new RuntimeException("ms_alertas: SMTP no disponible")).when(alertService).handleNewFocus(req);

        alertListener.onMessage(req);

        ArgumentCaptor<FailedAlert> captor = ArgumentCaptor.forClass(FailedAlert.class);
        verify(failedAlertRepository, times(1)).save(captor.capture());
        FailedAlert saved = captor.getValue();
        assertThat(saved.getReportId()).isEqualTo(7L);
        assertThat(saved.getError()).isEqualTo("ms_alertas: SMTP no disponible");
        assertThat(saved.getPayload()).contains("\"reportId\":7");
    }
}
