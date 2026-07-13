package muni_del_valle.ms_monitoreo.ms_alertas.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AlertModelTest {

    @Test
    void gettersSetters_deberianFuncionarCorrectamente() {
        Alert a = new Alert();
        Instant now = Instant.now();

        a.setId(1L);
        a.setReportId(10L);
        a.setSeverity("HIGH");
        a.setDescription("Incendio detectado");
        a.setStatus("CLOSED");
        a.setIncidentType("INCENDIO");
        a.setCreatedAt(now);

        assertThat(a.getId()).isEqualTo(1L);
        assertThat(a.getReportId()).isEqualTo(10L);
        assertThat(a.getSeverity()).isEqualTo("HIGH");
        assertThat(a.getDescription()).isEqualTo("Incendio detectado");
        assertThat(a.getStatus()).isEqualTo("CLOSED");
        assertThat(a.getIncidentType()).isEqualTo("INCENDIO");
        assertThat(a.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void defaultStatus_deberiaSerACTIVE() {
        Alert a = new Alert();
        assertThat(a.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void prePersist_cuandoCreatedAtEsNull_deberiaAsignarlo() {
        Alert a = new Alert();
        assertThat(a.getCreatedAt()).isNull();
        a.prePersist();
        assertThat(a.getCreatedAt()).isNotNull();
    }

    @Test
    void prePersist_cuandoCreatedAtYaExiste_noDeberiaModificarlo() {
        Alert a = new Alert();
        Instant existing = Instant.parse("2025-03-10T08:00:00Z");
        a.setCreatedAt(existing);
        a.prePersist();
        assertThat(a.getCreatedAt()).isEqualTo(existing);
    }

    @Test
    void setIncidentType_null_deberiaPermitirlo() {
        Alert a = new Alert();
        a.setIncidentType(null);
        assertThat(a.getIncidentType()).isNull();
    }
}
