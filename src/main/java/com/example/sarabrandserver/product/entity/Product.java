package com.example.sarabrandserver.product.entity;

import com.example.sarabrandserver.cart.entity.ShoppingSession;
import com.example.sarabrandserver.category.entity.ProductCategory;
import com.example.sarabrandserver.collection.entity.ProductCollection;
import com.example.sarabrandserver.order.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;

@Table(name = "product")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(name = "name", length = 25, unique = true, nullable = false)
    private String name;

    @Column(name = "default_image_path", nullable = false)
    private String defaultImagePath;

    @Column(name = "deleted_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedAt;

    @ManyToOne(fetch = LAZY) // Fetch type Lazy until business requirement changes
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private ProductCategory productCategory;

    @ManyToOne(fetch = LAZY) // Fetch type Lazy until business requirement changes
    @JoinColumn(name = "product_collection_id", referencedColumnName = "product_collection_id")
    private ProductCollection productCollection;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "session_id", referencedColumnName = "session_id")
    private ShoppingSession shoppingSession;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_item_id", referencedColumnName = "order_item_id")
    private OrderItem orderItem;

    @OneToMany(cascade = CascadeType.ALL, fetch = EAGER, mappedBy = "product", orphanRemoval = true)
    private Set<ProductDetail> productDetails;

    public Product(String name, String defaultImagePath) {
        this.name = name;
        this.defaultImagePath = defaultImagePath;
    }

    public void addProductDetail(ProductDetail detail) {
        this.productDetails.add(detail);
        detail.setProduct(this);
    }

}
