package dev.webserver.payment;

// Spring Data Projection
public interface PaymentDetailProjection {

    Long getReservationId();
    Integer getReservationQty();
    Long getProductSkuId();
}
