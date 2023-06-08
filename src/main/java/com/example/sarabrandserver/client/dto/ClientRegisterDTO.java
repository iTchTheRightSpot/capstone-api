package com.example.sarabrandserver.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import javax.json.Json;
import javax.json.JsonObject;

public record ClientRegisterDTO(
        @NotNull @NotEmpty
        @JsonProperty(value = "firstname")
        String firstname,
        @NotNull @NotEmpty
        @JsonProperty(value = "lastname")
        String lastname,
        @NotNull @NotEmpty
        @JsonProperty(value = "email")
        String email,
        @NotNull @NotEmpty
        @JsonProperty(value = "phone_number")
        String phone_number,
        @NotNull @NotEmpty
        @JsonProperty(value = "password")
        String password
) {
    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("firstname", firstname())
                .add("lastname", lastname())
                .add("email", email())
                .add("phone_number", phone_number())
                .add("password", password())
                .build();
    }
}
