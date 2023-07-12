package com.emmanuel.sarabrandserver.security;

import com.emmanuel.sarabrandserver.jwt.CustomFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;

    @Value(value = "${custom.cookie.frontend}")
    private String LOGGEDSESSION;

    private final AuthenticationEntryPoint authEntryPoint;
    private final CustomFilter customFilter;

    public SecurityConfig(
            @Qualifier(value = "authEntryPoint") AuthenticationEntryPoint authEntry,
            CustomFilter customFilter
    ) {
        this.authEntryPoint = authEntry;
        this.customFilter = customFilter;
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
        return http
                .csrf(csrf -> csrf.csrfTokenRepository(new CookieCsrfTokenRepository()))
//                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(publicRoutes()).permitAll();
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(this.customFilter, BearerTokenAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))
                .exceptionHandling((ex) -> ex.authenticationEntryPoint(this.authEntryPoint))
                .logout(out -> out
                        .logoutUrl("/api/v1/auth/logout")
                        .deleteCookies(JSESSIONID, LOGGEDSESSION)
                        .logoutSuccessHandler((request, response, authentication) ->
                                SecurityContextHolder.clearContext()
                        )
                )
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("role");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    /**
     * The reason for a custom BearerTokenResolver is since by default, Resource Server looks for a bearer token in the
     * Authorization header, and I am sending my jwt token as a cookie instead of Authorization
     * header, I need to inform Resource Server/BearerTokenAuthenticationFilter where to look for my jwt token.
     * Look at the link below for reference.
     * <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/bearer-tokens.html">...</a>
     * */
    @Bean
    public BearerTokenResolver bearerTokenResolver(JwtDecoder jwtDecoder) {
        return new CustomBearerTokenResolver(jwtDecoder);
    }

    @Bean
    public AuthenticationProvider provider(
            @Qualifier(value = "clientDetailService") UserDetailsService detailsService,
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

    /**
     * For each authentication that succeeds or fails, a AuthenticationSuccessEvent or AuthenticationFailureEvent,
     * respectively, is fired.
     * <a href="https://docs.spring.io/spring-security/reference/servlet/authentication/events.html">...</a>
     * */
    @Bean(name = "authenticationEventPublisher")
    public AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
        return new DefaultAuthenticationEventPublisher(publisher);
    }

    private static class CustomBearerTokenResolver implements BearerTokenResolver {
        @Value(value = "${server.servlet.session.cookie.name}")
        private String JSESSIONID;

        private final JwtDecoder jwtDecoder;

        public CustomBearerTokenResolver(JwtDecoder jwtDecoder) {
            this.jwtDecoder = jwtDecoder;
        }

        @Override
        public String resolve(HttpServletRequest request) {
            Cookie[] cookies = request.getCookies();

            if (cookies == null) {
                return null;
            }

            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(JSESSIONID)) {
                    // Try catch might be an expensive compute, but it is worth it
                    try {
                        String token = cookie.getValue();
                        this.jwtDecoder.decode(token); // Will throw an exception if token is invalid
                        return token;
                    } catch (JwtException e) {
                        log.error("Jwt Exception from CustomBearerTokenResolver {}", e.getMessage());
                    }
                }
            }

            return null;
        }
        // End of override method
    }

}
