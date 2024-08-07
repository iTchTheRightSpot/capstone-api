package dev.webserver.category;

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
        String name,
        @Column("is_visible")
        boolean isVisible,
        @Column("parent_id")
        Long parentId
) {
}
