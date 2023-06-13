package com.example.sarabrandserver.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import javax.json.Json;
import javax.json.JsonObject;

public record LoginDTO (
        @NotEmpty @NotNull
        @JsonProperty("principal")
        String principal,
        @NotEmpty @NotNull
        @JsonProperty("password")
        String password
) {
    public JsonObject convertToJSON() {
        return Json.createObjectBuilder()
                .add("principal", principal)
                .add("password", password)
                .build();
    }
}
