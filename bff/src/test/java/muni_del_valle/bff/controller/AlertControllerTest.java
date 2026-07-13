package muni_del_valle.bff.controller;

import muni_del_valle.bff.service.AlertService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AlertService alertService;

    @Test
    void getAlerts_conTokenValido_retornaOk() throws Exception {
        doReturn(ResponseEntity.ok(List.of())).when(alertService).getAlerts("mytoken");

        mvc.perform(get("/bff/alerts").header("Authorization", "Bearer mytoken"))
                .andExpect(status().isOk());
    }

    @Test
    void getAlerts_cuandoServicioRetorna503_propagaStatus() throws Exception {
        doReturn(ResponseEntity.status(503).build()).when(alertService).getAlerts("tok");

        mvc.perform(get("/bff/alerts").header("Authorization", "Bearer tok"))
                .andExpect(status().isServiceUnavailable());
    }
}
