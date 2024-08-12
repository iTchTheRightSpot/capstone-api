package dev.webserver.security.controller;

import dev.webserver.AbstractIntegration;
import dev.webserver.security.JwtService;
import dev.webserver.security.UserDetailz;
import dev.webserver.user.Role;
import dev.webserver.user.RoleRepository;
import dev.webserver.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class ActiveUserControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}active")
    private String path;
    @Value("${server.servlet.session.cookie.name}")
    private String jsessionid;
    @Value("${developer.email}")
    private String developeremail;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtService jwtService;

    @Test
    void shouldRetrieveActiveUserDetails() throws Exception {
        // given
        final var user = userRepository.userByPrincipal(developeremail).orElseThrow();
        final var roles = roleRepository.allRolesByUserId(user.userId());
        final var details = new UserDetailz(user, roles);
        final UsernamePasswordAuthenticationToken authentication =
                authenticated(details, null, details.getAuthorities());
        final var jwt = jwtService.generateJwt(authentication);

        // request
        final String content = super.mockMvc
                .perform(get(path).cookie(new Cookie(jsessionid, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(user.userId()))
                .andExpect(jsonPath("$.name").value(user.firstname()))
                .andExpect(jsonPath("$.email").value(user.email()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(roles.stream().map(Role::role).toList())
                .isEqualTo(super.mapper.readValue(content, ActiveUser.class).roles());
    }
}