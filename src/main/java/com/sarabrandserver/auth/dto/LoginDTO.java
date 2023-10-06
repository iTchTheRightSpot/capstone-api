package com.sarabrandserver.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record LoginDTO(
        @NotEmpty(message = "cannot be empty")
        @NotNull(message = "cannot be empty")
        String principal,

        @NotEmpty(message = "cannot be empty")
        @NotNull(message = "cannot be empty")
        String password
) implements Serializable { }