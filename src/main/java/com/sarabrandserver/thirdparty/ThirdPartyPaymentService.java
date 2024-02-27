package com.sarabrandserver.thirdparty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarabrandserver.exception.CustomAwsException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

@Service
@RequiredArgsConstructor
public class ThirdPartyPaymentService {

    private static final Logger log = LoggerFactory.getLogger(ThirdPartyPaymentService.class);

    private final SecretsManagerClient managerClient;
    private final ObjectMapper objectMapper;
    private final Environment env;

    /**
     * returns PayStack pub and secret keys
     * */
    public final PaymentCredentialObj payStackCredentials() {
        String profile = this.env.getProperty("spring.profiles.active", "");
        String pubKey = this.env.getProperty("paystack.pub.key", "");
        String secretKey = this.env.getProperty("paystack.secret.key", "");

        if (profile.equals("test") || (!pubKey.isBlank() && !secretKey.isBlank())) {
            return new PaymentCredentialObj(pubKey, secretKey);
        }

        String awsSecretId = this.env
                .getProperty("aws.paystack.secret.id", "paystack-credentials");

        // TODO find out how to set credentials as an env variable not system variable
        return impl(awsSecretId, PaymentCredentialObj.class);
    }

    final <T> T impl(String secretId, Class<T> clazz) {
        var build = GetSecretValueRequest.builder().secretId(secretId).build();
        try {
            String str = this.managerClient.getSecretValue(build).secretString();
            return this.objectMapper.readValue(str, clazz);
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
    }

}
