package dev.webserver.product;

import java.io.Serializable;

public record Variant (String sku, String inventory, String size) implements Serializable { }
