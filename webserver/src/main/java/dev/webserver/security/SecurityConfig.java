package dev.webserver.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.exception.ExceptionResponse;
import dev.webserver.user.UserRepository;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
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
@EnableMethodSecurity
class SecurityConfig {

    /**
     * Reason for Consumer<ResponseCookie.ResponseCookieBuilder> as per docs secure, domain
     * name and path are deprecated.
     *
     * @see <a href="https://github.com/spring-projects/spring-security/blob/main/web/src/main/java/org/springframework/security/web/csrf/CookieCsrfTokenRepository.java">documentation</a>
     */
    static final BiFunction<Boolean, String, CookieCsrfTokenRepository> csrfRepo = (secure, sameSite) -> {
        final Consumer<ResponseCookie.ResponseCookieBuilder> consumer = (cookie) -> cookie
                .httpOnly(false)
                .secure(secure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(-1);

        var csrf = new CookieCsrfTokenRepository();
        csrf.setCookieCustomizer(consumer);
        return csrf;
    };

    @Value(value = "${server.servlet.session.cookie.name}")
    private String jsessionid;
    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean cookiesecure;
    @Value(value = "${server.servlet.session.cookie.same-site}")
    private String samesite;
    @Value(value = "${cors.ui.domain}")
    private String corsdomain;
    @Value("/${api.endpoint.baseurl}")
    private String baseurl;
    @Value("${spring.profiles.active}")
    private String profile;

    @Bean
    public UserDetailsService userDetailsService(UserRepository repository) {
        return username -> repository
                .userByPrincipal(username)
                .map(CapstoneUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found"));
    }

    @Bean
    public AuthenticationManager manager(
            UserDetailsService service,
            PasswordEncoder encoder,
            @Qualifier(value = "authenticationEventPublisher") AuthenticationEventPublisher publisher
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(service);
        provider.setPasswordEncoder(encoder);

        ProviderManager manager = new ProviderManager(provider);
        manager.setAuthenticationEventPublisher(publisher);
        manager.setEraseCredentialsAfterAuthentication(true);
        return manager;
    }

    /**
     * <a href="https://docs.spring.io/spring-session/reference/guides/java-custom-cookie.html">documentation</a>
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        var serializer = new DefaultCookieSerializer();
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        return serializer;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(15);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(corsdomain));
        config.setAllowedMethods(List.of(GET.name(), PUT.name(), POST.name(), DELETE.name(), OPTIONS.name()));
        config.setAllowedHeaders(List.of(CONTENT_TYPE, ACCEPT, "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Security filter chain responsible for upholding app security
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            ObjectMapper mapper,
            RefreshTokenFilter refreshTokenFilter,
            JwtAuthenticationConverter converter
    ) throws Exception {

        if (profile.equals("native-test")) {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(registry -> registry.anyRequest().permitAll());
        } else {
            final String[] pubRoutes = {"/error", "/actuator/health", baseurl + "csrf", baseurl + "client/**", baseurl + "worker/auth/login", baseurl + "cart/**", baseurl + "payment/**", baseurl + "checkout/**"};
            var csrfTokenRepository = csrfRepo.apply(cookiesecure, samesite);

            // csrf config
            // https://docs.spring.io/spring-security/reference/5.8/migration/servlet/exploits.html
            http
                    .csrf(csrf -> csrf
                            .ignoringRequestMatchers(antMatcher(POST, baseurl + "payment/webhook"))
                            .csrfTokenRepository(csrfTokenRepository)
                            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                    .addFilterAfter(new CookieCsrfFilter(), BasicAuthenticationFilter.class)
                    .authorizeHttpRequests(registry -> registry
                            .requestMatchers(pubRoutes).permitAll()
                            .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                            .requestMatchers(baseurl + "cron/**").hasRole(NATIVE.name())
                            .requestMatchers(baseurl + "native/**").hasRole(NATIVE.name())
                            .requestMatchers(baseurl + "shipping/**").hasRole(WORKER.name())
                            .requestMatchers(baseurl + "tax/**").hasRole(WORKER.name())
                            .requestMatchers(baseurl + "worker/**").hasRole(WORKER.name())
                            .requestMatchers(baseurl + "auth/worker").hasRole(WORKER.name())
                            .requestMatchers(baseurl + "order/**").hasAnyRole(WORKER.name(), CLIENT.name())
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
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(OK))
                )
                .build();
    }

}