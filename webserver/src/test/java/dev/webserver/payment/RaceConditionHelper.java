package dev.webserver.payment;

public record RaceConditionHelper() {

    public static RaceConditionCartProjection raceConditionCartPojo(
            long skuId,
            String sku,
            int inventory,
            String size,
            long cartId,
            int qty,
            long sessionId
    ) {
        return new RaceConditionCartProjection() {
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

    public static OrderReservationProjection reservationPojo (long reservationId, int qty, String sku) {
        return new OrderReservationProjection() {
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
