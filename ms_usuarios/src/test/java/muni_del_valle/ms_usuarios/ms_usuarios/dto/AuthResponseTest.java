package muni_del_valle.ms_usuarios.ms_usuarios.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthResponseTest {

    @Test
    void constructorConToken_deberiaAsignarToken() {
        AuthResponse response = new AuthResponse("my.jwt.token");
        assertThat(response.getToken()).isEqualTo("my.jwt.token");
    }

    @Test
    void constructorVacio_deberiaCrearConTokenNull() {
        AuthResponse response = new AuthResponse();
        assertThat(response.getToken()).isNull();
    }

    @Test
    void setToken_deberiaActualizarToken() {
        AuthResponse response = new AuthResponse();
        response.setToken("new.token");
        assertThat(response.getToken()).isEqualTo("new.token");
    }
}
