package muni_del_valle.ms_monitoreo.ms_alertas.controller;

import muni_del_valle.ms_monitoreo.ms_alertas.model.Alert;
import muni_del_valle.ms_monitoreo.ms_alertas.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AmqpTemplate amqpTemplate;

    @MockitoBean
    private AlertRepository alertRepository;

    @Test
    void receiveWebhook_conPayloadValido_guardaAlertaYPublicaEnRabbit() throws Exception {
        Alert saved = new Alert();
        saved.setReportId(1L);
        when(alertRepository.save(any())).thenReturn(saved);

        mvc.perform(post("/api/alerts/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportId\":1,\"geometry\":\"POINT(-73.0444 -36.8201)\",\"severity\":\"HIGH\",\"description\":\"Foco activo\"}"))
                .andExpect(status().isAccepted());

        verify(alertRepository).save(any(Alert.class));
        verify(amqpTemplate).convertAndSend(
                eq("alerts.exchange"), eq("alerts.new"), any(muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest.class));
    }

    @Test
    void receiveWebhook_sinReportId_retornaBadRequest() throws Exception {
        mvc.perform(post("/api/alerts/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"geometry\":\"POINT(-73.0 -36.8)\",\"severity\":\"HIGH\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void receiveWebhook_sinGeometry_retornaBadRequest() throws Exception {
        mvc.perform(post("/api/alerts/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportId\":1,\"severity\":\"HIGH\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAlerts_retornaListaDeAlertas() throws Exception {
        Alert a1 = new Alert();
        a1.setReportId(1L);
        a1.setSeverity("HIGH");
        Alert a2 = new Alert();
        a2.setReportId(2L);
        a2.setSeverity("LOW");
        when(alertRepository.findAll()).thenReturn(List.of(a1, a2));

        mvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].reportId").value(1))
                .andExpect(jsonPath("$[1].reportId").value(2));
    }

    @Test
    void getAlerts_listaVacia_retornaArrayVacio() throws Exception {
        when(alertRepository.findAll()).thenReturn(List.of());

        mvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteByReportId_eliminaAlertasDelReporte() throws Exception {
        doNothing().when(alertRepository).deleteByReportId(5L);

        mvc.perform(delete("/api/alerts/by-report/5"))
                .andExpect(status().isNoContent());

        verify(alertRepository).deleteByReportId(5L);
    }
}
