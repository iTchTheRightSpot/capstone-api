package dev.webserver.cart;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
        Long sessionId,
        @NotNull(message = "shopping_session cookie cannot be null")
        @NotEmpty(message = "shopping_session cookie cannot be empty")
        @Size.List({@Size(max = 100, message = "shopping_session cookie max length of 100")})
        String cookie,
        @Column("created_at")
        LocalDateTime createdAt,
        @NotNull(message = "shopping_session expire_at cannot be null")
        @Column("expire_at")
        LocalDateTime expireAt // indexed
) {
}