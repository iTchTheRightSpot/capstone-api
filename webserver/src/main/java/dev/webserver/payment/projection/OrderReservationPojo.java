package dev.webserver.payment.projection;

// Spring data projection
public interface OrderReservationPojo {

    // OrderReservation
    Long getReservationId();
    Integer getReservationQty();

    // ProductSku
    String getProductSkuSku();

}
