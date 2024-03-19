<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/user/controller/UserController.java
package dev.webserver.user.controller;

import dev.webserver.user.res.UserResponse;
import dev.webserver.user.service.SarreBrandUserService;
========
package dev.capstone.user.controller;

import dev.capstone.user.res.UserResponse;
import dev.capstone.user.service.SarreBrandUserService;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/user/controller/UserController.java
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
