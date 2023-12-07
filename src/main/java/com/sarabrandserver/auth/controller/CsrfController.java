package com.sarabrandserver.auth.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * As per Spring Security docs
 * <a href="https://docs.spring.io/spring-security/reference/5.8/migration/servlet/exploits.html#servlet-opt-in-defer-loading-csrf-token">...</a>
 * */
@RestController
@RequestMapping(path = "${api.endpoint.baseurl}csrf")
public class CsrfController {

    @GetMapping
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

}
