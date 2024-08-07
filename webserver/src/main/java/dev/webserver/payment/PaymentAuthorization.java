package dev.webserver.payment;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "payment_authorization")
@Builder
record PaymentAuthorization(
        @Id
        @Column("authorization_id")
        Long authorizationId,
        @Column("authorization_code")
        String authorizationCode,
        String bin,
        @Column("card_last_4_digits")
        String last4,
        @Column("exp_month")
        String expirationMonth,
        @Column("exp_year")
        String expirationYear,
        String channel,
        @Column("card_type")
        String cardType,
        String bank,
        @Column("country_code")
        String countryCode,
        String brand,
        @Column("is_reusable")
        boolean isReusable,
        String signature
) {
}
