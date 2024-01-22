package com.sarabrandserver.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.StandardEnvironment;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
public class AwsConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AwsConfiguration.class.getName());
    private static final Region REGION;
    private static final AwsCredentialsProvider PROVIDER;

    static {
        var env = new StandardEnvironment();

        String region = env.getProperty("aws.region", "ca-central-1");

        REGION = Region.of(region);

        String profile = env.getProperty("spring.profiles.active", "");

        log.info("S3Config current active profile {}", profile);

        PROVIDER = profile.equals("test")
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

    @Lazy
    @Bean
    public static SecretsManagerClient secretsManager() {
        return SecretsManagerClient.builder()
                .region(REGION)
                .credentialsProvider(PROVIDER)
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
    }

//    @Bean
//    public static SesV2Client _sesV2Client() {
//        return SesV2Client.builder()
//                .region(REGION)
//                .credentialsProvider(PROVIDER)
//                .build();
//    }

}
