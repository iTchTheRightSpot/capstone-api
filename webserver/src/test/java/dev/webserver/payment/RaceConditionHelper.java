package dev.webserver.payment;

import dev.webserver.payment.projection.OrderReservationPojo;
import dev.webserver.payment.projection.RaceConditionCartPojo;

public record RaceConditionHelper() {

    public static RaceConditionCartPojo raceConditionCartPojo(
            long skuId,
            String sku,
            int inventory,
            String size,
            long cartId,
            int qty,
            long sessionId
    ) {
        return new RaceConditionCartPojo() {
            @Override
            public Long getProductSkuId() {
                return skuId;
            }

            @Override
            public String getProductSkuSku() {
                return sku;
            }

            @Override
            public Integer getProductSkuInventory() {
                return inventory;
            }

            @Override
            public String getProductSkuSize() {
                return size;
            }

            @Override
            public Long getCartItemId() {
                return cartId;
            }

            @Override
            public Integer getCartItemQty() {
                return qty;
            }

            @Override
            public Long getShoppingSessionId() {
                return sessionId;
            }
        };
    }

    public static OrderReservationPojo reservationPojo (long reservationId, int qty, String sku) {
        return new OrderReservationPojo() {
            @Override
            public Long getReservationId() {
                return reservationId;
            }

            @Override
            public Integer getReservationQty() {
                return qty;
            }

            @Override
            public String getProductSkuSku() {
                return sku;
            }
        };
    }

}
