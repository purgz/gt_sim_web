package com.purgz.egt_api.service;


import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final String minioBucket;

    @PostConstruct
    public void init(){

        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioBucket).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioBucket).build());
            }
        } catch (Exception e){
            throw new RuntimeException("Could not initialise minio bucket");
        }
    }

    public void save(String key, String json) {
        try {
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioBucket)
                    .object(key)
                    .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                    .contentType("application/json")
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save to MinIO: " + key, e);
        }
    }


    public String fetch(String key) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(minioBucket).object(key).build())) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch from MinIO: " + key, e);
        }
    }
}
