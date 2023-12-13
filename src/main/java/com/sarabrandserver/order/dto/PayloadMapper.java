package com.sarabrandserver.order.dto;

import java.io.Serializable;

public record PayloadMapper(String name, String key, String colour, int qty) implements Serializable { }