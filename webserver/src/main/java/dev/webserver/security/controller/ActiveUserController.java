package dev.webserver.security.controller;

import dev.webserver.security.JwtEnum;
import dev.webserver.security.demo.DemoUser;
import dev.webserver.user.Role;
import dev.webserver.user.RoleRepository;
import dev.webserver.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}active")
@RequiredArgsConstructor
class ActiveUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ActiveUser active(final Authentication authentication) {
        final var optional = Optional.ofNullable(authentication);

        if (optional.isEmpty()) return ActiveUser.builder().build();

        final JwtAuthenticationToken jwt = (JwtAuthenticationToken) authentication;

        final long userid = jwt.getToken().getClaim(JwtEnum.USER_ID.property());

        if (userid == -1L) {
            return DemoUser.active;
        }

        final var userOptional = userRepository.findById(userid);

        if (userOptional.isEmpty()) return ActiveUser.builder().build();

        final var user = userOptional.get();
        final var roles = roleRepository.allRolesByUserId(userid).stream().map(Role::role).toList();

        return ActiveUser.builder()
                .userId(user.userId())
                .name(user.firstname())
                .email(user.email())
                .imageKey(user.imageKey())
                .roles(roles)
                .build();
    }

}