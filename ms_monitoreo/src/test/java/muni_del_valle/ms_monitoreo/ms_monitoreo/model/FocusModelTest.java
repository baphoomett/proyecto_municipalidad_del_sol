package muni_del_valle.ms_monitoreo.ms_monitoreo.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class FocusModelTest {

    @Test
    void gettersSetters_deberianFuncionarCorrectamente() {
        Focus f = new Focus();
        Instant now = Instant.now();
        f.setId(5L);
        f.setReportId(10L);
        f.setDescription("Descripción de prueba");
        f.setStatus(FocusStatus.VERIFIED);
        f.setSeverity(Severity.HIGH);
        f.setCreatedAt(now);

        assertThat(f.getId()).isEqualTo(5L);
        assertThat(f.getReportId()).isEqualTo(10L);
        assertThat(f.getDescription()).isEqualTo("Descripción de prueba");
        assertThat(f.getStatus()).isEqualTo(FocusStatus.VERIFIED);
        assertThat(f.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(f.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void prePersist_cuandoCreatedAtEsNull_deberiaAsignarloAutomaticamente() {
        Focus f = new Focus();
        assertThat(f.getCreatedAt()).isNull();
        f.prePersist();
        assertThat(f.getCreatedAt()).isNotNull();
    }

    @Test
    void prePersist_cuandoCreatedAtYaExiste_noDeberiaModificarlo() {
        Focus f = new Focus();
        Instant existing = Instant.parse("2025-01-01T00:00:00Z");
        f.setCreatedAt(existing);
        f.prePersist();
        assertThat(f.getCreatedAt()).isEqualTo(existing);
    }

    @Test
    void defaultStatus_deberiaSerNEW() {
        Focus f = new Focus();
        assertThat(f.getStatus()).isEqualTo(FocusStatus.NEW);
    }

    @Test
    void defaultSeverity_deberiaSerMEDIUM() {
        Focus f = new Focus();
        assertThat(f.getSeverity()).isEqualTo(Severity.MEDIUM);
    }

    @Test
    void setGeometry_null_deberiaPermitirlo() {
        Focus f = new Focus();
        f.setGeometry(null);
        assertThat(f.getGeometry()).isNull();
    }
}
