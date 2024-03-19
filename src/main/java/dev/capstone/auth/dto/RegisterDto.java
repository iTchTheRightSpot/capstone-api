<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/auth/dto/RegisterDto.java
package dev.webserver.auth.dto;
========
package dev.capstone.auth.dto;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/auth/dto/RegisterDto.java

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
