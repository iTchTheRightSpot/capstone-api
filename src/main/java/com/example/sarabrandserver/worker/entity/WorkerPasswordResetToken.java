package com.example.sarabrandserver.worker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Table(name = "worker_password_reset_token")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class WorkerPasswordResetToken implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reset_id", nullable = false, unique = true)
    private Long resetId;

    @Column(name = "token", unique = true, length = 80)
    private String token;

    @OneToOne(mappedBy = "token")
    private Worker worker;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerPasswordResetToken that)) return false;
        return Objects.equals(getResetId(), that.getResetId())
                && Objects.equals(getToken(), that.getToken())
                && Objects.equals(getWorker(), that.getWorker());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getResetId(), getToken(), getWorker());
    }
}
