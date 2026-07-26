package com.purgz.egt_api.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    // Get our values from properties and add create the minio client
    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.user}")
    private String minioUser;

    @Value("${minio.password}")
    private String minioPassword;

    @Value("${minio.bucket}")
    private String minioBucket;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(minioUser, minioPassword)
                .build();
    }

    @Bean
    public String minioBucket() {
        return minioBucket;
    }
}
