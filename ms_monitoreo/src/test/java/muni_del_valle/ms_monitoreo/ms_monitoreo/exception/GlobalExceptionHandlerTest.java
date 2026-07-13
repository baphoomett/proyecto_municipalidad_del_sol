package muni_del_valle.ms_monitoreo.ms_monitoreo.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBadRequest_deberiaRetornar400ConMensajeDeError() {
        IllegalArgumentException ex = new IllegalArgumentException("Dato inválido");
        ResponseEntity<?> response = handler.handleBadRequest(ex);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().toString()).contains("Dato inválido");
    }

    @Test
    void handleBadRequest_conMensajeVacio_deberiaRetornar400() {
        ResponseEntity<?> response = handler.handleBadRequest(new IllegalArgumentException(""));
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleMethodArgumentNotValid_deberiaRetornar400ConErroresDeCampo() throws Exception {
        Method method = Object.class.getMethod("toString");
        MethodParameter param = new MethodParameter(method, -1);

        BindException bindErrors = new BindException(new HashMap<String, Object>(), "request");
        bindErrors.addError(new FieldError("request", "campo", "no puede ser nulo"));
        bindErrors.addError(new FieldError("request", "otro", "demasiado largo"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindErrors);

        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(
                ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsKey("campo");
        assertThat(body).containsKey("otro");
        assertThat(body.get("campo")).isEqualTo("no puede ser nulo");
    }

    @Test
    void handleMethodArgumentNotValid_sinErroresDeCampo_deberiaRetornarMapVacio() throws Exception {
        Method method = Object.class.getMethod("toString");
        MethodParameter param = new MethodParameter(method, -1);

        BindException bindErrors = new BindException(new HashMap<String, Object>(), "request");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindErrors);

        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(
                ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
}
