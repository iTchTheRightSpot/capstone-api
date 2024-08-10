package dev.webserver.payment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
        @NotNull(message = "payment_authorization authorization_code cannot be null")
        @NotEmpty(message = "payment_authorization authorization_code cannot be empty")
        @Size.List({@Size(max = 50, message = "payment_authorization authorization_code max length of 50")})
        @Column("authorization_code")
        String authorizationCode,
        @NotNull(message = "payment_authorization bin cannot be null")
        @NotEmpty(message = "payment_authorization bin cannot be empty")
        @Size.List({@Size(max = 50, message = "payment_authorization bin max length of 50")})
        String bin,
        @NotNull(message = "payment_authorization card_last_4_digits cannot be null")
        @NotEmpty(message = "payment_authorization card_last_4_digits cannot be empty")
        @Size.List({@Size(max = 5, message = "payment_authorization card_last_4_digits max length of 5")})
        @Column("card_last_4_digits")
        String last4,
        @NotNull(message = "payment_authorization exp_month cannot be null")
        @NotEmpty(message = "payment_authorization exp_month cannot be empty")
        @Size.List({@Size(max = 2, message = "payment_authorization exp_month max length of 2")})
        @Column("exp_month")
        String expirationMonth,
        @Column("exp_year")
        @NotNull(message = "payment_authorization exp_year cannot be null")
        @NotEmpty(message = "payment_authorization exp_year cannot be empty")
        @Size.List({@Size(max = 6, message = "payment_authorization exp_year max length of 6")})
        String expirationYear,
        @NotNull(message = "payment_authorization channel cannot be null")
        @NotEmpty(message = "payment_authorization channel cannot be empty")
        @Size.List({@Size(max = 10, message = "payment_authorization channel max length of 10")})
        String channel,
        @NotNull(message = "payment_authorization card_type cannot be null")
        @NotEmpty(message = "payment_authorization card_type cannot be empty")
        @Size.List({@Size(max = 20, message = "payment_authorization card_type max length of 20")})
        @Column("card_type")
        String cardType,
        @NotNull(message = "payment_authorization bank cannot be null")
        @NotEmpty(message = "payment_authorization bank cannot be empty")
        @Size.List({@Size(max = 100, message = "payment_authorization bank max length of 100")})
        String bank,
        @Column("country_code")
        @NotNull(message = "payment_authorization country_code cannot be null")
        @NotEmpty(message = "payment_authorization country_code cannot be empty")
        @Size.List({@Size(max = 10, message = "payment_authorization country_code max length of 10")})
        String countryCode,
        @NotNull(message = "payment_authorization brand cannot be null")
        @NotEmpty(message = "payment_authorization brand cannot be empty")
        @Size.List({@Size(max = 20, message = "payment_authorization brand max length of 20")})
        String brand,
        @Column("is_reusable")
        boolean isReusable,
        @NotNull(message = "payment_authorization signature cannot be null")
        @NotEmpty(message = "payment_authorization signature cannot be empty")
        @Size.List({@Size(max = 50, message = "payment_authorization signature max length of 50")})
        String signature
) {
}
