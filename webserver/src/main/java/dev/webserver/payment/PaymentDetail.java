package dev.webserver.payment;

import dev.webserver.enumeration.PaymentStatus;
import dev.webserver.enumeration.SarreCurrency;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "payment_detail")
@Builder
public record PaymentDetail(
        @Id
        @Column("payment_id")
        Long paymentId,
        @NotNull(message = "payment_detail fullname cannot be null")
        @NotEmpty(message = "payment_detail fullname cannot be empty")
        @Size.List({@Size(max = 255, message = "payment_detail fullname max length of 255")})
        String fullname,
        @NotNull(message = "payment_detail email cannot be null")
        @NotEmpty(message = "payment_detail email cannot be empty")
        @Size.List({@Size(max = 255, message = "payment_detail email max length of 255")})
        String email,
        @NotNull(message = "payment_detail phone cannot be null")
        @NotEmpty(message = "payment_detail phone cannot be empty")
        @Size.List({@Size(max = 20, message = "payment_detail phone max length of 20")})
        String phone,
        @NotNull(message = "payment_detail reference_id cannot be null")
        @NotEmpty(message = "payment_detail reference_id cannot be empty")
        @Size.List({@Size(max = 255, message = "payment_detail reference_id max length of 255")})
        @Column("reference_id")
        String referenceId, // equivalent to reference id
        SarreCurrency currency,
        BigDecimal amount,
        @NotNull(message = "payment_detail payment_provider cannot be null")
        @NotEmpty(message = "payment_detail payment_provider cannot be empty")
        @Size.List({@Size(max = 30, message = "payment_detail payment_provider max length of 30")})
        @Column("payment_provider")
        String paymentProvider,
        @Column("payment_status")
        PaymentStatus paymentStatus,
        @NotNull(message = "payment_detail paid_at cannot be null")
        @NotEmpty(message = "payment_detail paid_at cannot be empty")
        @Size.List({@Size(max = 255, message = "payment_detail paid_at max length of 255")})
        @Column("paid_at")
        String paidAt,
        @NotNull(message = "payment_detail created_at cannot be null")
        @Column("created_at")
        LocalDateTime createAt,
        @NotNull(message = "payment_detail user_id cannot be null")
        @Column("user_id")
        Long userId
) {
}
