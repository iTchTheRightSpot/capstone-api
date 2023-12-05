package com.sarabrandserver.auth.controller;

import com.sarabrandserver.auth.dto.LoginDTO;
import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.response.ActiveUser;
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
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}client/auth")
@RequiredArgsConstructor
public class ClientAuthController {

    private final AuthService authService;

    @ResponseStatus(CREATED)
    @PostMapping(path = "/register", consumes = APPLICATION_JSON_VALUE)
    public void register(@Valid @RequestBody RegisterDTO dto, HttpServletResponse response) {
        this.authService.clientRegister(dto, response);
    }

    @ResponseStatus(OK)
    @PostMapping(path = "/login", consumes = APPLICATION_JSON_VALUE)
    public void login(
            @Valid @RequestBody LoginDTO dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        this.authService.login(RoleEnum.CLIENT, dto, request, response);
    }

    /**
     * validates if a user still has a valid session
     * */
    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ROLE_CLIENT')")
    public ActiveUser getUser() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return new ActiveUser(name);
    }

}
