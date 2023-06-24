package com.emmanuel.sarabrandserver.clientz.service;

import com.emmanuel.sarabrandserver.clientz.repository.ClientzRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service(value = "clientDetailService")
public class ClientDetailService implements UserDetailsService {

    private final ClientzRepository clientzRepository;

    public ClientDetailService(ClientzRepository clientzRepository) {
        this.clientzRepository = clientzRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.clientzRepository.findByPrincipal(username).map(ClientzDetail::new)
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found"));
    }

}
