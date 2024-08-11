package dev.webserver.security.mvc;

import dev.webserver.AbstractEnvironment;
import dev.webserver.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.Arrays;

@Controller
@RequestMapping(path = "${api.endpoint.baseurl}authentication")
class AuthenticationController extends AbstractEnvironment {

    private final JwtService service;

    protected AuthenticationController(final Environment environment, final JwtService service) {
        super(environment);
        this.service = service;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/authenticate")
    public String authenticate(final ModelMap map, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            final var optional = Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(jsessionid) && service.jwtNoneExpired(cookie.getValue()))
                    .findFirst();

            if (optional.isPresent()) {
                response.addCookie(optional.get());
                response.sendRedirect(uiRedirect);
                return null;
            }
        }
        map.addAttribute("frontend", uiRedirect);
        return "authentication";
    }
}
