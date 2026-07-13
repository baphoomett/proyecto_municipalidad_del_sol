package muni_del_valle.ms_reportes.ms_reportes.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportModelTest {

    @Test
    void gettersSetters_deberianFuncionarCorrectamente() {
        Report r = new Report();
        Instant now = Instant.now();

        r.setId(1L);
        r.setReporterEmail("user@test.cl");
        r.setLatitude(-36.8);
        r.setLongitude(-73.0);
        r.setDescription("Descripción");
        r.setMediaUrls(List.of("http://media1.jpg"));
        r.setStatus(ReportStatus.CONTROLADO);
        r.setSeverity("ALTA");
        r.setIncidentType("INCENDIO");
        r.setCreatedAt(now);

        assertThat(r.getId()).isEqualTo(1L);
        assertThat(r.getReporterEmail()).isEqualTo("user@test.cl");
        assertThat(r.getLatitude()).isEqualTo(-36.8);
        assertThat(r.getLongitude()).isEqualTo(-73.0);
        assertThat(r.getDescription()).isEqualTo("Descripción");
        assertThat(r.getMediaUrls()).containsExactly("http://media1.jpg");
        assertThat(r.getStatus()).isEqualTo(ReportStatus.CONTROLADO);
        assertThat(r.getSeverity()).isEqualTo("ALTA");
        assertThat(r.getIncidentType()).isEqualTo("INCENDIO");
        assertThat(r.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void prePersist_cuandoCreatedAtEsNull_deberiaAsignarlo() {
        Report r = new Report();
        assertThat(r.getCreatedAt()).isNull();
        r.prePersist();
        assertThat(r.getCreatedAt()).isNotNull();
    }

    @Test
    void prePersist_cuandoCreatedAtYaExiste_noDeberiaModificarlo() {
        Report r = new Report();
        Instant existing = Instant.parse("2025-01-15T10:00:00Z");
        r.setCreatedAt(existing);
        r.prePersist();
        assertThat(r.getCreatedAt()).isEqualTo(existing);
    }

    @Test
    void defaultStatus_deberiaSerACTIVO() {
        Report r = new Report();
        assertThat(r.getStatus()).isEqualTo(ReportStatus.ACTIVO);
    }

    @Test
    void defaultSeverity_deberiaSerMEDIA() {
        Report r = new Report();
        assertThat(r.getSeverity()).isEqualTo("MEDIA");
    }

    @Test
    void defaultIncidentType_deberiaSerOTRO() {
        Report r = new Report();
        assertThat(r.getIncidentType()).isEqualTo("OTRO");
    }
}
