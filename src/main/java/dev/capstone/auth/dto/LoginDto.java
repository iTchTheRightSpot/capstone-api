<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/auth/dto/LoginDto.java
package dev.webserver.auth.dto;
========
package dev.capstone.auth.dto;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/auth/dto/LoginDto.java

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record LoginDto(
        @NotEmpty(message = "cannot be empty")
        @NotNull(message = "cannot be empty")
        String principal,

        @NotEmpty(message = "cannot be empty")
        @NotNull(message = "cannot be empty")
        String password
) implements Serializable { }