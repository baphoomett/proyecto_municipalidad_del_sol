package muni_del_valle.bff.controller;

import muni_del_valle.bff.security.JwtUtil;
import muni_del_valle.bff.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void createReport_conTokenValido_retornaRespuestaDelGateway() throws Exception {
        doReturn(ResponseEntity.status(201).body(Map.of("id", 1)))
                .when(reportService).createReport(any(), eq("mytoken"));

        mvc.perform(post("/bff/reports")
                        .header("Authorization", "Bearer mytoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Incendio\",\"latitude\":-36.8,\"longitude\":-73.0}"))
                .andExpect(status().isCreated());
    }

    @Test
    void getReports_conTokenValido_retornaOk() throws Exception {
        doReturn(ResponseEntity.ok(List.of())).when(reportService).getReports("mytoken");

        mvc.perform(get("/bff/reports").header("Authorization", "Bearer mytoken"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_conRolAdmin_retornaRespuestaDelGateway() throws Exception {
        when(jwtUtil.extractRole("mytoken")).thenReturn("ROLE_ADMIN");
        doReturn(ResponseEntity.ok().build())
                .when(reportService).updateStatus(eq(1L), eq("CONTROLADO"), eq("mytoken"));

        mvc.perform(patch("/bff/reports/1/status")
                        .header("Authorization", "Bearer mytoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONTROLADO\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_sinRolAdmin_retorna403() throws Exception {
        when(jwtUtil.extractRole("mytoken")).thenReturn("ROLE_USER");

        mvc.perform(patch("/bff/reports/1/status")
                        .header("Authorization", "Bearer mytoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONTROLADO\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void extinguishReport_conRolAdmin_retornaRespuestaDelGateway() throws Exception {
        when(jwtUtil.extractRole("mytoken")).thenReturn("ROLE_ADMIN");
        doReturn(ResponseEntity.noContent().build())
                .when(reportService).extinguishReport(eq(2L), eq("mytoken"));

        mvc.perform(delete("/bff/reports/2/extinguish")
                        .header("Authorization", "Bearer mytoken"))
                .andExpect(status().isNoContent());
    }

    @Test
    void extinguishReport_sinRolAdmin_retorna403() throws Exception {
        when(jwtUtil.extractRole("mytoken")).thenReturn("ROLE_USER");

        mvc.perform(delete("/bff/reports/2/extinguish")
                        .header("Authorization", "Bearer mytoken"))
                .andExpect(status().isForbidden());
    }
}
