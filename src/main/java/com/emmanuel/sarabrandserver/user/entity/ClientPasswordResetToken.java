package com.emmanuel.sarabrandserver.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "client_password_reset_token")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class ClientPasswordResetToken implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reset_id", nullable = false, unique = true)
    private Long resetId;

    @Column(name = "token", unique = true, length = 80)
    private String token;

    @OneToOne(mappedBy = "token")
    private Clientz clientz;

}
