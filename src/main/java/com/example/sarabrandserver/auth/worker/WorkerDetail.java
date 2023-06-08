package com.example.sarabrandserver.auth.worker;

import com.example.sarabrandserver.worker.entity.Worker;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public record WorkerDetail(Worker worker) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.worker.getAuthorities();
    }

    @Override
    public String getPassword() {
        return this.worker.getPassword();
    }

    @Override
    public String getUsername() {
        return this.worker.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.worker.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.worker.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.worker.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return this.worker.isEnabled();
    }
}
