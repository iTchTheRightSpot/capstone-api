package dev.webserver;

import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "test")
@Profile(value = {"test"})
class TestController {

    @GetMapping(path = "/client")
    @PreAuthorize(value = "hasRole('ROLE_USER')")
    public String client() {
        return "client";
    }

    @GetMapping(path = "/worker")
    @PreAuthorize(value = "hasRole('ROLE_EMPLOYEE')")
    public String worker() {
        return "worker";
    }

}