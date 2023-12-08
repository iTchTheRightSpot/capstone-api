package com.sarabrandserver.auth.config;

import com.sarabrandserver.auth.jwt.RefreshTokenFilter;
import com.sarabrandserver.auth.service.UserDetailService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
import java.util.function.Consumer;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * API docs using session <a href="https://docs.spring.io/spring-session/reference/api.html">...</a>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;
    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean COOKIESECURE;
    @Value(value = "${server.servlet.session.cookie.same-site}")
    private String SAMESITE;
    @Value(value = "${cors.ui.domain}")
    private String CORSDOMAIN;
    @Value("${api.endpoint.baseurl}")
    private String BASEURL;

    @Bean
    public AuthenticationProvider provider(UserDetailService service, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(service);
        provider.setPasswordEncoder(encoder);
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
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(this.CORSDOMAIN));
        configuration.setAllowedMethods(List.of("GET", "PUT", "POST", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(CONTENT_TYPE, ACCEPT, "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Security filter chain responsible for upholding app security
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AuthEntryPoint authEntryPoint,
            RefreshTokenFilter refreshTokenFilter,
            JwtAuthenticationConverter converter
    ) throws Exception {
        var csrfTokenRepository = getCookieCsrfTokenRepository(this.COOKIESECURE, this.SAMESITE);
        return http

                // CSRF Config
                // https://docs.spring.io/spring-security/reference/5.8/migration/servlet/exploits.html
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .addFilterAfter(new CookieCsrfFilter(), BasicAuthenticationFilter.class)

                // Cors Config
                .cors(withDefaults())

                // Public routes
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/" + this.BASEURL + "csrf",
                            "/" + this.BASEURL + "client/auth/register",
                            "/" + this.BASEURL + "client/auth/login",
                            "/" + this.BASEURL + "client/product/**",
                            "/" + this.BASEURL + "client/category/**",
                            "/" + this.BASEURL + "client/collection/**",
                            "/" + this.BASEURL + "worker/auth/login",
                            "/" + this.BASEURL + "cart/**"
                    ).permitAll();
                    auth.anyRequest().authenticated();
                })

                 // Jwt
                .addFilterBefore(refreshTokenFilter, BearerTokenAuthenticationFilter.class)
                // https://docs.spring.io/spring-security/reference/6.0/servlet/oauth2/resource-server/jwt.html
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))

                // Session Management
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))

                // Exception Handling.
                .exceptionHandling((ex) -> ex.authenticationEntryPoint(authEntryPoint))

                // Logout
                // https://docs.spring.io/spring-security/reference/servlet/authentication/logout.html
                .logout((logoutConfig) -> logoutConfig
                        .logoutUrl("/" + this.BASEURL + "logout")
                        .deleteCookies(this.JSESSIONID)
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(OK))
                )
                .build();
    }

    /**
     * Reason for Consumer<ResponseCookie.ResponseCookieBuilder> as per docs secure, domain name and path are deprecated
     * As per docs
     * <a href="https://github.com/spring-projects/spring-security/blob/main/web/src/main/java/org/springframework/security/web/csrf/CookieCsrfTokenRepository.java">...</a>
     */
    private static CookieCsrfTokenRepository getCookieCsrfTokenRepository(boolean secure, String sameSite) {
        Consumer<ResponseCookie.ResponseCookieBuilder> consumer = (cookie) -> cookie
                .httpOnly(false)
                .secure(secure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(-1);

        CookieCsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();
        csrfTokenRepository.setCookieCustomizer(consumer);
        return csrfTokenRepository;
    }

}
