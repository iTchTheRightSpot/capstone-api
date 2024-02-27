package com.sarabrandserver.payment.entity;

import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.enumeration.ReservationStatus;
import com.sarabrandserver.product.entity.ProductSku;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Table(name = "order_reservation")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id", nullable = false, unique = true)
    private Long reservationId;

    @Column(nullable = false, length = 36)
    private String reference;

    @Column(nullable = false)
    private int qty;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(name = "expire_at", nullable = false)
    private Date expireAt;

    @ManyToOne
    @JoinColumn(name = "sku_id", referencedColumnName = "sku_id", nullable = false)
    private ProductSku productSku;

    @ManyToOne
    @JoinColumn(name = "session_id", referencedColumnName = "session_id")
    private ShoppingSession shoppingSession;

    public OrderReservation(
            String reference,
            int qty,
            ReservationStatus status,
            Date expireAt,
            ProductSku sku,
            ShoppingSession session
    ) {
        this.reference = reference;
        this.qty = qty;
        this.status = status;
        this.expireAt = expireAt;
        this.productSku = sku;
        this.shoppingSession = session;
    }

}
