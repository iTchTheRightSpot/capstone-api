package dev.webserver.external.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ThirdPartyPaymentService {

    private final Environment environment;

    // TODO
    public final PaymentCredentialObj payStackCredentials() {
        String pubKey = environment.getProperty("paystack.pub.key", "");
        String secretKey = environment.getProperty("paystack.secret.key", "");
        return new PaymentCredentialObj(pubKey, secretKey);
    }

}
