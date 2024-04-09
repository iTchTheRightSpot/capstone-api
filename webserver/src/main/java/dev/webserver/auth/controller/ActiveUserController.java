package dev.webserver.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}auth")
public class ActiveUserController {

    public record ActiveUser(String principal) { }

    @ResponseStatus(OK)
    @GetMapping(path = "/client", produces = APPLICATION_JSON_VALUE)
    public ActiveUser client() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return new ActiveUser(name);
    }

    @ResponseStatus(OK)
    @GetMapping(path = "/worker", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ROLE_WORKER')")
    public ActiveUser worker() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return new ActiveUser(name);
    }

}