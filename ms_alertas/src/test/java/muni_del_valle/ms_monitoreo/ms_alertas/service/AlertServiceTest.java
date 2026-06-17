package muni_del_valle.ms_monitoreo.ms_alertas.service;

import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private SmsSender smsSender;

    @InjectMocks
    private AlertService alertService;

    private CreateAlertRequest buildRequest() {
        CreateAlertRequest req = new CreateAlertRequest();
        req.setReportId(1L);
        req.setGeometry("POINT(-73.0444 -36.8201)");
        req.setDescription("Foco de incendio detectado");
        req.setSeverity("HIGH");
        return req;
    }

    @Test
    void handleNewFocus_deberiaEnviarEmailYSms() {
        CreateAlertRequest req = buildRequest();

        alertService.handleNewFocus(req);

        verify(emailSender, times(1)).sendAlert(req);
        verify(smsSender, times(1)).sendAlert(req);
    }

    @Test
    void handleNewFocus_siEmailSenderFalla_deberiaPropagarExcepcionYNoEnviarSms() {
        CreateAlertRequest req = buildRequest();
        doThrow(new RuntimeException("SMTP no disponible")).when(emailSender).sendAlert(req);

        assertThatThrownBy(() -> alertService.handleNewFocus(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("SMTP no disponible");

        verify(smsSender, never()).sendAlert(any());
    }
}
