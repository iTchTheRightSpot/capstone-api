package com.sarabrandserver.product.dto;

import java.io.Serializable;

public record VariantMapper(String sku, String size, String inventory) implements Serializable { }