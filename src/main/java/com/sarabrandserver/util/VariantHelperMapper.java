package com.sarabrandserver.util;

import java.io.Serializable;

public record VariantHelperMapper (String sku, String inventory, String size) implements Serializable { }