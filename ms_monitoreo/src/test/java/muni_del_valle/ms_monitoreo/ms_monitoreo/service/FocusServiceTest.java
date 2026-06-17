package muni_del_valle.ms_monitoreo.ms_monitoreo.service;

import muni_del_valle.ms_monitoreo.ms_monitoreo.dto.CreateFocusRequest;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.Focus;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.FocusStatus;
import muni_del_valle.ms_monitoreo.ms_monitoreo.repository.FocusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FocusServiceTest {

    @Mock
    private FocusRepository focusRepository;

    @Mock
    private AmqpTemplate amqpTemplate;

    private FocusService focusService;

    @BeforeEach
    void setUp() {
        focusService = new FocusService(focusRepository, amqpTemplate);
    }

    private CreateFocusRequest buildRequest() {
        CreateFocusRequest req = new CreateFocusRequest();
        req.setReportId(10L);
        req.setGeometry("POINT(-73.0444 -36.8201)");
        req.setDescription("Foco activo");
        req.setSeverity("HIGH");
        return req;
    }

    @Test
    void createFocus_conDatosValidos_deberiaGuardarYPublicarEnRabbitMQ() {
        CreateFocusRequest req = buildRequest();
        when(focusRepository.save(any(Focus.class))).thenAnswer(inv -> {
            Focus f = inv.getArgument(0);
            f.setId(1L);
            return f;
        });

        Focus result = focusService.createFocus(req);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getReportId()).isEqualTo(10L);
        verify(focusRepository, times(1)).save(any(Focus.class));
        verify(amqpTemplate, times(1)).convertAndSend(eq("alerts.exchange"), eq("alerts.new"), any(Object.class));
    }

    @Test
    void createFocus_siRabbitMQFalla_noDeberiaPropagarLaExcepcion() {
        CreateFocusRequest req = buildRequest();
        when(focusRepository.save(any(Focus.class))).thenAnswer(inv -> {
            Focus f = inv.getArgument(0);
            f.setId(2L);
            return f;
        });
        doThrow(new RuntimeException("RabbitMQ no disponible"))
                .when(amqpTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        Focus result = focusService.createFocus(req);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    void listFocus_sinFiltro_deberiaRetornarTodos() {
        Pageable pageable = mock(Pageable.class);
        Page<Focus> page = new PageImpl<>(List.of(new Focus(), new Focus()));
        when(focusRepository.findAll(pageable)).thenReturn(page);

        Page<Focus> result = focusService.listFocus(Optional.empty(), pageable);

        assertThat(result.getContent()).hasSize(2);
        verify(focusRepository, never()).findByStatus(any(), any());
    }

    @Test
    void listFocus_conFiltroValido_deberiaFiltrarPorStatus() {
        Pageable pageable = mock(Pageable.class);
        Page<Focus> page = new PageImpl<>(List.of(new Focus()));
        when(focusRepository.findByStatus(FocusStatus.VERIFIED, pageable)).thenReturn(page);

        Page<Focus> result = focusService.listFocus(Optional.of("VERIFIED"), pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(focusRepository, times(1)).findByStatus(FocusStatus.VERIFIED, pageable);
    }

    @Test
    void listFocus_conFiltroInvalido_deberiaRetornarTodosSinFallar() {
        Pageable pageable = mock(Pageable.class);
        Page<Focus> page = new PageImpl<>(List.of(new Focus()));
        when(focusRepository.findAll(pageable)).thenReturn(page);

        Page<Focus> result = focusService.listFocus(Optional.of("ESTADO_QUE_NO_EXISTE"), pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(focusRepository, times(1)).findAll(pageable);
    }

    @Test
    void getById_focoExistente_deberiaRetornarloEnvueltoEnOptional() {
        Focus focus = new Focus();
        focus.setId(5L);
        when(focusRepository.findById(5L)).thenReturn(Optional.of(focus));

        Optional<Focus> result = focusService.getById(5L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(5L);
    }

    @Test
    void getById_focoInexistente_deberiaRetornarOptionalVacio() {
        when(focusRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Focus> result = focusService.getById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void subscribe_deberiaRetornarUnSseEmitterNoNulo() {
        SseEmitter emitter = focusService.subscribe();

        assertThat(emitter).isNotNull();
    }
}
