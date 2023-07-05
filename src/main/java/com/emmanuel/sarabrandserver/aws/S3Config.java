package com.emmanuel.sarabrandserver.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Profile(value = "prod")
public class S3Config {

    @Bean
    public static S3Client s3Client() {
        return S3Client.builder()
                .credentialsProvider(ProfileCredentialsProvider.create())
                .httpClientBuilder(ApacheHttpClient.builder())
                .region(Region.CA_CENTRAL_1)
                .build();
    }

}
