package dev.webserver.category;

import dev.webserver.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Table(name = "product_category")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductCategory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false, unique = true)
    private Long categoryId;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "is_visible")
    private boolean isVisible;

    @ManyToOne
    @JoinColumn(name = "parent_category_id", referencedColumnName = "category_id")
    private ProductCategory parentCategory;

    @OneToMany(cascade = ALL, fetch = LAZY, mappedBy = "parentCategory", orphanRemoval = true)
    private Set<ProductCategory> categories;

    @OneToMany(cascade = ALL, fetch = LAZY, mappedBy = "productCategory")
    private Set<Product> product;

}
