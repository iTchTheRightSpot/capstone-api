package com.emmanuel.sarabrandserver.auth.config;

import com.emmanuel.sarabrandserver.util.CustomUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.session.*;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED;

/** API docs using session <a href="https://docs.spring.io/spring-session/reference/api.html">...</a> */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {
    private final Environment environment;
    private final LogoutHandler logoutHandler;
    private final CustomUtil customUtil;

    public SecurityConfig(
            Environment environment,
            @Qualifier(value = "customLogoutHandler") LogoutHandler logoutHandler,
            CustomUtil customUtil
    ) {
        this.environment = environment;
        this.logoutHandler = logoutHandler;
        this.customUtil = customUtil;
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

    /** <a href="https://docs.spring.io/spring-session/reference/guides/java-custom-cookie.html">...</a> */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setCookieName("JSESSIONID");
        cookieSerializer.setUseHttpOnlyCookie(true);
        cookieSerializer.setUseSecureCookie(true);
        cookieSerializer.setCookiePath("/");
        cookieSerializer.setSameSite("lax");
        cookieSerializer.setCookieMaxAge(3600);
        cookieSerializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");

        var profile = this.environment.getProperty("spring.profiles.active", "");
        if (profile.equals("dev") || profile.equals("test")) {
            cookieSerializer.setUseHttpOnlyCookie(false);
            cookieSerializer.setUseSecureCookie(false);
            cookieSerializer.setCookieMaxAge(1800);
        }
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

        var profile = this.environment.getProperty("spring.profiles.active", "");
        if (profile.equals("dev") || profile.equals("test")) {
            allowOrigins.add("http://localhost:4200/");
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowOrigins);
        configuration.setAllowedMethods(List.of("GET", "PUT", "POST", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(CONTENT_TYPE, ACCEPT, "X-XSRF-TOKEN"));
        configuration.setExposedHeaders(List.of("X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /** Security filter chain responsible for upholding app security */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfig,
            @Qualifier(value = "authEntryPoint") AuthenticationEntryPoint authEntry,
            SessionAuthenticationStrategy strategy
    ) throws Exception {
        var JSESSIONID = this.environment.getProperty("server.servlet.session.cookie.name");
        var domain = this.environment.getProperty("server.servlet.session.cookie.domain");
        var profile = this.environment.getProperty("spring.profiles.active", "");
        var csrfTokenRepository = getCookieCsrfTokenRepository(domain, profile);

        return http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .cors(cors -> cors.configurationSource(corsConfig))
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
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionAuthenticationStrategy(strategy)
                        .sessionCreationPolicy(IF_REQUIRED) //
                )
                .exceptionHandling((ex) -> ex.authenticationEntryPoint(authEntry))
                // https://docs.spring.io/spring-security/reference/servlet/authentication/logout.html
                .logout((logoutConfig) -> logoutConfig
                        .logoutUrl("/api/v1/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies(JSESSIONID)
                        .addLogoutHandler(this.logoutHandler)
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                )
                .build();
    }

    /**
     * Reason for Consumer<ResponseCookie.ResponseCookieBuilder> as per docs secure, domain name and path are deprecated
     * As per docs
     * <a href="https://github.com/spring-projects/spring-security/blob/main/web/src/main/java/org/springframework/security/web/csrf/CookieCsrfTokenRepository.java">...</a>
     * */
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

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(
            SessionRegistry sessionRegistry,
            FindByIndexNameSessionRepository<? extends Session> sessionRepository
    ) {
        SessionAuthenticationStrategy mitigate = new ChangeSessionIdAuthenticationStrategy();

        ConcurrentSessionControlAuthenticationStrategy check =
                new CustomConcurrentSession(sessionRepository, sessionRegistry, customUtil);
        check.setMaximumSessions(this.customUtil.getMaxSession());

        RegisterSessionAuthenticationStrategy register =
                new RegisterSessionAuthenticationStrategy(sessionRegistry);

        return new CompositeSessionAuthenticationStrategy(List.of(check, mitigate, register, check));
    }

    /**
     * Used to put a constraint on number of active sessions a user has. FindByIndexNameSessionRepository is needed to
     * manually delete users session as inbuilt config SessionInformation.expireNow is not does not expire user session.
     * */
    private static class CustomConcurrentSession extends ConcurrentSessionControlAuthenticationStrategy {
        private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
        private final SessionRegistry sessionRegistry;
        private final CustomUtil customUtil;

        public CustomConcurrentSession(
                FindByIndexNameSessionRepository<? extends Session> sessionRepository,
                SessionRegistry sessionRegistry,
                CustomUtil customUtil
        ) {
            super(sessionRegistry);
            this.sessionRepository = sessionRepository;
            this.sessionRegistry = sessionRegistry;
            this.customUtil = customUtil;
        }

        @Override
        public void onAuthentication(
                Authentication authentication,
                HttpServletRequest request,
                HttpServletResponse response
        ) {
            int allowedSession = getMaximumSessionsForThisUser(authentication);

            if (allowedSession == -1 || allowedSession < customUtil.getMaxSession()) {
                return;
            }

            sessionRegistry
                    .getAllSessions(authentication.getPrincipal(), false)
                    .stream() //
                    .min(Comparator.comparing(SessionInformation::getLastRequest)) // Gets the oldest session
                    .ifPresent(sessionInfo -> sessionRepository.deleteById(sessionInfo.getSessionId()));
        }

    }

}
