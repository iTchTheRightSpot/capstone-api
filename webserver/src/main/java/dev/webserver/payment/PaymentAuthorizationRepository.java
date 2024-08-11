package dev.webserver.payment;

import dev.webserver.exception.CustomServerError;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentAuthorizationRepository {
    private final JdbcClient client;

    public PaymentAuthorization save(final PaymentAuthorization authorization) {
        final String query = """
                INSERT INTO payment_authorization (authorization_id, authorization_code, bin, card_last_4_digits, exp_month, exp_year, channel, card_type, bank, country_code, brand, is_reusable, signature)
                    VALUE (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trim();

        final int update = client.sql(query)
                .param(1, authorization.authorizationId())
                .param(2, authorization.authorizationCode())
                .param(3, authorization.bin())
                .param(4, authorization.last4())
                .param(5, authorization.expirationMonth())
                .param(6, authorization.expirationYear())
                .param(7, authorization.channel())
                .param(8, authorization.cardType())
                .param(9, authorization.bank())
                .param(10, authorization.countryCode())
                .param(11, authorization.brand())
                .param(12, authorization.isReusable())
                .param(13, authorization.signature())
                .update();

        if (update < 1) throw new CustomServerError("error saving PaymentAuthorization");

        return authorization;
    }

    public List<PaymentAuthorization> findAll() {
        record PAuthMapper(Long authorization_id, String authorization_code, String bin, String card_last_4_digits, String exp_month, String exp_year, String channel, String card_type, String bank, String country_code, String brand, boolean is_reusable, String signature) {}

        return client.sql("SELECT * FROM payment_authorization")
                .query(PAuthMapper.class)
                .list()
                .stream()
                .map(m -> PaymentAuthorization.builder()
                        .authorizationId(m.authorization_id)
                        .authorizationCode(m.authorization_code)
                        .bin(m.bin)
                        .last4(m.card_last_4_digits)
                        .expirationMonth(m.exp_month)
                        .expirationYear(m.exp_year)
                        .channel(m.channel)
                        .cardType(m.card_type)
                        .bank(m.bank)
                        .countryCode(m.country_code)
                        .brand(m.brand)
                        .isReusable(m.is_reusable)
                        .signature(m.signature)
                        .build())
                .toList();
    }
}