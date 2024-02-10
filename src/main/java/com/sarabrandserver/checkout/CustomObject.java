package com.sarabrandserver.checkout;

import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.shipping.entity.ShipSetting;
import com.sarabrandserver.tax.Tax;

import java.util.List;

public record CustomObject(
        ShoppingSession session,
        List<CartItem> cartItems,
        ShipSetting ship,
        Tax tax
) { }