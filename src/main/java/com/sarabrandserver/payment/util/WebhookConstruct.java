package com.sarabrandserver.payment.util;

import com.fasterxml.jackson.databind.JsonNode;

public record WebhookConstruct(JsonNode node, String validate) {}