package dev.webserver.payment;

import dev.webserver.cart.ShoppingSession;
import dev.webserver.shipping.ShipSetting;
import dev.webserver.tax.Tax;

import java.io.Serializable;
import java.util.List;

public record CustomCheckoutObject(
        ShoppingSession session,
        List<RaceConditionCartProjection> cartItems,
        ShipSetting ship,
        Tax tax
) implements Serializable { }