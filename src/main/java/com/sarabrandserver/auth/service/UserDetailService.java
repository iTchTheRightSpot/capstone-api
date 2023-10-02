package com.sarabrandserver.auth.service;

import com.sarabrandserver.user.entity.UserDetailz;
import com.sarabrandserver.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return this.userRepository
                .findByPrincipal(email)
                .map(UserDetailz::new)
                .orElseThrow(() -> new UsernameNotFoundException(email + " not found"));
    }

}
