package com.example.sarabrandserver.worker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.EAGER;

@Table(name = "worker")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Worker implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "worker_id", nullable = false, unique = true)
    private Long workerId;

    @Column(name = "name")
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "credentials_none_expired", nullable = false)
    private boolean credentialsNonExpired;

    @Column(name = "account_none_expired", nullable = false)
    private boolean accountNonExpired;

    @Column(name = "locked", nullable = false)
    private boolean locked;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "reset_id", referencedColumnName = "reset_id")
    private WorkerPasswordResetToken token;

    @OneToMany(cascade = {PERSIST, MERGE, REMOVE}, fetch = EAGER, mappedBy = "worker", orphanRemoval = true)
    private Set<WorkerRole> workerRole = new HashSet<>();

    public Worker(
            String name,
            String email,
            String username,
            String password,
            boolean enabled,
            boolean credentialsNonExpired,
            boolean accountNonExpired,
            boolean locked
    ) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonExpired = accountNonExpired;
        this.locked = locked;
    }

    public void addRole(WorkerRole role) {
        this.workerRole.add(role);
        role.setWorker(this);
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this
                .workerRole
                .stream() //
                .map(role -> new SimpleGrantedAuthority(role.getRole().toString()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Worker worker)) return false;
        return isEnabled() == worker.isEnabled() &&
                isCredentialsNonExpired() == worker.isCredentialsNonExpired()
                && isAccountNonExpired() == worker.isAccountNonExpired()
                && isLocked() == worker.isLocked()
                && Objects.equals(getWorkerId(), worker.getWorkerId())
                && Objects.equals(getName(), worker.getName())
                && Objects.equals(getEmail(), worker.getEmail())
                && Objects.equals(getUsername(), worker.getUsername())
                && Objects.equals(getPassword(), worker.getPassword())
                && Objects.equals(getWorkerRole(), worker.getWorkerRole()
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getWorkerId(),
                getName(),
                getEmail(),
                getUsername(),
                getPassword(),
                isEnabled(),
                isCredentialsNonExpired(),
                isAccountNonExpired(),
                isLocked(),
                getWorkerRole()
        );
    }
}
