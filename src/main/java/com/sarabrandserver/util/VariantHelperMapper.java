package com.sarabrandserver.util;

import java.io.Serializable;

public record VariantHelperMapper (String sku, String size, String inventory) implements Serializable { }