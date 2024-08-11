package dev.webserver.security.mvc;

import dev.webserver.AbstractIntegration;
import dev.webserver.enumeration.RoleEnum;
import dev.webserver.security.JwtService;
import dev.webserver.security.UserDetailz;
import dev.webserver.user.Role;
import dev.webserver.user.RoleRepository;
import dev.webserver.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

final class AuthenticationControllerTest extends AbstractIntegration {

    @Value("/${api.endpoint.baseurl}")
    private String apiPrefix;
    @Value(value = "${server.servlet.session.cookie.name}")
    private String jsessionid;
    @Value(value = "${developer.email}")
    private String developerEmail;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtService jwtService;

    @Test
    void shouldTestCustomAuthenticationEntryPointIsAddingRedirectUrl() throws Exception {
        super.mockMvc.perform(get(apiPrefix + "employee/test"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.redirect_url", not(empty())));
    }

    @Test
    void shouldTestCustomJwtMechanics() throws Exception {
        // given
        final var employee = userRepository.userByPrincipal(developerEmail).orElseThrow();

        final var roles = List.of(
                roleRepository.save(new Role(null, RoleEnum.DEVELOPER, employee.userId())),
                roleRepository.save(new Role(null, RoleEnum.EMPLOYEE, employee.userId())));

        final UserDetailz details = new UserDetailz(employee, roles);

        final String jwt = jwtService.generateJwt(authenticated(details, null, details.getAuthorities()));

        // request
        super.mockMvc
                .perform(get(apiPrefix + "employee/test").cookie(new Cookie(jsessionid, jwt)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void shouldTestRedirectFromAuthenticationRouteIfRequestContainsValidJwt() throws Exception {
        // given
        final var employee = userRepository.userByPrincipal(developerEmail).orElseThrow();

        final var roles = List.of(
                roleRepository.save(new Role(null, RoleEnum.DEVELOPER, employee.userId())),
                roleRepository.save(new Role(null, RoleEnum.EMPLOYEE, employee.userId())));

        final UserDetailz details = new UserDetailz(employee, roles);

        final String jwt = jwtService.generateJwt(authenticated(details, null, details.getAuthorities()));

        // request
        super.mockMvc
                .perform(get(apiPrefix + "authentication/authenticate").cookie(new Cookie(jsessionid, jwt)))
                .andExpect(status().isFound())
                .andExpect(cookie().exists(jsessionid))
                .andExpect(jsonPath("$").doesNotExist())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    void shouldTestExcessDeniedPayload() throws Exception {
        super.mockMvc
                .perform(get(apiPrefix + "employee/test"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    void shouldSuccessfullyLogout() throws Exception {
        // given
        final var employee = userRepository.userByPrincipal(developerEmail).orElseThrow();

        final var roles = List.of(
                roleRepository.save(new Role(null, RoleEnum.DEVELOPER, employee.userId())),
                roleRepository.save(new Role(null, RoleEnum.EMPLOYEE, employee.userId())));

        final UserDetailz details = new UserDetailz(employee, roles);

        final String jwt = jwtService.generateJwt(authenticated(details, null, details.getAuthorities()));

        // jwt cookie
        Cookie cookie = new Cookie(jsessionid, jwt);

        // logout
        final MvcResult logout = this.mockMvc
                .perform(post(apiPrefix + "logout").cookie(cookie).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirect_url", not(empty())))
                .andReturn();

        cookie = logout.getResponse().getCookie(jsessionid);

        assertThat(cookie).isNotNull();

        // access protected route with invalid cookie
        super.mockMvc
                .perform(get(apiPrefix + "employee/test").cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"));
    }

}