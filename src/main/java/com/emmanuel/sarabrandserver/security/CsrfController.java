package com.emmanuel.sarabrandserver.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/v1/csrf")
@Slf4j
public class CsrfController {

    @GetMapping
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

}
