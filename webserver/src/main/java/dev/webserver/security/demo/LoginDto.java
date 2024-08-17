package dev.webserver.security.demo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record LoginDto(
        @NotNull(message = "principal cannot be null")
        @NotEmpty(message = "principal cannot be empty")
        String principal,
        @NotNull(message = "password cannot be null")
        @NotEmpty(message = "password cannot be null")
        String password
) implements Serializable {
}
