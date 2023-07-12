package com.emmanuel.sarabrandserver.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public static S3Client s3Client() {
        return S3Client.builder()
                .region(Region.CA_CENTRAL_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
    }

}
