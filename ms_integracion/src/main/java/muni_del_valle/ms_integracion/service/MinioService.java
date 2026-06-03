package muni_del_valle.ms_integracion.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import io.minio.ObjectWriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.io.InputStream;

@Service
public class MinioService {
    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);

    private final MinioClient minioClient;

    @Value("${minio.bucket:integracion}")
    private String bucket;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Retry(name = "minio")
    @CircuitBreaker(name = "minio")
    public void upload(String objectName, InputStream data, long size, String contentType) throws Exception {
        logger.info("Uploading to MinIO: {}/{}", bucket, objectName);
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .stream(data, size, -1)
                .contentType(contentType)
                .build();
        ObjectWriteResponse resp = minioClient.putObject(args);
        logger.debug("MinIO putObject result: {}", resp);
    }

    @Retry(name = "minio")
    @CircuitBreaker(name = "minio")
    public String generatePresignedUrl(String objectName, int expirySeconds) throws Exception {
        logger.info("Generating presigned URL for: {}/{} ({}s)", bucket, objectName, expirySeconds);
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .object(objectName)
                .expiry(expirySeconds)
                .build();
        return minioClient.getPresignedObjectUrl(args);
    }
}
