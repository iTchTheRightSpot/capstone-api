package com.example.sarabrandserver.worker.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class WorkerConfig {

    private final UserDetailsService workerDetailService;

    private final PasswordEncoder passwordEncoder;

    public WorkerConfig(
            @Qualifier(value = "workerDetailService") UserDetailsService workerDetailService,
            PasswordEncoder passwordEncoder
    ) {
        this.workerDetailService = workerDetailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Primary
    @Bean(name = "workerAuthProvider")
    public AuthenticationProvider workerAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(this.passwordEncoder);
        provider.setUserDetailsService(this.workerDetailService);
        return provider;
    }

    @Primary
    @Bean(name = "workerAuthManager")
    public AuthenticationManager workerAuthManager() {
        return new ProviderManager(workerAuthProvider());
    }

}
