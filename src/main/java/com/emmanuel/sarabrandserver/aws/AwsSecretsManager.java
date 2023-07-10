package com.emmanuel.sarabrandserver.aws;

import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.auth.service.AuthService;
import com.emmanuel.sarabrandserver.category.repository.CategoryRepository;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.clientz.repository.ClientRoleRepo;
import com.emmanuel.sarabrandserver.clientz.repository.ClientzRepository;
import com.emmanuel.sarabrandserver.collection.repository.CollectionRepository;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Configuration @Slf4j
@Profile(value = "prod")
public class AwsSecretsManager {
    private final Environment environment;

    public AwsSecretsManager(Environment environment) {
        this.environment = environment;
    }

    // Inject dummy account for display purpose
    @Bean
    public CommandLineRunner commandLineRunner(AuthService authService, ClientzRepository repository) {
        return args -> {
            if (repository.principalExists("admin@admin.com", "admin123456") == 0) {
                authService.workerRegister(new RegisterDTO(
                        "Test Account",
                        "Development",
                        "admin@admin.com",
                        "admin123456",
                        "0000000000",
                        "password123456"
                ));
            }
        };
    }

    @Bean(name = "awsSecretString")
    public String getSecret() {
        return retrieveSecrete(this.environment.getProperty("aws.secret.manager.name"));
    }

    public static String retrieveSecrete(String env) {
        // Create a Secrets Manager client
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.CA_CENTRAL_1) // Can be injected in application-prod.properties
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();


        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(env)
                .build();

        GetSecretValueResponse getSecretValueResponse;

        try {
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
            return getSecretValueResponse.secretString();
        } catch (Exception e) {
            log.error("Error retrieving secrets from AWS Secret Manager");
            throw e;
        }

    }

}
