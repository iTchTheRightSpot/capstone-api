package dev.webserver.checkout;

import dev.webserver.cart.ShoppingSession;
import dev.webserver.payment.RaceConditionCartProjection;
import dev.webserver.shipping.ShipSetting;
import dev.webserver.tax.Tax;

import java.io.Serializable;
import java.util.List;

public record CustomObject(
        ShoppingSession session,
        List<RaceConditionCartProjection> cartItems,
        ShipSetting ship,
        Tax tax
) implements Serializable { }