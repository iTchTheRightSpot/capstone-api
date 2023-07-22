package com.emmanuel.sarabrandserver.security;

import com.emmanuel.sarabrandserver.jwt.RefreshTokenFilter;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {
    private final AuthenticationEntryPoint authEntryPoint;
    private final RefreshTokenFilter refreshTokenFilter;
    private final Environment environment;
    private final CustomUtil customUtil;

    public SecurityConfig(
            @Qualifier(value = "authEntryPoint") AuthenticationEntryPoint authEntry,
            RefreshTokenFilter refreshTokenFilter,
            Environment environment,
            CustomUtil customUtil
    ) {
        this.authEntryPoint = authEntry;
        this.refreshTokenFilter = refreshTokenFilter;
        this.environment = environment;
        this.customUtil = customUtil;
    }

    @Bean
    public AuthenticationProvider provider(
            @Qualifier(value = "clientDetailsService") UserDetailsService detailsService,
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
        return providerManager;
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        return cookieSerializer;
    }

    private String[] publicRoutes() {
        return new String[]{
                "/api/v1/csrf/**",
                "/api/v1/client/auth/register",
                "/api/v1/client/auth/login",
                "/api/v1/client/product/**",
                "/api/v1/client/category/**",
                "/api/v1/client/collection/**",
                "/api/v1/worker/auth/login",
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String JSESSIONID = this.environment.getProperty("server.servlet.session.cookie.name");
        String LOGGEDSESSION = this.environment.getProperty("custom.cookie.frontend");

        return http
                .csrf(csrf -> csrf.csrfTokenRepository(new CookieCsrfTokenRepository()))
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(publicRoutes()).permitAll();
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(this.refreshTokenFilter, BearerTokenAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))
                .exceptionHandling((ex) -> ex.authenticationEntryPoint(this.authEntryPoint))
                .logout(out -> out
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            Cookie[] cookies = request.getCookies();
                            if (cookies != null) {
                                for (Cookie cookie : cookies) {
                                    if (cookie.getName().equals(JSESSIONID) || cookie.getName().equals(LOGGEDSESSION)) {
                                        this.customUtil.expireCookie(cookie);
                                        response.addCookie(cookie);
                                    }
                                }
                            }
                            SecurityContextHolder.clearContext();
                        })
                )
                .build();
    }

    /**
     * The reason for BearerResolver is since by default, Resource Server looks for a bearer token in the
     * Authorization header, and I am sending my jwt token as a cookie instead of Authorization
     * header, I need to inform Resource Server/BearerTokenAuthenticationFilter where to look for my jwt token.
     * Look at the link below for reference.
     * <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/bearer-tokens.html">...</a>
     * */
    @Bean
    public BearerTokenResolver bearerTokenResolver(JwtDecoder jwtDecoder) {
        return new BearerResolver(this.environment, jwtDecoder, this.customUtil);
    }

    private record BearerResolver(Environment env, JwtDecoder decoder, CustomUtil customUtil) implements BearerTokenResolver {
        @Override
        public String resolve(HttpServletRequest request) {
            Cookie[] cookies = request.getCookies();

            if (cookies == null) {
                return null;
            }

            for (Cookie cookie : request.getCookies()) {
                String JSESSIONID = this.env.getProperty("server.servlet.session.cookie.name");
                if (cookie.getName().equals(JSESSIONID)) {
                    try {
                        String token = cookie.getValue();
                        this.decoder.decode(token); // Will throw an exception if token is invalid
                        return token;
                    } catch (JwtException e) {
                        this.customUtil.expireCookie(cookie);
                        log.error("Jwt Exception from CustomBearerTokenResolver {}", e.getMessage());
                    }
                }
            }
            return null;
        }
    }

}
