package dev.webserver.payment.projection;

import dev.webserver.cart.entity.ShoppingSession;
import dev.webserver.enumeration.ReservationStatus;
import dev.webserver.product.entity.ProductSku;

import java.util.Date;

// Spring data projection
public interface OrderReservationPojo {

    Long getReservationId();
    String getReference();
    Integer getQty();
    ReservationStatus getStatus();
    Date getExpireAt();
    ProductSku getProductSku();
    ShoppingSession getShoppingSession();

}
