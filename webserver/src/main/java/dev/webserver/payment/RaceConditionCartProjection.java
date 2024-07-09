package dev.webserver.payment;

public interface RaceConditionCartProjection {

    // ProductSku
    Long getProductSkuId();
    String getProductSkuSku();
    Integer getProductSkuInventory();
    String getProductSkuSize();

    // CartItem
    Long getCartItemId();
    Integer getCartItemQty();

    // ShoppingSession
    Long getShoppingSessionId();

}
