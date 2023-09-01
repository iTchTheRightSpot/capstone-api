package com.emmanuel.sarabrandserver.aws.prodstage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.util.logging.Logger;

@Configuration
@Profile(value = {"prod"})
public class AwsSecretsManager {
    private static final Logger log = Logger.getLogger(AwsSecretsManager.class.getName());
    private final Environment environment;

    public AwsSecretsManager(Environment environment) {
        this.environment = environment;
    }

    @Bean(name = "awsSecretString")
    public String getSecret() {
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.CA_CENTRAL_1)
                .credentialsProvider(InstanceProfileCredentialsProvider.builder().build())
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(this.environment.getProperty("aws.secret.manager.name"))
                .build();

        try {
            String secretString = client.getSecretValue(getSecretValueRequest).secretString();
            log.info("Successfully retrieved secrets");
            return secretString;
        } catch (Exception e) {
            log.warning("Error retrieving secrets from AWS Secret Manager " + e.getMessage());
            throw e;
        }
    }

}
