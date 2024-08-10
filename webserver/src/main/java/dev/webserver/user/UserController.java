package dev.webserver.user;

import dev.webserver.util.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}worker/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Pageable<UserResponse> allUsers(
            @RequestParam(name = "page", defaultValue = "0")
            final Integer page,
            @RequestParam(name = "size", defaultValue = "20")
            final Integer size
    ) {
        return service.allUsers(page, Math.min(size, 20));
    }

}
