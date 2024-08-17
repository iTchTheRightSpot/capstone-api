package dev.webserver.user;

import dev.webserver.enumeration.RoleEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "role")
@Builder
public record Role(
        @Id
        @Column("role_id")
        Long roleId,
        RoleEnum role,
        @NotNull(message = "role user_id cannot be null")
        @Column("user_id")
        Long userId
) {
}
