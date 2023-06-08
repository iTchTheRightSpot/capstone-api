package com.example.sarabrandserver.auth.controller;

import com.example.sarabrandserver.auth.service.AuthService;
import com.example.sarabrandserver.dto.LoginDTO;
import com.example.sarabrandserver.worker.dto.WorkerRegisterDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping(path = "api/v1/auth/worker")
public class WorkerAuthController {

    private final AuthService authService;

    public WorkerAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(path = "/register", consumes = "application/json")
    @PreAuthorize(value = "hasAnyAuthority('WORKER')")
    public ResponseEntity<?> register(@Valid @RequestBody WorkerRegisterDTO dto) {
        return this.authService.workerRegister(dto);
    }

    @PostMapping(path = "/login", consumes = "application/json")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest req, HttpServletResponse res) {
        return this.authService.login("worker", dto, req, res);
    }

}
