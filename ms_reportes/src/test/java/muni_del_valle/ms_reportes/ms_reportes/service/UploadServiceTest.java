package muni_del_valle.ms_reportes.ms_reportes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class UploadServiceTest {

    @TempDir
    Path tempDir;

    private UploadService uploadService;

    @BeforeEach
    void setUp() throws IOException {
        uploadService = new UploadService(tempDir.toString(), 52_428_800L);
    }

    @Test
    void save_archivoPngValido_retornaUrlAccesible() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "imagen.png", "image/png", new byte[1024]);

        String url = uploadService.save(file);

        assertThat(url).startsWith("/media/").endsWith(".png");
    }

    @Test
    void save_archivoJpegValido_retornaUrlAccesible() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "foto.jpeg", "image/jpeg", new byte[2048]);

        String url = uploadService.save(file);

        assertThat(url).startsWith("/media/").endsWith(".jpeg");
    }

    @Test
    void save_archivoJpgValido_retornaUrlAccesible() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "foto.jpg", "image/jpg", new byte[512]);

        String url = uploadService.save(file);

        assertThat(url).startsWith("/media/").endsWith(".jpg");
    }

    @Test
    void save_archivoSuperaTamanioMaximo_lanzaIllegalArgumentException() {
        byte[] largeData = new byte[(int) 52_428_800 + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file", "grande.png", "image/png", largeData);

        assertThatThrownBy(() -> uploadService.save(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("max allowed size");
    }

    @Test
    void save_tipoDeContenidoInvalido_lanzaIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[512]);

        assertThatThrownBy(() -> uploadService.save(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PNG and JPEG");
    }

    @Test
    void save_archivoVacio_lanzaIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "vacio.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> uploadService.save(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void save_sinContentType_lanzaIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sin_tipo.png", null, new byte[512]);

        assertThatThrownBy(() -> uploadService.save(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("content type");
    }

    @Test
    void saveAll_variosArchivosValidos_retornaListaDeUrls() throws IOException {
        MockMultipartFile f1 = new MockMultipartFile("f1", "a.png", "image/png", new byte[100]);
        MockMultipartFile f2 = new MockMultipartFile("f2", "b.jpeg", "image/jpeg", new byte[200]);

        List<String> urls = uploadService.saveAll(new MockMultipartFile[]{f1, f2});

        assertThat(urls).hasSize(2);
        assertThat(urls.get(0)).startsWith("/media/");
        assertThat(urls.get(1)).startsWith("/media/");
    }

    @Test
    void saveAll_arregloNull_retornaListaVacia() throws IOException {
        List<String> urls = uploadService.saveAll(null);

        assertThat(urls).isEmpty();
    }

    @Test
    void saveAll_arregloVacio_retornaListaVacia() throws IOException {
        List<String> urls = uploadService.saveAll(new MockMultipartFile[0]);

        assertThat(urls).isEmpty();
    }
}
