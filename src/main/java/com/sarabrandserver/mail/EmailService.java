package com.sarabrandserver.mail;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Service
public class EmailService {
    private final Environment _environment;

    public EmailService(Environment _environment) {
        this._environment = _environment;
    }

    public void _sendEmail(String _sender, String _recipient, String _message) {
        SesV2Client _sesV2Client = SesV2Client.builder()
                .region(Region.CA_CENTRAL_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }

    private void _emailBuilder() {}

}
