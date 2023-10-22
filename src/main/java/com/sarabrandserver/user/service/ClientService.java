package com.sarabrandserver.user.service;

import com.sarabrandserver.user.entity.SarreBrandUser;
import com.sarabrandserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final UserRepository userRepository;

    public Optional<SarreBrandUser> userByPrincipal(String principal) {
        return this.userRepository.findByPrincipal(principal);
    }

}
