package dev.webserver.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
class AwsConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AwsConfiguration.class.getName());
    private final Region REGION;
    private final AwsCredentialsProvider PROVIDER;

    public AwsConfiguration(Environment env) {
        String region = env.getProperty("aws.region", "ca-central-1");

        REGION = Region.of(region);

        String profile = env.getProperty("spring.profiles.active", "default");

        log.info("S3Config current active profile {}", profile);

        PROVIDER = profile.equals("aws")
                ? InstanceProfileCredentialsProvider.builder().build()
                : EnvironmentVariableCredentialsProvider.create();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(REGION)
                .credentialsProvider(PROVIDER)
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(REGION)
                .credentialsProvider(PROVIDER)
                .build();
    }

    @Bean
    public SecretsManagerClient secretsManager() {
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