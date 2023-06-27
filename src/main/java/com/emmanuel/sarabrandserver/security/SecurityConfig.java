package com.emmanuel.sarabrandserver.security;

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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import static org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Value(value = "${custom.max.session}")
    private int MAX_SESSION;

    @Value(value = "${custom.cookie.name}")
    private String COOKIE_NAME;

    @Value(value = "${custom.cookie.frontend}")
    private String LOGGED_IN;

    private final RedisIndexedSessionRepository redisIndexedSessionRepository;
    private final AuthenticationEntryPoint authEntryPoint;

    public SecurityConfig(
            RedisIndexedSessionRepository sessionRepository,
            @Qualifier(value = "authEntryPoint") AuthenticationEntryPoint authEntry
    ) {
        this.redisIndexedSessionRepository = sessionRepository;
        this.authEntryPoint = authEntry;
    }

    private String[] publicRoutes() {
        return new String[]{
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
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
//                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> {
//                    auth.anyRequest().permitAll();
                    auth.requestMatchers(publicRoutes()).permitAll();
                    auth.anyRequest().authenticated();
                })
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(IF_REQUIRED) //
                        .sessionAuthenticationStrategy(new CustomStrategy(
                                this.redisIndexedSessionRepository,
                                sessionRegistry()
                        ))
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::newSession) //
                        .maximumSessions(MAX_SESSION) //
                        .sessionRegistry(sessionRegistry())
                )
                .exceptionHandling((ex) -> ex.authenticationEntryPoint(this.authEntryPoint))
                .logout(out -> out
                        .logoutUrl("/api/v1/auth/logout")
                        .invalidateHttpSession(true) // Invalidate all sessions after logout
                        .deleteCookies(COOKIE_NAME, LOGGED_IN)
                        .addLogoutHandler(new CustomLogoutHandler(this.redisIndexedSessionRepository))
                        .logoutSuccessHandler((request, response, authentication) ->
                                SecurityContextHolder.clearContext()
                        )
                )
                .build();
    }

    @Bean
    public SpringSessionBackedSessionRegistry<? extends Session> sessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(this.redisIndexedSessionRepository);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            @Qualifier(value = "clientDetailService") UserDetailsService clientDetailService,
            @Qualifier(value = "passwordEncoder") PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(clientDetailService);
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

}
