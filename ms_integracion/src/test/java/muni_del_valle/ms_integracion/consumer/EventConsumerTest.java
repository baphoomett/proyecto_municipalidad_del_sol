package muni_del_valle.ms_integracion.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;

class EventConsumerTest {

    private EventConsumer eventConsumer;

    @BeforeEach
    void setUp() {
        eventConsumer = new EventConsumer();
    }

    @Test
    void receive_payloadJsonValido_procesaSinExcepcion() {
        String payload = "{\"tipo\":\"INCENDIO\",\"zona\":\"Sector Norte\",\"mensaje\":\"Foco detectado\"}";

        assertThatNoException().isThrownBy(() -> eventConsumer.receive(payload));
    }

    @Test
    void receive_payloadJsonParcial_procesaSinExcepcion() {
        String payload = "{\"tipo\":\"ALERTA\"}";

        assertThatNoException().isThrownBy(() -> eventConsumer.receive(payload));
    }

    @Test
    void receive_payloadNoJson_logsAdvertenciaYNoLanzaExcepcion() {
        String payload = "payload-no-es-json-valido";

        assertThatNoException().isThrownBy(() -> eventConsumer.receive(payload));
    }

    @Test
    void receive_payloadVacio_noLanzaExcepcion() {
        assertThatNoException().isThrownBy(() -> eventConsumer.receive(""));
    }

    @Test
    void receive_payloadNull_noLanzaExcepcion() {
        assertThatNoException().isThrownBy(() -> eventConsumer.receive(null));
    }

    @Test
    void receive_payloadJsonConCamposExtra_procesaSinExcepcion() {
        String payload = "{\"tipo\":\"MONITOREO\",\"zona\":\"Sur\",\"mensaje\":\"test\",\"extra\":\"campo\"}";

        assertThatNoException().isThrownBy(() -> eventConsumer.receive(payload));
    }
}
