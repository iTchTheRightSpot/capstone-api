package dev.webserver.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.AbstractEnvironment;
import dev.webserver.exception.ExceptionResponse;
import dev.webserver.user.RoleRepository;
import dev.webserver.user.UserRepository;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static dev.webserver.enumeration.RoleEnum.*;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
class SecurityConfig extends AbstractEnvironment {

    /**
     * Reason for Consumer<ResponseCookie.ResponseCookieBuilder> as per docs secure, domain
     * name and path are deprecated.
     *
     * @see <a href="https://github.com/spring-projects/spring-security/blob/main/web/src/main/java/org/springframework/security/web/csrf/CookieCsrfTokenRepository.java">documentation</a>
     */
    static final BiFunction<Boolean, String, CookieCsrfTokenRepository> CSRF_REPO = (secure, sameSite) -> {
        final Consumer<ResponseCookie.ResponseCookieBuilder> consumer = (cookie) -> cookie
                .httpOnly(false)
                .secure(secure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(-1);

        final var csrf = new CookieCsrfTokenRepository();
        csrf.setCookieCustomizer(consumer);
        return csrf;
    };

    protected SecurityConfig(final Environment environment) {
        super(environment);
    }

    @Bean
    public UserDetailsService userDetailsService(final UserRepository repository, final RoleRepository roleRepository) {
        return email -> {
            final var user = repository.userByPrincipal(email).orElseThrow(() -> new UsernameNotFoundException(email + " not found"));
            final var roles = roleRepository.allRolesByUserId(user.userId());
            return new UserDetailz(user, roles);
        };
    }

    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler successHandler() {
        return new SavedRequestAwareAuthenticationSuccessHandler();
    }

    /**
     * <a href="https://docs.spring.io/spring-session/reference/guides/java-custom-cookie.html">documentation</a>
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        final var serializer = new DefaultCookieSerializer();
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        return serializer;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(super.corsdomain));
        config.setAllowedMethods(List.of(GET.name(), PUT.name(), POST.name(), DELETE.name(), OPTIONS.name()));
        config.setAllowedHeaders(List.of(CONTENT_TYPE, ACCEPT, "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);

        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Security filter chain responsible for upholding app security
     */
    @Bean
    public SecurityFilterChain filterChain(
            final HttpSecurity http,
            final ObjectMapper mapper,
            final RefreshTokenFilter refreshTokenFilter,
            final JwtAuthenticationConverter converter
    ) throws Exception {

        if (activeprofile.equals("native-test")) {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(registry -> registry.anyRequest().permitAll());
        } else {
            final String[] pubRoutes = {"/error", "/api/v1/actuator/health", baseurl + "csrf", baseurl + "client/**", baseurl + "worker/auth/login", baseurl + "cart/**", baseurl + "payment/**", baseurl + "checkout/**", baseurl + "active/**"};
            final var csrfTokenRepository = CSRF_REPO.apply(cookiesecure, samesite);

            http
                    // csrf config
                    // https://docs.spring.io/spring-security/reference/5.8/migration/servlet/exploits.html
                    .csrf(csrf -> csrf
                            .ignoringRequestMatchers(antMatcher(POST, baseurl + "payment/webhook"))
                            .csrfTokenRepository(csrfTokenRepository)
                            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                    .addFilterAfter(new CookieCsrfFilter(), BasicAuthenticationFilter.class)

                    // global route protection
                    .authorizeHttpRequests(registry -> registry
                            .requestMatchers(pubRoutes).permitAll()
                            .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                            .requestMatchers("/api/v1/actuator/**").hasRole(DEVELOPER.name())
                            .requestMatchers(baseurl + "shipping/**").hasRole(WORKER.name())
                            .requestMatchers(baseurl + "tax/**").hasRole(WORKER.name())
                            .requestMatchers(baseurl + "worker/**").hasRole(WORKER.name())
                            .requestMatchers(baseurl + "auth/worker").hasRole(WORKER.name())
                            .requestMatchers(baseurl + "order/**").hasAnyRole(WORKER.name(), USER.name())
                            .anyRequest().denyAll());
        }

        return http

                // cors config
                .cors(withDefaults())

                // jwt
                .addFilterBefore(refreshTokenFilter, BearerTokenAuthenticationFilter.class)
                // https://docs.spring.io/spring-security/reference/6.0/servlet/oauth2/resource-server/jwt.html
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))

                // session management
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))

                // global security exception handing
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, exception) -> {
                            final String str = mapper.writeValueAsString(new ExceptionResponse(exception.getMessage(), UNAUTHORIZED));
                            response.setStatus(UNAUTHORIZED.value());
                            response.getWriter().write(str);
                            response.flushBuffer();
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            final String str = mapper.writeValueAsString(new ExceptionResponse("Access Denied", FORBIDDEN));
                            response.setStatus(FORBIDDEN.value());
                            response.getWriter().write(str);
                            response.flushBuffer();
                        }))

                // logout
                // https://docs.spring.io/spring-security/reference/servlet/authentication/logout.html
                .logout(config -> config
                        .logoutUrl(baseurl + "logout")
                        .deleteCookies(jsessionid)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            record LogoutSuccessRes(String message, String redirect, HttpStatus status) {}

                            final String redirect = String
                                    .format("%s%sauthentication/authenticate", request.getRequestURL().toString().replace(request.getRequestURI(), ""), baseurl);
                            final String str = mapper.writeValueAsString(new LogoutSuccessRes("", redirect, OK));
                            response.setStatus(OK.value());
                            response.getWriter().write(str);
                            response.flushBuffer();
                        }))
                .build();
    }

}