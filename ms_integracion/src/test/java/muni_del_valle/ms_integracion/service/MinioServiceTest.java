package muni_del_valle.ms_integracion.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    private MinioService minioService;

    @BeforeEach
    void setUp() {
        minioService = new MinioService(minioClient);
        ReflectionTestUtils.setField(minioService, "bucket", "integracion");
    }

    @Test
    void upload_conDatosValidos_deberiaInvocarPutObjectEnMinioClient() throws Exception {
        ObjectWriteResponse response = mock(ObjectWriteResponse.class);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(response);

        InputStream data = new ByteArrayInputStream("contenido-de-prueba".getBytes());
        minioService.upload("evidencias/foto1.jpg", data, 19L, "image/jpeg");

        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void upload_siMinioClientFalla_deberiaPropagarLaExcepcion() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("MinIO no disponible"));

        InputStream data = new ByteArrayInputStream("x".getBytes());

        assertThatThrownBy(() -> minioService.upload("evidencias/foto2.jpg", data, 1L, "image/jpeg"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("MinIO no disponible");
    }

    @Test
    void generatePresignedUrl_conDatosValidos_deberiaRetornarLaUrlDeMinioClient() throws Exception {
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("https://minio.local/integracion/evidencias/foto1.jpg?sig=abc");

        String url = minioService.generatePresignedUrl("evidencias/foto1.jpg", 3600);

        assertThat(url).isEqualTo("https://minio.local/integracion/evidencias/foto1.jpg?sig=abc");
        verify(minioClient, times(1)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }
}
