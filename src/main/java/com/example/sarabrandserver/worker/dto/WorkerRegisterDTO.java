package com.example.sarabrandserver.worker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import javax.json.Json;
import javax.json.JsonObject;

public record WorkerRegisterDTO(
        @NotEmpty @NotNull
        @JsonProperty("name")
        String name,
        @NotEmpty @NotNull
        @JsonProperty("email")
        String email,

        @NotEmpty @NotNull
        @JsonProperty("username")
        String username,

        @NotEmpty @NotNull
        @JsonProperty("password")
        String password
) {
        public JsonObject toJson() {
                return Json.createObjectBuilder()
                        .add("name", name())
                        .add("email", email())
                        .add("username", username())
                        .add("password", password())
                        .build();
        }
}
