package dev.webserver.security.controller;

import dev.webserver.AbstractEnvironment;
import dev.webserver.enumeration.RoleEnum;
import dev.webserver.security.JwtService;
import dev.webserver.security.UserDetailz;
import dev.webserver.user.Role;
import dev.webserver.user.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}demo")
class DemoController extends AbstractEnvironment {

    private final JwtService service;

    protected DemoController(final Environment environment, final JwtService service) {
        super(environment);
        this.service = service;
    }

    private void cookie(final HttpServletResponse response, final String jwt) {
        final Cookie cookie = new Cookie(jsessionid, jwt);
        cookie.setMaxAge(cookiemaxage);
        cookie.setHttpOnly(true);
        cookie.setPath(cookiepath);
        cookie.setSecure(cookiesecure);
        // add cookie to response
        response.addCookie(cookie);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(consumes = "application/json")
    public void demo(final HttpServletResponse response) {
        final var user = User.builder()
                .userId(0L)
                .firstname("Capstone")
                .fullname("Capstone Demo")
                .email("demo@capstone.com")
                .imageKey(null)
                .build();

        final var roles = List.of(new Role(null, RoleEnum.USER, user.userId()), new Role(null, RoleEnum.DEMO, user.userId()));

        final var details = new UserDetailz(user, roles);

        cookie(response, service.generateJwt(authenticated(details, null, details.getAuthorities())));
    }

}
