package dev.webserver;

import dev.webserver.payment.Address;
import dev.webserver.payment.PaymentAuthorization;
import org.springframework.context.annotation.Bean;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;

class PrimaryKeyConfiguration {

    @Bean
    public BeforeConvertCallback<Address> addressBeforeConvertCallback() {
        return (address) -> address;
    }

    @Bean
    public BeforeConvertCallback<PaymentAuthorization> paymentAuthorizationBeforeConvertCallback() {
        return (authorization) -> authorization;
    }

}
