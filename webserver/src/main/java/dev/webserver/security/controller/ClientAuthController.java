package dev.webserver.security.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static dev.webserver.enumeration.RoleEnum.CLIENT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}client/auth")
@RequiredArgsConstructor
public class ClientAuthController {

    private final AuthenticationService service;

    @ResponseStatus(CREATED)
    @PostMapping(path = "/register", consumes = APPLICATION_JSON_VALUE)
    public void register(@Valid @RequestBody RegisterDto dto, HttpServletResponse res) {
        this.service.register(res, dto, CLIENT);
    }

    @ResponseStatus(OK)
    @PostMapping(path = "/login", consumes = APPLICATION_JSON_VALUE)
    public void login(
            @Valid @RequestBody LoginDto dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        this.service.login(CLIENT, dto, request, response);
    }

}
