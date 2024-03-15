package dev.capstone.checkout;

import dev.capstone.cart.entity.CartItem;
import dev.capstone.cart.entity.ShoppingSession;
import dev.capstone.shipping.entity.ShipSetting;
import dev.capstone.tax.Tax;

import java.io.Serializable;
import java.util.List;

public record CustomObject(
        ShoppingSession session,
        List<CartItem> cartItems,
        ShipSetting ship,
        Tax tax
) implements Serializable { }