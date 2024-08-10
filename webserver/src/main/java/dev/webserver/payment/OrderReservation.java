package dev.webserver.payment;

import dev.webserver.enumeration.ReservationStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "order_reservation")
@Builder
public record OrderReservation(
        @Id
        @Column("reservation_id")
        Long reservationId,
        @NotNull(message = "order_reservation reference cannot be null")
        @NotEmpty(message = "order_reservation reference cannot be empty")
        @Size.List({
                @Size(min = 36, message = "order_reservation reference max length of 36"),
                @Size(max = 36, message = "order_reservation reference max length of 36")
        })
        String reference,
        int qty,
        ReservationStatus status,
        @NotNull(message = "order_reservation expire_at cannot be null")
        @Column("expire_at")
        LocalDateTime expireAt,
        @NotNull(message = "order_reservation sku_id cannot be null")
        @Column("sku_id")
        Long skuId,
        @NotNull(message = "order_reservation session_id cannot be null")
        @Column("session_id")
        Long sessionId
) {
}
