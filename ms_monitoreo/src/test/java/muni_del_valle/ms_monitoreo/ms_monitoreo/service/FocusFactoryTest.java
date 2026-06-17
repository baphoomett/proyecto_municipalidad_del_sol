package muni_del_valle.ms_monitoreo.ms_monitoreo.service;

import muni_del_valle.ms_monitoreo.ms_monitoreo.dto.CreateFocusRequest;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.Focus;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FocusFactoryTest {

    private CreateFocusRequest buildRequest(String wkt, String severity) {
        CreateFocusRequest req = new CreateFocusRequest();
        req.setReportId(1L);
        req.setGeometry(wkt);
        req.setDescription("Foco detectado por sensor satelital");
        req.setSeverity(severity);
        return req;
    }

    @Test
    void createFocus_conWktValidoYSeveridadValida_deberiaCrearFocoConGeometriaYSeveridad() {
        CreateFocusRequest req = buildRequest("POINT(-73.0444 -36.8201)", "HIGH");

        Focus focus = FocusFactory.createFocus(req);

        assertThat(focus.getReportId()).isEqualTo(1L);
        assertThat(focus.getGeometry()).isNotNull();
        assertThat(focus.getGeometry().getGeometryType()).isEqualTo("Point");
        assertThat(focus.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(focus.getDescription()).isEqualTo("Foco detectado por sensor satelital");
    }

    @Test
    void createFocus_conWktInvalido_deberiaLanzarIllegalArgumentException() {
        CreateFocusRequest req = buildRequest("ESTO_NO_ES_WKT", "HIGH");

        assertThatThrownBy(() -> FocusFactory.createFocus(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid geometry WKT");
    }

    @Test
    void createFocus_conSeveridadInvalida_deberiaUsarMediumPorDefecto() {
        CreateFocusRequest req = buildRequest("POINT(-73.0444 -36.8201)", "SEVERIDAD_QUE_NO_EXISTE");

        Focus focus = FocusFactory.createFocus(req);

        assertThat(focus.getSeverity()).isEqualTo(Severity.MEDIUM);
    }

    @Test
    void createFocus_conSeveridadNula_deberiaUsarMediumPorDefecto() {
        CreateFocusRequest req = buildRequest("POINT(-73.0444 -36.8201)", null);

        Focus focus = FocusFactory.createFocus(req);

        assertThat(focus.getSeverity()).isEqualTo(Severity.MEDIUM);
    }

    @Test
    void createFocus_sinGeometria_deberiaCrearFocoConGeometriaNula() {
        CreateFocusRequest req = buildRequest(null, "LOW");

        Focus focus = FocusFactory.createFocus(req);

        assertThat(focus.getGeometry()).isNull();
        assertThat(focus.getSeverity()).isEqualTo(Severity.LOW);
    }
}
