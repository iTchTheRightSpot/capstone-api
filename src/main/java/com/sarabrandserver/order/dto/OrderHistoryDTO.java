package com.sarabrandserver.order.dto;

import java.io.Serializable;

public record OrderHistoryDTO(long date, int total, String orderNumber, PayloadMapper[] obj) implements Serializable {}