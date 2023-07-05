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

    @NotNull @NotEmpty
    @JsonProperty(value = "firstname")
    private String firstname;

    @NotNull @NotEmpty
    @JsonProperty(value = "lastname")
    private String lastname;

    @NotNull @NotEmpty
    @JsonProperty(value = "email")
    private String email;

    @NotNull @NotEmpty
    @JsonProperty(value = "username")
    private String username;

    @NotNull @NotEmpty
    @JsonProperty(value = "phone_number")
    private String phone_number;

    @NotNull @NotEmpty
    @JsonProperty(value = "password")
    private String password;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("firstname", getFirstname())
                .add("lastname", getLastname())
                .add("email", getEmail())
                .add("username", getUsername())
                .add("phone_number", getPhone_number())
                .add("password", getPassword())
                .build();
    }
}
