package muni_del_valle.ms_reportes.ms_reportes.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EventModelTest {

    @Test
    void gettersSetters_deberianFuncionarCorrectamente() {
        Event e = new Event();
        Report r = new Report();
        Instant now = Instant.now();

        e.setId(1L);
        e.setReport(r);
        e.setType(EventType.DISPATCHED);
        e.setPayload("payload de prueba");
        e.setCreatedAt(now);

        assertThat(e.getId()).isEqualTo(1L);
        assertThat(e.getReport()).isSameAs(r);
        assertThat(e.getType()).isEqualTo(EventType.DISPATCHED);
        assertThat(e.getPayload()).isEqualTo("payload de prueba");
        assertThat(e.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void prePersist_cuandoCreatedAtEsNull_deberiaAsignarlo() {
        Event e = new Event();
        assertThat(e.getCreatedAt()).isNull();
        e.prePersist();
        assertThat(e.getCreatedAt()).isNotNull();
    }

    @Test
    void prePersist_cuandoCreatedAtYaExiste_noDeberiaModificarlo() {
        Event e = new Event();
        Instant existing = Instant.parse("2025-06-01T00:00:00Z");
        e.setCreatedAt(existing);
        e.prePersist();
        assertThat(e.getCreatedAt()).isEqualTo(existing);
    }

    @Test
    void setType_null_deberiaPermitirlo() {
        Event e = new Event();
        e.setType(null);
        assertThat(e.getType()).isNull();
    }
}
