package com.emmanuel.sarabrandserver.auth.dto;

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
    private String principal;

    @NotEmpty @NotNull
   private String password;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("principal", principal)
                .add("password", password)
                .build();
    }
}
