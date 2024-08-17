package dev.webserver.security.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.webserver.enumeration.RoleEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ActiveUser(
        @NotNull(message = "user_id cannot be null")
        @NotEmpty(message = "user_id cannot be empty")
        @JsonProperty("user_id")
        long userId,
        @NotNull(message = "name cannot be null")
        @NotEmpty(message = "name cannot be empty")
        String name,
        String email,
        @JsonProperty("image_key")
        String imageKey,
        List<RoleEnum> roles
) implements Serializable {
}
