package com.emmanuel.sarabrandserver.security;

import com.emmanuel.sarabrandserver.util.CustomUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final Environment environment;

    public SecurityConfig(Environment environment) {
        this.environment = environment;
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
        providerManager.setEraseCredentialsAfterAuthentication(true);
        return providerManager;
    }

    /** <a href="https://docs.spring.io/spring-session/reference/guides/java-custom-cookie.html">...</a> */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setCookieName("JSESSIONID");
        cookieSerializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        return cookieSerializer;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(15);
    }

    @Bean(name = "corsConfig")
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowOrigins = new ArrayList<>(3);
        allowOrigins.add("https://admin.emmanueluluabuike.com/");
        allowOrigins.add("https://store.emmanueluluabuike.com/");

        var property = Optional.ofNullable(this.environment.getProperty("spring.profiles.active"));
        if (property.isPresent() && (property.get().equals("dev") || property.get().equals("test"))) {
            allowOrigins.add("http://localhost:4200");
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowOrigins);
        configuration.setAllowedMethods(List.of("GET", "PUT", "POST", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(CONTENT_TYPE,  ACCEPT, "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CustomUtil customUtil,
            @Qualifier(value = "corsConfig") CorsConfigurationSource source,
            @Qualifier(value = "authEntryPoint") AuthenticationEntryPoint authEntry,
            @Qualifier(value = "strategy") SessionAuthenticationStrategy strategy,
            @Qualifier(value = "sessionRegistry") SessionRegistry sessionRegistry
    ) throws Exception {
        String JSESSIONID = this.environment.getProperty("server.servlet.session.cookie.name");
        String LOGGEDSESSION = this.environment.getProperty("custom.cookie.frontend");

        return http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/v1/logout")
                        .csrfTokenRepository(new CookieCsrfTokenRepository())
                )
                .cors(cors -> cors.configurationSource(source))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/api/v1/csrf/**",
                            "/api/v1/client/auth/register",
                            "/api/v1/client/auth/login",
                            "/api/v1/client/product/**",
                            "/api/v1/client/category/**",
                            "/api/v1/client/collection/**",
                            "/api/v1/worker/auth/login"
                    ).permitAll();
                    auth.anyRequest().authenticated();
                })
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(IF_REQUIRED) //
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::newSession) //
                        .sessionAuthenticationStrategy(strategy)
                        .maximumSessions(customUtil.getMaxSession())
                        .sessionRegistry(sessionRegistry)
                )
                .exceptionHandling((ex) -> ex.authenticationEntryPoint(authEntry))
                // https://docs.spring.io/spring-security/reference/servlet/authentication/logout.html
                .logout(logout -> logout
                        .logoutUrl("/api/v1/logout")
                        .invalidateHttpSession(true)
                        .addLogoutHandler(new CookieClearingLogoutHandler(JSESSIONID, LOGGEDSESSION))
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                        .logoutSuccessHandler((request, response, authentication) ->
                                SecurityContextHolder.clearContext()
                        )
                )
                .build();
    }

    /** A SecurityContextRepository implementation which stores the security context in the HttpSession between requests. */
    @Bean(name = "contextRepository")
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

}
