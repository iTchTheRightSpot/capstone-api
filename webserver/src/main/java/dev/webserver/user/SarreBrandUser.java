package dev.webserver.user;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "clientz")
@Builder
public record SarreBrandUser(
        @Id
        @Column("client_id")
        Long clientId,
        String firstname,
        String lastname,
        String email,
        @Column("phone_number")
        String phoneNumber,
        String password,
        boolean enabled
) {
}
