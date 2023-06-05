package com.example.sarabrandserver.worker.auth.controller;

import com.example.sarabrandserver.dto.LoginDTO;
import com.example.sarabrandserver.worker.auth.service.WorkerAuthService;
import com.example.sarabrandserver.worker.dto.WorkerRegisterDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping(path = "api/v1/auth/worker/")
public class WorkerAuthController {

    private final WorkerAuthService workerAuthService;

    public WorkerAuthController(WorkerAuthService workerAuthService) {
        this.workerAuthService = workerAuthService;
    }

    @PostMapping(path = "register", consumes = "application/json")
    @PreAuthorize(value = "hasAnyAuthority('WORKER')")
    public ResponseEntity<?> register(WorkerRegisterDTO dto) {
        return this.workerAuthService.register(dto);
    }

    @PostMapping(path = "login", consumes = "application/json")
    public ResponseEntity<?> login(LoginDTO dto, HttpServletRequest req, HttpServletResponse res) {
        return this.workerAuthService.login(dto, req, res);
    }

}
