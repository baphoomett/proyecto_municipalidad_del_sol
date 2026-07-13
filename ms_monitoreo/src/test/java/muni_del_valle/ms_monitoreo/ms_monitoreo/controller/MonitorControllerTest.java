package muni_del_valle.ms_monitoreo.ms_monitoreo.controller;

import muni_del_valle.ms_monitoreo.ms_monitoreo.model.Focus;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.FocusStatus;
import muni_del_valle.ms_monitoreo.ms_monitoreo.security.JwtUtil;
import muni_del_valle.ms_monitoreo.ms_monitoreo.service.FocusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MonitorController.class)
@WithMockUser
class MonitorControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FocusService focusService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private Focus buildFocus(Long id, FocusStatus status) {
        Focus f = new Focus();
        f.setId(id);
        f.setStatus(status);
        f.setDescription("Foco de prueba");
        return f;
    }

    @Test
    void createFocus_conDatosValidos_retorna201() throws Exception {
        Focus focus = buildFocus(1L, FocusStatus.NEW);
        when(focusService.createFocus(any())).thenReturn(focus);

        mvc.perform(post("/api/monitor/focus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportId\":1,\"geometry\":\"POINT(-73.0 -36.8)\",\"description\":\"test\",\"severity\":\"HIGH\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createFocus_sinCamposRequeridos_retornaBadRequest() throws Exception {
        mvc.perform(post("/api/monitor/focus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"sin reportId ni geometry\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFocus_cuandoServicioLanzaIllegalArgumentException_retornaBadRequest() throws Exception {
        when(focusService.createFocus(any())).thenThrow(new IllegalArgumentException("Invalid geometry WKT"));

        mvc.perform(post("/api/monitor/focus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportId\":1,\"geometry\":\"INVALIDO\",\"description\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid geometry WKT"));
    }

    @Test
    void listFocus_sinFiltro_retornaPageConFocos() throws Exception {
        PageImpl<Focus> page = new PageImpl<>(List.of(buildFocus(1L, FocusStatus.NEW)));
        when(focusService.listFocus(eq(Optional.empty()), any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/api/monitor/focus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void listFocus_conFiltroDeStatus_retornaFocosFiltrados() throws Exception {
        PageImpl<Focus> page = new PageImpl<>(List.of(buildFocus(2L, FocusStatus.VERIFIED)));
        when(focusService.listFocus(eq(Optional.of("VERIFIED")), any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/api/monitor/focus").param("status", "VERIFIED"))
                .andExpect(status().isOk());
    }

    @Test
    void getFocus_existente_retorna200ConFoco() throws Exception {
        Focus focus = buildFocus(5L, FocusStatus.NEW);
        when(focusService.getById(5L)).thenReturn(Optional.of(focus));

        mvc.perform(get("/api/monitor/focus/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getFocus_noExistente_retorna404() throws Exception {
        when(focusService.getById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/monitor/focus/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void stream_retornaOk() throws Exception {
        when(focusService.subscribe()).thenReturn(new SseEmitter());

        mvc.perform(get("/api/monitor/stream"))
                .andExpect(status().isOk());
    }
}
