package dev.webserver.product.util;

import java.io.Serializable;

public record Variant (String sku, String inventory, String size) implements Serializable { }
