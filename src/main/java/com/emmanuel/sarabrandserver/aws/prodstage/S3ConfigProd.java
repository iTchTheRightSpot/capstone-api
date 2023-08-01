package com.emmanuel.sarabrandserver.aws.prodstage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Profile(value = {"prod", "stage"})
public class S3ConfigProd {

    @Bean
    public static S3Client s3Client() {
        return S3Client.builder()
                .region(Region.CA_CENTRAL_1)
                .credentialsProvider(InstanceProfileCredentialsProvider.builder().build())
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
    }

    @Bean
    public static S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.CA_CENTRAL_1)
                .credentialsProvider(InstanceProfileCredentialsProvider.builder().build())
                .build();
    }

}
