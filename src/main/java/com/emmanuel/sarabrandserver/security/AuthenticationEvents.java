package com.emmanuel.sarabrandserver.security;

import com.emmanuel.sarabrandserver.security.bruteforce.BruteForceService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEvents {

    private final BruteForceService bruteForceService;

    public AuthenticationEvents(BruteForceService bruteForceService) {
        this.bruteForceService = bruteForceService;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        bruteForceService.resetBruteForceCounter(success.getAuthentication());
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        bruteForceService.loginFailure(failures.getAuthentication());
    }

}
