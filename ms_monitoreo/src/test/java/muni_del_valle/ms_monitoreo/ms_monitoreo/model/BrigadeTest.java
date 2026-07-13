package muni_del_valle.ms_monitoreo.ms_monitoreo.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrigadeTest {

    @Test
    void gettersSetters_deberianFuncionarCorrectamente() {
        Brigade b = new Brigade();
        b.setId(1L);
        b.setName("Brigada Norte");
        b.setActive(false);
        b.setAreaGeoJson("{\"type\":\"Point\"}");

        assertThat(b.getId()).isEqualTo(1L);
        assertThat(b.getName()).isEqualTo("Brigada Norte");
        assertThat(b.isActive()).isFalse();
        assertThat(b.getAreaGeoJson()).isEqualTo("{\"type\":\"Point\"}");
    }

    @Test
    void defaultActive_deberiaSerTrue() {
        Brigade b = new Brigade();
        assertThat(b.isActive()).isTrue();
    }

    @Test
    void setActive_true_deberiaQuedarActiva() {
        Brigade b = new Brigade();
        b.setActive(true);
        assertThat(b.isActive()).isTrue();
    }

    @Test
    void setName_null_deberiaPermitirlo() {
        Brigade b = new Brigade();
        b.setName(null);
        assertThat(b.getName()).isNull();
    }

    @Test
    void setAreaGeoJson_null_deberiaPermitirlo() {
        Brigade b = new Brigade();
        b.setAreaGeoJson(null);
        assertThat(b.getAreaGeoJson()).isNull();
    }
}
