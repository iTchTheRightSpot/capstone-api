<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/auth/service/UserDetailz.java
package dev.webserver.auth.service;

import dev.webserver.user.entity.SarreBrandUser;
========
package dev.capstone.auth.service;

import dev.capstone.user.entity.SarreBrandUser;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/auth/service/UserDetailz.java
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

record UserDetailz(SarreBrandUser user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user
                .getClientRole()
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.role().toString()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

}
