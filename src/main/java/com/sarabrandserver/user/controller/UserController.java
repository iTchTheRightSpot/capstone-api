package com.sarabrandserver.user.controller;

import com.sarabrandserver.user.res.UserResponse;
import com.sarabrandserver.user.service.SarreBrandUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}worker/user")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
public class UserController {

    private final SarreBrandUserService service;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Page<UserResponse> allUsers(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        return service.allUsers(page, Math.min(size, 20));
    }

}
