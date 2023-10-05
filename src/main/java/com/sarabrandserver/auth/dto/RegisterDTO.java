package com.sarabrandserver.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RegisterDTO(
        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        @JsonProperty(value = "firstname")
        String firstname,

        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        @JsonProperty(value = "lastname")
        String lastname,

        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        @JsonProperty(value = "email")
        String email,

        @JsonProperty(value = "username")
        String username,

        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        @JsonProperty(value = "phone")
        String phone,

        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        @JsonProperty(value = "password")
        String password
) { }
