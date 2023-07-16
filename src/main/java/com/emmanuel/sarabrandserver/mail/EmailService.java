package com.emmanuel.sarabrandserver.mail;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Service
public class EmailService {
    private final Environment environment;

    public EmailService(Environment environment) {
        this.environment = environment;
    }

    public void sendEmail(String sender, String recipient, String message) {
        SesV2Client sesV2Client = SesV2Client.builder()
                .region(Region.CA_CENTRAL_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();



    }

    private void _emailBuilder() {}

}
