package com.emmanuel.sarabrandserver.user.entity;

import com.emmanuel.sarabrandserver.user.entity.SaraBrandUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public record ClientzDetail(SaraBrandUser saraBrandUser) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.saraBrandUser.getAuthorities();
    }

    @Override
    public String getPassword() {
        return this.saraBrandUser.getPassword();
    }

    @Override
    public String getUsername() {
        return this.saraBrandUser.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.saraBrandUser.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.saraBrandUser.isAccountNoneLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.saraBrandUser.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return this.saraBrandUser.isEnabled();
    }
}
