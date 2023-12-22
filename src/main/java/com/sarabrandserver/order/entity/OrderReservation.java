package com.sarabrandserver.order.entity;

import com.sarabrandserver.enumeration.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Table(name = "order_reservation")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class OrderReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id", nullable = false, unique = true)
    private Long reservationId;

    @Column(nullable = false, length = 36)
    private String cookie; // translates to ShoppingSession cookie table

    @Column(nullable = false, length = 36)
    private String sku; // translates to product_sku table

    @Column(nullable = false)
    private int qty;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(name = "expire_at", nullable = false)
    private Date expireAt;

    public OrderReservation(
            String cookie,
            String sku,
            int qty,
            ReservationStatus status,
            Date expireAt
    ) {
        this.cookie = cookie;
        this.sku = sku;
        this.qty = qty;
        this.status = status;
        this.expireAt = expireAt;
    }

}
