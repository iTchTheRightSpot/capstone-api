package dev.webserver.category;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "category")
@Builder
public record Category(
        @Id
        @Column("category_id")
        Long categoryId,
        @NotNull(message = "category name cannot be null")
        @NotEmpty(message = "category name cannot be empty")
        @Size.List({@Size(max = 50, message = "category name max length of 50")})
        String name,
        @Column("is_visible")
        Boolean isVisible,
        @Column("parent_id")
        Long parentId
) {
}
