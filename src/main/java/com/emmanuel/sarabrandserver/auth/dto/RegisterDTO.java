package com.emmanuel.sarabrandserver.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.json.Json;
import javax.json.JsonObject;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RegisterDTO {

    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    @JsonProperty(value = "firstname")
    private String firstname;

    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    @JsonProperty(value = "lastname")
    private String lastname;

    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    @JsonProperty(value = "email")
    private String email;

    @JsonProperty(value = "username")
    private String username;

    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    @JsonProperty(value = "phone")
    private String phone;

    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    @JsonProperty(value = "password")
    private String password;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("firstname", getFirstname())
                .add("lastname", getLastname())
                .add("email", getEmail())
                .add("username", getUsername())
                .add("phone", getPhone())
                .add("password", getPassword())
                .build();
    }
}
