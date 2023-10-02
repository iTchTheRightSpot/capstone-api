package com.sarabrandserver.product.entity;

import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.collection.entity.ProductCollection;
import com.sarabrandserver.order.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Table(name = "product", indexes = @Index(name = "IX_product_uuid", columnList = "uuid"))
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

    @Column(name = "uuid", length = 36, nullable = false, unique = true, updatable = false)
    private String uuid;

    @Column(name = "name", length = 50, unique = true, nullable = false)
    private String name;

    @Column(name = "description", length = 400, nullable = false)
    private String description;

    @Column(name = "default_image_key", nullable = false)
    private String defaultKey;

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

    @OneToMany(cascade = ALL, fetch = LAZY, mappedBy = "product", orphanRemoval = true)
    private Set<ProductDetail> productDetails;

}
