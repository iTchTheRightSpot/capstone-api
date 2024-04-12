package dev.webserver.payment.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;

public record WebhookConstruct(JsonNode node, String validate) implements Serializable {}