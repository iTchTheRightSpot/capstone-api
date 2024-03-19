<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/user/entity/ClientRole.java
package dev.webserver.user.entity;

import dev.webserver.enumeration.RoleEnum;
========
package dev.capstone.user.entity;

import dev.capstone.enumeration.RoleEnum;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/user/entity/ClientRole.java
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
