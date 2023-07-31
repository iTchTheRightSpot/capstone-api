package com.emmanuel.sarabrandserver.security;

import com.emmanuel.sarabrandserver.util.CustomUtil;
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
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final FindByIndexNameSessionRepository<? extends Session> indexedRepository;
    private final LogoutHandler logoutHandler;

    public SecurityConfig(
            Environment environment,
            FindByIndexNameSessionRepository<? extends Session> repo,
            @Qualifier(value = "customLogoutHandler") LogoutHandler logoutHandler
    ) {
        this.environment = environment;
        this.indexedRepository = repo;
        this.logoutHandler = logoutHandler;
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
        var domain = this.environment.getProperty("${server.servlet.session.cookie.domain}");
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setCookieName("JSESSIONID");
        cookieSerializer.setUseHttpOnlyCookie(true);
        cookieSerializer.setUseSecureCookie(true);
        cookieSerializer.setCookiePath("/");
        cookieSerializer.setSameSite("lax");
        cookieSerializer.setCookieMaxAge(3600);
        cookieSerializer.setDomainName(domain);

        var property = Optional.ofNullable(this.environment.getProperty("spring.profiles.active"));
        if (property.isPresent() && (property.get().equals("dev") || property.get().equals("test"))) {
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

        var property = Optional.ofNullable(this.environment.getProperty("spring.profiles.active"));
        if (property.isPresent() && (property.get().equals("dev") || property.get().equals("test"))) {
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

    /**
     * Security filter chain responsible for upholding app security
     * Reason for Consumer<ResponseCookie.ResponseCookieBuilder> as per docs secure, domain name and path are deprecated
     * <a href="https://github.com/spring-projects/spring-security/blob/main/web/src/main/java/org/springframework/security/web/csrf/CookieCsrfTokenRepository.java">...</a>
     * */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CustomUtil customUtil,
            CorsConfigurationSource corsConfig,
            @Qualifier(value = "authEntryPoint") AuthenticationEntryPoint authEntry
    ) throws Exception {
        String JSESSIONID = this.environment.getProperty("server.servlet.session.cookie.name");
        String DOMAIN = this.environment.getProperty("server.servlet.session.cookie.domain");

        Consumer<ResponseCookie.ResponseCookieBuilder> csrfCookieCustomizer = cookie -> cookie
                .domain(DOMAIN)
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(-1);
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieCustomizer(csrfCookieCustomizer);

        return http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .cors(cors -> cors.configurationSource(corsConfig))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
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
                        .sessionCreationPolicy(IF_REQUIRED) //
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::newSession) //
                        .maximumSessions(customUtil.getMaxSession())
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
     * Maintains a registry of Session information instances. For better understanding visit
     * <a href="https://github.com/spring-projects/spring-session/blob/main/spring-session-docs/modules/ROOT/examples/java/docs/security/SecurityConfiguration.java">...</a>
     * **/
    @Bean(name = "sessionRegistry")
    public SpringSessionBackedSessionRegistry<? extends Session> sessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(indexedRepository);
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /** A SecurityContextRepository implementation which stores the security context in the HttpSession between requests. */
    @Bean(name = "contextRepository")
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

}
