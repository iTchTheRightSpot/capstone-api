package com.example.sarabrandserver.user.auth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ClientConfig {
    private final UserDetailsService clientDetailService;
    private final PasswordEncoder passwordEncoder;

    public ClientConfig(
            @Qualifier(value = "clientDetailService") UserDetailsService clientDetailService,
            PasswordEncoder passwordEncoder
    ) {
        this.clientDetailService = clientDetailService;
        this.passwordEncoder = passwordEncoder;
    }


    @Bean(name = "clientAuthProvider")
    public AuthenticationProvider clientAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(this.passwordEncoder);
        provider.setUserDetailsService(this.clientDetailService);
        return provider;
    }

}
