package com.emmanuel.sarabrandserver.user.service;

import com.emmanuel.sarabrandserver.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ClientDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public ClientDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findByPrincipal(username).map(ClientzDetail::new)
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found"));
    }

}
