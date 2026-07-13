package muni_del_valle.ms_monitoreo.ms_alertas.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class FailedAlertModelTest {

    @Test
    void gettersSetters_deberianFuncionarCorrectamente() {
        FailedAlert fa = new FailedAlert();
        Instant now = Instant.now();

        fa.setId(2L);
        fa.setReportId(5L);
        fa.setPayload("{\"tipo\":\"test\"}");
        fa.setError("SMTP timeout");
        fa.setAttempts(3);
        fa.setCreatedAt(now);

        assertThat(fa.getId()).isEqualTo(2L);
        assertThat(fa.getReportId()).isEqualTo(5L);
        assertThat(fa.getPayload()).isEqualTo("{\"tipo\":\"test\"}");
        assertThat(fa.getError()).isEqualTo("SMTP timeout");
        assertThat(fa.getAttempts()).isEqualTo(3);
        assertThat(fa.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void defaultAttempts_deberiaSerCero() {
        FailedAlert fa = new FailedAlert();
        assertThat(fa.getAttempts()).isEqualTo(0);
    }

    @Test
    void prePersist_cuandoCreatedAtEsNull_deberiaAsignarlo() {
        FailedAlert fa = new FailedAlert();
        assertThat(fa.getCreatedAt()).isNull();
        fa.prePersist();
        assertThat(fa.getCreatedAt()).isNotNull();
    }

    @Test
    void prePersist_cuandoCreatedAtYaExiste_noDeberiaModificarlo() {
        FailedAlert fa = new FailedAlert();
        Instant existing = Instant.parse("2025-04-01T12:00:00Z");
        fa.setCreatedAt(existing);
        fa.prePersist();
        assertThat(fa.getCreatedAt()).isEqualTo(existing);
    }

    @Test
    void setPayload_null_deberiaPermitirlo() {
        FailedAlert fa = new FailedAlert();
        fa.setPayload(null);
        assertThat(fa.getPayload()).isNull();
    }

    @Test
    void setError_null_deberiaPermitirlo() {
        FailedAlert fa = new FailedAlert();
        fa.setError(null);
        assertThat(fa.getError()).isNull();
    }
}
