package dev.webserver.user;

import dev.webserver.enumeration.RoleEnum;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "client_role")
@Builder
public record ClientRole(

        @Id
        @Column("role_id")
        Long roleId,
        RoleEnum role,
        @Column("client_id")
        SarreBrandUser userId
) {
}
