package dev.webserver.payment.projection;

import dev.webserver.cart.entity.ShoppingSession;
import dev.webserver.payment.entity.OrderReservation;
import dev.webserver.product.entity.ProductSku;

// Spring Data Projection
public interface PaymentDetailPojo {
    OrderReservation getReservation();
    ProductSku getSku();
    ShoppingSession getSession();
}
