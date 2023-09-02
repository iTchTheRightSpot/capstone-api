package com.emmanuel.sarabrandserver.auth.config;

import com.emmanuel.sarabrandserver.auth.jwt.RefreshTokenFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.security.config.Customizer.withDefaults;

/**
 * API docs using session <a href="https://docs.spring.io/spring-session/reference/api.html">...</a>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final Environment environment;
    private final AuthenticationEntryPoint authEntryPoint;
    private final RefreshTokenFilter refreshTokenFilter;

    public SecurityConfig(
            Environment environment,
            @Qualifier(value = "authEntryPoint") AuthenticationEntryPoint authEntry,
            RefreshTokenFilter refreshTokenFilter
    ) {
        this.environment = environment;
        this.authEntryPoint = authEntry;
        this.refreshTokenFilter = refreshTokenFilter;
    }

    @Bean
    public AuthenticationProvider provider(
            @Qualifier(value = "userDetailService") UserDetailsService detailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(detailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationProvider provider,
            @Qualifier(value = "authenticationEventPublisher") AuthenticationEventPublisher publisher
    ) {
        ProviderManager providerManager = new ProviderManager(provider);
        providerManager.setAuthenticationEventPublisher(publisher);
        providerManager.setEraseCredentialsAfterAuthentication(true);
        return providerManager;
    }

    /**
     * <a href="https://docs.spring.io/spring-session/reference/guides/java-custom-cookie.html">...</a>
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        return cookieSerializer;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(15);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowOrigins = new ArrayList<>(2);
        allowOrigins.add("https://*.emmanueluluabuike.com/");

        var profile = this.environment.getProperty("spring.profiles.active", "");
        if (profile.equals("dev") || profile.equals("test")) {
            allowOrigins.add("http://localhost:4200/");
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowOrigins);
        configuration.setAllowedMethods(List.of("GET", "PUT", "POST", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(CONTENT_TYPE, ACCEPT, "X-XSRF-TOKEN"));
//        configuration.setExposedHeaders(List.of("X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Security filter chain responsible for upholding app security
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var JSESSIONID = this.environment.getProperty("server.servlet.session.cookie.name", "");
        var domain = this.environment.getProperty("server.servlet.session.cookie.domain", "");
        var profile = this.environment.getProperty("spring.profiles.active", "");
        var csrfTokenRepository = getCookieCsrfTokenRepository(domain, profile);

        return http

                // Cors and CSRF config
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .cors(withDefaults())

                // Public routes
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/api/v1/home/**",
                            "/api/v1/auth/csrf",
                            "/api/v1/client/auth/register",
                            "/api/v1/client/auth/login",
                            "/api/v1/client/product/**",
                            "/api/v1/client/category/**",
                            "/api/v1/client/collection/**",
                            "/api/v1/worker/auth/login"
                    ).permitAll();
                    auth.anyRequest().authenticated();
                })

                // Refresh Token Filter
                .addFilterBefore(this.refreshTokenFilter, BearerTokenAuthenticationFilter.class)

                // Session Management
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))

                // Exception Handling. Allows ControllerAdvices to take effect
                .exceptionHandling((ex) -> ex.authenticationEntryPoint(this.authEntryPoint))

                // Logout
                // https://docs.spring.io/spring-security/reference/servlet/authentication/logout.html
                .logout((logoutConfig) -> logoutConfig
                        .logoutUrl("/api/v1/logout")
                        .deleteCookies(JSESSIONID)
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                )
                .build();
    }

    /**
     * Reason for Consumer<ResponseCookie.ResponseCookieBuilder> as per docs secure, domain name and path are deprecated
     * As per docs
     * <a href="https://github.com/spring-projects/spring-security/blob/main/web/src/main/java/org/springframework/security/web/csrf/CookieCsrfTokenRepository.java">...</a>
     */
    private static CookieCsrfTokenRepository getCookieCsrfTokenRepository(String domain, String profile) {
        boolean secure = profile.equals("prod") || profile.equals("stage");
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        Consumer<ResponseCookie.ResponseCookieBuilder> csrfCookieCustomizer = (cookie) -> cookie
                .domain(domain)
                .httpOnly(false)
                .secure(secure)
                .path("/")
                .sameSite("lax")
                .maxAge(-1);
        csrfTokenRepository.setCookieCustomizer(csrfCookieCustomizer);
        return csrfTokenRepository;
    }

}
