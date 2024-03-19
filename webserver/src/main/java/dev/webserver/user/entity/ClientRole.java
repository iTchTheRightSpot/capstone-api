package dev.webserver.user.entity;

import dev.webserver.enumeration.RoleEnum;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "client_role")
@Entity
@NoArgsConstructor
@Setter
public class ClientRole implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", nullable = false, unique = true)
    private Long roleId;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    private RoleEnum role;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false, referencedColumnName = "client_id")
    private SarreBrandUser sarreBrandUser;

    public ClientRole(RoleEnum role, SarreBrandUser sarreBrandUser) {
        this.role = role;
        this.sarreBrandUser = sarreBrandUser;
    }

    public Long roleId() {
        return roleId;
    }

    public RoleEnum role() {
        return role;
    }

    public SarreBrandUser user() {
        return sarreBrandUser;
    }

}
