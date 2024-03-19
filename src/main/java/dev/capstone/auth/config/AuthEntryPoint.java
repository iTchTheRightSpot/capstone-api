<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/auth/config/AuthEntryPoint.java
package dev.webserver.auth.config;
========
package dev.capstone.auth.config;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/auth/config/AuthEntryPoint.java

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * A global exception handler that allows {@code ControllerAdvices} class to take effect
 * */
@Component(value = "authEntryPoint")
public class AuthEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public AuthEntryPoint(
            @Qualifier(value = "handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        this.resolver = resolver;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        this.resolver.resolveException(request, response, null, authException);
    }

}
