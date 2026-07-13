package muni_del_valle.ms_usuarios.ms_usuarios.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleModelTest {

    @Test
    void constructorConNombre_deberiaAsignarNombre() {
        Role role = new Role("ROLE_ADMIN");
        assertThat(role.getName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void constructorVacio_deberiaCrearRolSinNombre() {
        Role role = new Role();
        assertThat(role.getName()).isNull();
        assertThat(role.getId()).isNull();
    }

    @Test
    void settersGetters_deberianFuncionarCorrectamente() {
        Role role = new Role();
        role.setId(5L);
        role.setName("ROLE_USER");

        assertThat(role.getId()).isEqualTo(5L);
        assertThat(role.getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void setName_null_deberiaPermitirlo() {
        Role role = new Role("ROLE_GUEST");
        role.setName(null);
        assertThat(role.getName()).isNull();
    }
}
