package dev.webserver.security.demo;

import dev.webserver.enumeration.RoleEnum;
import dev.webserver.security.UserDetailz;
import dev.webserver.security.controller.ActiveUser;
import dev.webserver.user.Role;
import dev.webserver.user.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class DemoUser {
    public static final User demo = User.builder()
            .firstname("Capstone Demo")
            .fullname("Capstone Demo Account")
            .email("demo@capstone.com")
            .userId(-1L)
            .imageKey(null)
            .build();

    public static final String password = "password123";

    public static final List<Role> roles = List.of(
            new Role(1L, RoleEnum.DEMO, demo.userId()),
            new Role(2L, RoleEnum.USER, demo.userId()));

    public static final ActiveUser active = ActiveUser.builder()
            .name(demo.firstname())
            .email(demo.email())
            .userId(-1L)
            .imageKey(null)
            .roles(List.of(RoleEnum.DEMO, RoleEnum.USER))
            .build();

    private static final Function<UserDetailz, UsernamePasswordAuthenticationToken> authenticated = (details) ->
            UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());

    /**
     * Returns a {@link DemoUser} credentials or {@link User} credentials.
     * */
    public static final BiFunction<String, UserDetailsService, UsernamePasswordAuthenticationToken> UPAT = (userid, service) -> {
        if (Long.parseLong(userid) == demo.userId())
            return authenticated.apply(new UserDetailz(demo, roles));
        final UserDetails details = service.loadUserByUsername(userid);
        return UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
    };
}
