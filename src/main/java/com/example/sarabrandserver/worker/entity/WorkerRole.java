package com.example.sarabrandserver.worker.entity;

import com.example.sarabrandserver.enumeration.RoleEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "worker_role")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class WorkerRole implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "worker_role_id", nullable = false, unique = true)
    private Long roleId;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    @ManyToOne
    @JoinColumn(name = "worker_id", nullable = false, referencedColumnName = "worker_id")
    private Worker worker;

    public WorkerRole(RoleEnum roleEnum) {
        this.role = roleEnum;
    }
}
