package dev.webserver.product.response;

import java.io.Serializable;

public record Variant (String sku, String inventory, String size) implements Serializable { }
