package com.emmanuel.sarabrandserver.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

@Component(value = "customLogoutHandler")
public class CustomLogoutHandler implements LogoutHandler {

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    public CustomLogoutHandler(FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void logout(HttpServletRequest req, HttpServletResponse res, Authentication auth) {
        String id = req.getSession(false).getId();
        if (id != null && this.sessionRepository.findById(id) != null) {
            this.sessionRepository.deleteById(id);
        }
    }

}
