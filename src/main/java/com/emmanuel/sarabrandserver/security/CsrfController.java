package com.emmanuel.sarabrandserver.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * As per Spring Security docs
 * <a href="https://docs.spring.io/spring-security/reference/5.8/migration/servlet/exploits.html#servlet-opt-in-defer-loading-csrf-token">...</a>
 * */
@RestController
@RequestMapping(path = "api/v1/auth")
public class CsrfController {

    @GetMapping(path = "/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

    /** Validates if a user still has a valid session */
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getUser() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return new ResponseEntity<>(new AuthRes(name), HttpStatus.OK);
    }

    private record AuthRes (String principal) { }

}
