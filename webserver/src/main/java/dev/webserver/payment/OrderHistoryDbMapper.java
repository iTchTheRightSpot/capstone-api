package dev.webserver.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public record OrderHistoryDbMapper(String name, @JsonProperty("image_key") String imageKey, String colour) implements Serializable { }