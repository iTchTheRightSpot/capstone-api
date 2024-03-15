package dev.capstone.auth.service;

import dev.capstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return this.repository
                .userByPrincipal(email)
                .map(UserDetailz::new)
                .orElseThrow(() -> new UsernameNotFoundException(email + " not found"));
    }

}
