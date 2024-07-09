package dev.webserver.security.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record RegisterDto(
        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String firstname,

        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String lastname,

        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String email,

        String username,

        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String phone,

        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String password
) implements Serializable { }
