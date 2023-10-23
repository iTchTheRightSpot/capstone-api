package com.sarabrandserver.cart.response;

public record CartResponse(String sessionId, String url, String product_name, String sku, int qty) { }