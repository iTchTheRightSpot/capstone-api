package dev.webserver.payment;

import dev.webserver.enumeration.PaymentStatus;
import dev.webserver.enumeration.SarreCurrency;
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
        @Column("payment_detail_id")
        Long paymentDetailId,
        @Column("full_name")
        String name,
        String email,
        String phone,
        @Column("reference_id")
        String referenceId, // equivalent to reference id
        SarreCurrency currency,
        BigDecimal amount,
        @Column("payment_provider")
        String paymentProvider,
        @Column("payment_status")
        PaymentStatus paymentStatus,
        @Column("paid_at")
        String paidAt,
        @Column("created_at")
        LocalDateTime createAt,
        @Column("client_id")
        Long clientId
) {
}
