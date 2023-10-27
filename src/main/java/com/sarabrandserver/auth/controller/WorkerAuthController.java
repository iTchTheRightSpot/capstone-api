package com.sarabrandserver.auth.controller;

import com.sarabrandserver.auth.response.ActiveUser;
import com.sarabrandserver.auth.dto.LoginDTO;
import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.service.AuthService;
import com.sarabrandserver.enumeration.RoleEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "api/v1/worker/auth")
@RequiredArgsConstructor
public class WorkerAuthController {

    private final AuthService authService;

    @ResponseStatus(CREATED)
    @PostMapping(path = "/register", consumes = "application/json")
    @PreAuthorize(value = "hasRole('ROLE_WORKER')")
    public void register(@Valid @RequestBody RegisterDTO dto) {
        this.authService.workerRegister(dto);
    }

    @ResponseStatus(OK)
    @PostMapping(path = "/login", consumes = "application/json")
    public void login(
            @Valid @RequestBody LoginDTO dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        this.authService.login(RoleEnum.WORKER, dto, request, response);
    }

    /** Validates if a user still has a valid session */
    @ResponseStatus(OK)
    @GetMapping(produces = "application/json")
    @PreAuthorize(value = "hasRole('ROLE_WORKER')")
    public ActiveUser getUser() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return new ActiveUser(name);
    }

}
