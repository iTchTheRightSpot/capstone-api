package com.emmanuel.sarabrandserver.auth.controller;

import com.emmanuel.sarabrandserver.auth.dto.ActiveUser;
import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping(path = "api/v1/worker/auth")
public class WorkerAuthController {
    private final AuthService authService;

    public WorkerAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(path = "/register", consumes = "application/json")
    @PreAuthorize(value = "hasRole('ROLE_WORKER')")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO dto) {
        return this.authService.workerRegister(dto);
    }

    @PostMapping(path = "/login", consumes = "application/json")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginDTO dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return this.authService.login(dto, request, response);
    }

    /** Validates if a user still has a valid session */
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getUser() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return new ResponseEntity<>(new ActiveUser(name), HttpStatus.OK);
    }

}
