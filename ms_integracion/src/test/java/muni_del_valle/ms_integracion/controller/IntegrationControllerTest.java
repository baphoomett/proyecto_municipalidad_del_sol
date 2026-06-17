package muni_del_valle.ms_integracion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni_del_valle.ms_integracion.service.MinioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IntegrationController.class)
class IntegrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private MinioService minioService;

    @Test
    void presigned_conObjectNameValido_deberiaRetornar200ConLaUrl() throws Exception {
        when(minioService.generatePresignedUrl(eq("evidencias/foto1.jpg"), eq(3600)))
                .thenReturn("https://minio.local/presigned-url");

        mockMvc.perform(post("/api/minio/presigned")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "objectName", "evidencias/foto1.jpg",
                                "expirySeconds", 3600))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://minio.local/presigned-url"));
    }

    @Test
    void presigned_sinObjectName_deberiaRetornar400() throws Exception {
        mockMvc.perform(post("/api/minio/presigned")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("expirySeconds", 3600))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("objectName is required"));
    }

    @Test
    void presigned_siMinioServiceFalla_deberiaRetornar500() throws Exception {
        when(minioService.generatePresignedUrl(eq("evidencias/foto1.jpg"), eq(3600)))
                .thenThrow(new RuntimeException("MinIO no disponible"));

        mockMvc.perform(post("/api/minio/presigned")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "objectName", "evidencias/foto1.jpg",
                                "expirySeconds", 3600))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("MinIO no disponible"));
    }

    @Test
    void health_deberiaRetornar200ConStatusUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
