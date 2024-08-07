package dev.webserver.payment;

import dev.webserver.enumeration.ReservationStatus;
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
        String reference,
        int qty,
        ReservationStatus status,
        @Column("expire_at")
        LocalDateTime expireAt,
        @Column("sku_id")
        Long skuId,
        @Column("session_id")
        Long sessionId
) {
}
