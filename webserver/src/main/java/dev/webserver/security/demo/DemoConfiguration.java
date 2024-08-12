package dev.webserver.security.demo;

import dev.webserver.security.UserDetailz;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
class DemoConfiguration {

    private static final PasswordEncoder encoder = new BCryptPasswordEncoder(10);
    private static final String hash = encoder.encode(DemoUser.password);

    @Bean
    public AuthenticationManager demoAuthenticationManager() {
        record DemoAuthenticationManager() implements AuthenticationManager {
            @Override
            public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
                if (!DemoUser.demo.email().equals(authentication.getPrincipal())) return null;
                else if (encoder.matches((String) authentication.getCredentials(), hash)) {
                    final var details = new UserDetailz(DemoUser.demo, DemoUser.roles);
                    return UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
                }
                return null;
            }
        }

        return new DemoAuthenticationManager();
    }

}
