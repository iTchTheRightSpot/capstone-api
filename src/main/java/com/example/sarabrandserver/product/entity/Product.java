package com.example.sarabrandserver.product.entity;

import com.example.sarabrandserver.cart.entity.ShoppingSession;
import com.example.sarabrandserver.category.entity.ProductCategory;
import com.example.sarabrandserver.collection.entity.ProductCollection;
import com.example.sarabrandserver.order.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
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

    @Column(name = "name", length = 80, unique = true, nullable = false)
    private String name;

    @Column(name = "default_image_path", nullable = false)
    private String defaultImagePath;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "currency", nullable = false, length = 50)
    private String currency;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private ProductCategory productCategory;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "collection_id", referencedColumnName = "collection_id")
    private ProductCollection productCollection;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "session_id", referencedColumnName = "session_id")
    private ShoppingSession shoppingSession;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_item_id", referencedColumnName = "order_item_id")
    private OrderItem orderItem;

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "product", orphanRemoval = true)
    private Set<ProductDetail> productDetails;

    public Product(String name, String defaultImagePath) {
        this.name = name;
        this.defaultImagePath = defaultImagePath;
    }

    public void addDetails(ProductDetail detail) {
        this.productDetails.add(detail);
        detail.setProduct(this);
    }

}
