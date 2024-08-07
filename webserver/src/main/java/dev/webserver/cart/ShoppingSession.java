package dev.webserver.cart;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "shopping_session")
@Builder
public record ShoppingSession(
        @Id
        @Column("session_id")
        Long shoppingSessionId,
        String cookie,
        @Column("created_at")
        LocalDateTime createAt,
        @Column("expire_at")
        LocalDateTime expireAt
) {
}