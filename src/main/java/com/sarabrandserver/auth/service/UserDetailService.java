package com.sarabrandserver.auth.service;

import com.sarabrandserver.user.entity.UserDetailz;
import com.sarabrandserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return this.userRepository
                .findByPrincipal(email)
                .map(UserDetailz::new)
                .orElseThrow(() -> new UsernameNotFoundException(email + " not found"));
    }

}
