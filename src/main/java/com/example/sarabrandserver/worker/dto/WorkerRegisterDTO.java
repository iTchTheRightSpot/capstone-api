package com.example.sarabrandserver.worker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import javax.json.Json;
import javax.json.JsonObject;

public record WorkerRegisterDTO(
        @NotEmpty @NotNull
        @JsonProperty("name")
        String name,
        @NotEmpty @NotNull
        @Email(
                message = "Email is not valid",
                regexp = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$"
        )
        @JsonProperty("email")
        String email,

        @NotEmpty @NotNull
        @Size.List({
                @Size(min = 8, message = "username must be a min of 5 characters"),
                @Size(max = 30, message = "username must be a max of 30 characters")
        })
        @JsonProperty("username")
        String username,

        @NotEmpty @NotNull
        @Size.List({
                @Size(min = 10, message = "password must be a min of 10 characters"),
                @Size(max = 30, message = "password must be a min of 30 characters")
        })
        @JsonProperty("password")
        String password
) {
        public JsonObject convertToJSON() {
                return Json.createObjectBuilder()
                        .add("name", name())
                        .add("email", email())
                        .add("username", username())
                        .add("password", password())
                        .build();
        }
}
