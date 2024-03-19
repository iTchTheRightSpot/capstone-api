package dev.webserver.checkout;

import dev.webserver.cart.entity.CartItem;
import dev.webserver.cart.entity.ShoppingSession;
import dev.webserver.shipping.entity.ShipSetting;
import dev.webserver.tax.Tax;

import java.io.Serializable;
import java.util.List;

public record CustomObject(
        ShoppingSession session,
        List<CartItem> cartItems,
        ShipSetting ship,
        Tax tax
) implements Serializable { }