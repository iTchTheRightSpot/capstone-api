package dev.webserver.auth.controller;

import dev.webserver.auth.dto.LoginDto;
import dev.webserver.auth.dto.RegisterDto;
import dev.webserver.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static dev.webserver.enumeration.RoleEnum.WORKER;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}worker/auth")
@RequiredArgsConstructor
public class WorkerAuthController {

    private final AuthService service;

    @ResponseStatus(CREATED)
    @PostMapping(path = "/register", consumes = "application/json")
    @PreAuthorize(value = "hasRole('ROLE_WORKER')")
    public void register(@Valid @RequestBody RegisterDto dto, HttpServletResponse res) {
        this.service.register(res, dto, WORKER);
    }

    @ResponseStatus(OK)
    @PostMapping(path = "/login", consumes = "application/json")
    public void login(
            @Valid @RequestBody LoginDto dto,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        this.service.login(WORKER, dto, req, res);
    }

}
