package com.sarabrandserver.user.entity;

import com.sarabrandserver.enumeration.RoleEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "client_role")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class ClientRole implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_role_id", nullable = false, unique = true)
    private Long roleId;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    private RoleEnum role;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false, referencedColumnName = "client_id")
    private SarreBrandUser sarreBrandUser;

    public ClientRole(RoleEnum roleEnum) {
        this.role = roleEnum;
    }

}
