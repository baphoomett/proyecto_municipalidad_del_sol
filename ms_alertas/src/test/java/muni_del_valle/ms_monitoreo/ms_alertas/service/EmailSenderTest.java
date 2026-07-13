package muni_del_valle.ms_monitoreo.ms_alertas.service;

import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSenderTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailSender emailSender;

    @BeforeEach
    void setUp() {
        emailSender = new EmailSender(mailSender);
        ReflectionTestUtils.setField(emailSender, "alertEmailTo", "alert@test.com,admin@test.com");
    }

    private CreateAlertRequest buildReq(String geometry, String severity) {
        CreateAlertRequest req = new CreateAlertRequest();
        req.setReportId(1L);
        req.setGeometry(geometry);
        req.setDescription("Foco de incendio detectado");
        req.setSeverity(severity);
        return req;
    }

    @Test
    void sendAlert_conGeometriaPOINT_enviaEmailConCoordenadas() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailSender.sendAlert(buildReq("POINT(-73.0444 -36.8201)", "HIGH"));

        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertThat(msg.getText()).contains("Latitud").contains("Longitud").contains("google.com/maps");
    }

    @Test
    void sendAlert_sinGeometria_enviaEmailSinUbicacion() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailSender.sendAlert(buildReq(null, "MEDIUM"));

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).contains("No disponible");
    }

    @Test
    void sendAlert_severidadHIGH_traduceAAlta() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailSender.sendAlert(buildReq("POINT(-73 -36)", "HIGH"));

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).contains("Alta");
    }

    @Test
    void sendAlert_severidadALTA_tambienTraduceAAlta() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailSender.sendAlert(buildReq("POINT(-73 -36)", "ALTA"));

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).contains("Alta");
    }

    @Test
    void sendAlert_severidadMEDIUM_traduceAMedia() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailSender.sendAlert(buildReq("POINT(-73 -36)", "MEDIUM"));

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).contains("Media");
    }

    @Test
    void sendAlert_severidadLOW_traduceABaja() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailSender.sendAlert(buildReq("POINT(-73 -36)", "LOW"));

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).contains("Baja");
    }

    @Test
    void sendAlert_severidadNull_usaSinDefinir() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailSender.sendAlert(buildReq("POINT(-73 -36)", null));

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).contains("Sin definir");
    }

    @Test
    void sendAlert_cuandoMailSenderFalla_lanzaRuntimeException() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailSender.sendAlert(buildReq("POINT(-73 -36)", "HIGH")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void sendAlert_geometriaFormatoInvalido_enviaEmailConUbicacionNoDisponible() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailSender.sendAlert(buildReq("NO_ES_POINT", "HIGH"));

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).contains("No disponible");
    }
}
