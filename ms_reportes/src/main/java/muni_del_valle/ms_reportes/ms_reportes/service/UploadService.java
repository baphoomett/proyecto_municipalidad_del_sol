package muni_del_valle.ms_reportes.ms_reportes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UploadService {

    private final Path uploadDir;
    private final long maxSizeBytes;

    public UploadService(@Value("${file.upload-dir:uploads}") String uploadDir,
                         @Value("${file.max-size-bytes:52428800}") long maxSizeBytes) throws IOException {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxSizeBytes = maxSizeBytes;
        Files.createDirectories(this.uploadDir);
    }

    public String save(MultipartFile file) throws IOException {
        validate(file);
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = "";
        int i = original.lastIndexOf('.');
        if (i >= 0) ext = original.substring(i);
        String filename = UUID.randomUUID().toString() + ext;
        Path target = this.uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), target);
        // return accessible URL path
        return "/media/" + filename;
    }

    public List<String> saveAll(MultipartFile[] files) throws IOException {
        List<String> urls = new ArrayList<>();
        if (files == null) return urls;
        for (MultipartFile f : files) {
            urls.add(save(f));
        }
        return urls;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("File is empty");
        if (file.getSize() > maxSizeBytes) throw new IllegalArgumentException("File exceeds max allowed size");
        String ct = file.getContentType();
        if (ct == null) throw new IllegalArgumentException("Unknown content type");
        if (!ct.equalsIgnoreCase("image/png") && !ct.equalsIgnoreCase("image/jpeg") && !ct.equalsIgnoreCase("image/jpg")) {
            throw new IllegalArgumentException("Only PNG and JPEG images are allowed");
        }
    }
}
