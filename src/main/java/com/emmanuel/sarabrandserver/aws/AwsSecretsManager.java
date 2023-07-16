package com.emmanuel.sarabrandserver.aws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

@Configuration @Slf4j
@Profile(value = "prod")
public class AwsSecretsManager {
    private final Environment environment;

    public AwsSecretsManager(Environment environment) {
        this.environment = environment;
    }

    @Bean(name = "awsSecretString")
    public String getSecret() {
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.CA_CENTRAL_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(this.environment.getProperty("aws.secret.manager.name"))
                .build();

        try {
            return client.getSecretValue(getSecretValueRequest).secretString();
        } catch (Exception e) {
            log.error("Error retrieving secrets from AWS Secret Manager {}", e.getMessage());
            throw e;
        }
    }

}
