package com.example.sarabrandserver.clientz.auth;

import com.example.sarabrandserver.clientz.repository.ClientRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service(value = "clientDetailService")
public class ClientDetailService implements UserDetailsService {

    private final ClientRepository clientRepository;

    public ClientDetailService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.clientRepository.findByPrincipal(username).map(ClientzDetail::new)
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found"));
    }

}
