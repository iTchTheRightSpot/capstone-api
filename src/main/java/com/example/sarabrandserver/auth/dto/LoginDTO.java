package com.example.sarabrandserver.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.json.Json;
import javax.json.JsonObject;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LoginDTO {

    @NotEmpty @NotNull
    @JsonProperty("principal")
    private String principal;
    @NotEmpty @NotNull
    @JsonProperty("password")
   private String password;

    public JsonObject convertToJSON() {
        return Json.createObjectBuilder()
                .add("principal", principal)
                .add("password", password)
                .build();
    }
}
