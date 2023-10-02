package com.sarabrandserver.user.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public record UserDetailz(SarreBrandUser sarreBrandUser) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.sarreBrandUser.getAuthorities();
    }

    @Override
    public String getPassword() {
        return this.sarreBrandUser.getPassword();
    }

    @Override
    public String getUsername() {
        return this.sarreBrandUser.getEmail();
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
        return this.sarreBrandUser.isEnabled();
    }

}
