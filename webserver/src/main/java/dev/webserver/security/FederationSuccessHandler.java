package dev.webserver.security;

import dev.webserver.AbstractEnvironment;
import dev.webserver.external.log.ILogEventPublisher;
import dev.webserver.user.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.function.BiConsumer;

import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated;

/**
 * Implementation for successful authentication by social login.
 * */
final class FederationSuccessHandler extends AbstractEnvironment implements AuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(FederationSuccessHandler.class);

    private final StringBuilder redirect = new StringBuilder();

    private final AuthenticationSuccessHandler delegate;
    private final BiConsumer<HttpServletResponse, OAuth2User> oauth2UserHandler;
    private final BiConsumer<HttpServletResponse, OidcUser> oidcUserHandler;

    public FederationSuccessHandler(
            final Environment environment,
            final SavedRequestAwareAuthenticationSuccessHandler successHandler,
            final JwtService jwtService,
            final UserService userService,
            final ILogEventPublisher publisher
    ) {
        super(environment);
        delegate = successHandler;

        oauth2UserHandler = (response, auth2User) -> {
            final String fullname = auth2User.getAttribute("name"); // concatenation of first and last names
            final String firstname =  auth2User.getAttribute("given_name"); // firstname
            final String email = auth2User.getAttribute("email");
            final String picture = auth2User.getAttribute("picture");

            redirect.setLength(0);

            try {
                final var details = userService.create(fullname, firstname, email, picture);

                final String jwt = jwtService.generateJwt(authenticated(details, null, details.getAuthorities()));

                cookieHandler.accept(jwt, response);
                publisher.publishSignInOrRegistration(firstname, email);
                redirect.append(uiRedirect);
            } catch (Exception e) {
                publisher.publishRegistrationException(firstname, email);
                redirect.append(mailExceptionRedirect);
                log.error("exception in authentication success handler: {}", e.getMessage());
            }
        };
        oidcUserHandler = oauth2UserHandler::accept;
    }

    private final BiConsumer<String, HttpServletResponse> cookieHandler = (jwt, response) -> {
        final Cookie cookie = new Cookie(jsessionid, jwt);
        cookie.setPath(cookiepath);
        cookie.setMaxAge(cookiemaxage);
        cookie.setHttpOnly(true);
        cookie.setSecure(!activeprofile.endsWith("test"));
        response.addCookie(cookie);
    };

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            if (authentication.getPrincipal() instanceof OidcUser) { // OIDC_USER represents google auth
                oidcUserHandler.accept(response, (OidcUser) authentication.getPrincipal());
            } else if (authentication.getPrincipal() instanceof OAuth2User) {
                oauth2UserHandler.accept(response, (OAuth2User) authentication.getPrincipal());
            }
        }

        response.sendRedirect(redirect.toString());
        delegate.onAuthenticationSuccess(request, response, authentication);
    }

}
