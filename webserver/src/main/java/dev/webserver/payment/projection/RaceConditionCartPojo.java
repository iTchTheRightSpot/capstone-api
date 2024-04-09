package dev.webserver.payment.projection;

public interface RaceConditionCartPojo {

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
