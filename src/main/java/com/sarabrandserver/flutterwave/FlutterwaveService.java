package com.sarabrandserver.flutterwave;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.exception.CustomAwsException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class FlutterwaveService {

    private static final Logger log = LoggerFactory.getLogger(FlutterwaveService.class);

    private final SecretsManagerClient managerClient;
    private final ObjectMapper objectMapper;
    private final Environment env;

    /**
     * Returns Flutterwave credentials
     * */
    public final FlutterWaveCredentialObj flutterWaveCredentials() {
        String profile = this.env.getProperty("spring.profiles.active", "");

        if (profile.equals("test")) {
            String pubKey = this.env.getProperty("flutterwave.pub.key", "");
            String secretKey = this.env.getProperty("flutterwave.secret.key", "");
            String encryptionKey = this.env.getProperty("flutterwave.encryption.key", "");
            return new FlutterWaveCredentialObj(pubKey, secretKey, encryptionKey);
        }
        return impl.apply(this.managerClient, this.objectMapper);
    }

    private final BiFunction<SecretsManagerClient, ObjectMapper, FlutterWaveCredentialObj> impl = (manager, mapper) -> {
        var build = GetSecretValueRequest.builder().secretId("flutterwave-credentials").build();
        try {
            String str = manager.getSecretValue(build).secretString();
            return mapper.readValue(str, FlutterWaveCredentialObj.class);
        } catch (Exception e) {
            String err = "error can either be from retrieving aws secret or converting secret to custom object";
            log.error(err + e.getMessage());
            String ui = """
                    We apologize for the inconvenience. Our payment processing service is
                    currently experiencing technical difficulties. Please try again later.
                    If the issue persists, feel free to contact our support team for assistance.
                    Thank you for your understanding.
                    """;
            throw new CustomAwsException(ui);
        }
    };

}
