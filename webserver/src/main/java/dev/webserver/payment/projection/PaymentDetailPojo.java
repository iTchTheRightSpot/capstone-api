package dev.webserver.payment.projection;

// Spring Data Projection
public interface PaymentDetailPojo {

    Long getReservationId();
    Integer getReservationQty();
    Long getProductSkuId();
}
