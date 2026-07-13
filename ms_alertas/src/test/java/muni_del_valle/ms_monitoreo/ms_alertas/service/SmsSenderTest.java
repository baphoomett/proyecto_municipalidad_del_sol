package muni_del_valle.ms_monitoreo.ms_alertas.service;

import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;

class SmsSenderTest {

    private SmsSender smsSender;

    @BeforeEach
    void setUp() {
        smsSender = new SmsSender();
    }

    @Test
    void sendAlert_conRequestCompleto_noLanzaExcepcion() {
        CreateAlertRequest req = new CreateAlertRequest();
        req.setReportId(1L);
        req.setDescription("Foco de incendio activo en sector norte");
        req.setSeverity("HIGH");
        req.setGeometry("POINT(-73.0 -36.8)");

        assertThatNoException().isThrownBy(() -> smsSender.sendAlert(req));
    }

    @Test
    void sendAlert_conSeveridadNull_noLanzaExcepcion() {
        CreateAlertRequest req = new CreateAlertRequest();
        req.setReportId(2L);
        req.setDescription("Descripción de prueba");

        assertThatNoException().isThrownBy(() -> smsSender.sendAlert(req));
    }

    @Test
    void sendAlert_conDescripcionNull_noLanzaExcepcion() {
        CreateAlertRequest req = new CreateAlertRequest();
        req.setReportId(3L);
        req.setSeverity("MEDIUM");

        assertThatNoException().isThrownBy(() -> smsSender.sendAlert(req));
    }

    @Test
    void sendAlert_conRequestNull_noLanzaExcepcion() {
        assertThatNoException().isThrownBy(() -> smsSender.sendAlert(null));
    }
}
