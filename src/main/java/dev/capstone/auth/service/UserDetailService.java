<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/auth/service/UserDetailService.java
package dev.webserver.auth.service;

import dev.webserver.user.repository.UserRepository;
========
package dev.capstone.auth.service;

import dev.capstone.user.repository.UserRepository;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/auth/service/UserDetailService.java
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
