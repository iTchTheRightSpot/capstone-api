package dev.webserver.security.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static dev.webserver.enumeration.RoleEnum.WORKER;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}worker/auth")
@RequiredArgsConstructor
public class WorkerAuthController {

    private final AuthenticationService service;

    @ResponseStatus(CREATED)
    @PostMapping(path = "/register", consumes = "application/json")
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
