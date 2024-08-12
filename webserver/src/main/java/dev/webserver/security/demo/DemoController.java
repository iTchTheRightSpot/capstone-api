package dev.webserver.security.demo;

import dev.webserver.AbstractEnvironment;
import dev.webserver.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.function.BiConsumer;

import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}demo")
class DemoController extends AbstractEnvironment {

    private final AuthenticationManager manager;
    private final JwtService jwtService;

    DemoController(final AuthenticationManager manager, final Environment environment, final JwtService jwtService) {
        super(environment);
        this.manager = manager;
        this.jwtService = jwtService;
    }

    private final BiConsumer<String, HttpServletResponse> cookieHandler = (jwt, response) -> {
        final Cookie cookie = new Cookie(jsessionid, jwt);
        cookie.setPath(cookiepath);
        cookie.setMaxAge(cookiemaxage);
        cookie.setHttpOnly(true);
        cookie.setSecure(!activeprofile.endsWith("test"));
        response.addCookie(cookie);
    };

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(consumes = "application/json")
    public void login(@Valid @RequestBody final LoginDto dto, final HttpServletResponse response) {
        final Authentication authenticate = manager.authenticate(unauthenticated(dto.principal().trim(), dto.password().trim()));
        cookieHandler.accept(jwtService.generateJwt(authenticate), response);
    }

}
