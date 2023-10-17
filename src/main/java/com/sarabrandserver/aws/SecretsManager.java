package com.sarabrandserver.aws;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SecretsManager {

//    @Bean(name = "awsSecretString")
//    public String getSecret() {
//        SecretsManagerClient client = SecretsManagerClient.builder()
//                .region(Region.CA_CENTRAL_1)
//                .credentialsProvider(InstanceProfileCredentialsProvider.builder().build())
//                .httpClient(UrlConnectionHttpClient.builder().build())
//                .build();
//
//        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
//                .secretId(this.environment.getProperty("aws.secret.manager.name"))
//                .build();
//
//        try {
//            String secretString = client.getSecretValue(getSecretValueRequest).secretString();
//            log.info("Successfully retrieved secrets");
//            return secretString;
//        } catch (Exception e) {
//            log.warning("Error retrieving secrets from AWS Secret Manager " + e.getMessage());
//            throw e;
//        }
//    }

}
