package com.emmanuel.sarabrandserver.auth.controller;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping(path = "api/v1/client/auth")
public class ClientAuthController {
    private final AuthService authService;

    public ClientAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(path = "/register", consumes = "application/json")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO dto) {
        return this.authService.clientRegister(dto);
    }

    @PostMapping(path = "/login", consumes = "application/json")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginDTO dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return this.authService.login(dto, request, response);
    }

}
