package com.emmanuel.sarabrandserver.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LoginDTO {

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be empty")
    private String principal;

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be empty")
    private String password;

}
