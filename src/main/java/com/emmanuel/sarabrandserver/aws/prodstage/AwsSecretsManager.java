package com.emmanuel.sarabrandserver.aws.prodstage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

@Configuration
@Profile(value = {"prod"})
public class AwsSecretsManager {

    private static final Logger log = LoggerFactory.getLogger(AwsSecretsManager.class.getName());

    @Value(value = "${aws.secret.manager.name}")
    private String SECRET_MANAGER_NAME;


    @Bean(name = "awsSecretString")
    public String getSecret() {
        return getSecret(this.SECRET_MANAGER_NAME);
    }

    private static String getSecret(String secretID) {
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.CA_CENTRAL_1)
                .credentialsProvider(InstanceProfileCredentialsProvider.builder().build())
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretID)
                .build();

        try {
            return client.getSecretValue(getSecretValueRequest).secretString();
        } catch (Exception e) {
            log.error("Error retrieving secrets from AWS Secret Manager " + e.getMessage());
            throw e;
        }
    }

}
