package dev.webserver.payment;

// Spring data projection
public interface OrderReservationProjection {

    // OrderReservation
    Long getReservationId();
    Integer getReservationQty();

    // ProductSku
    String getProductSkuSku();

}
