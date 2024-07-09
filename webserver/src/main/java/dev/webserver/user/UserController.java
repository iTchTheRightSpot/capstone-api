package dev.webserver.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}worker/user")
@RequiredArgsConstructor
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
