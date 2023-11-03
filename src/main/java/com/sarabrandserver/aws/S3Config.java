package com.sarabrandserver.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    private final static AwsCredentialsProvider PROVIDER;
    private final static Region REGION;

    static {
        String profile = new StandardEnvironment()
                .getProperty("spring.profiles.active", "");

        REGION = Region.CA_CENTRAL_1;

        PROVIDER = profile.equals("dev") || profile.equals("test")
                ? EnvironmentVariableCredentialsProvider.create()
                : InstanceProfileCredentialsProvider.builder().build();
    }

    @Bean
    public static S3Client s3Client() {
        return S3Client.builder()
                .region(REGION)
                .credentialsProvider(PROVIDER)
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
    }

    @Bean
    public static S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(REGION)
                .credentialsProvider(PROVIDER)
                .build();
    }

}
