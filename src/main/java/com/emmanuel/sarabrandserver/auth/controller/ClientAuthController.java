package com.emmanuel.sarabrandserver.auth.controller;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.service.AuthService;
import com.emmanuel.sarabrandserver.clientz.dto.ClientRegisterDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** All routes in class a public */
@RestController @RequestMapping(path = "api/v1/client/auth")
public class ClientAuthController {

    private final AuthService authService;

    public ClientAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(path = "/register", consumes = "application/json")
    public ResponseEntity<?> register(@Valid @RequestBody ClientRegisterDTO dto) {
        return this.authService.clientRegister(dto);
    }

    @PostMapping(path = "/login", consumes = "application/json")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest req, HttpServletResponse res) {
        return this.authService.login("client", dto, req, res);
    }

}
