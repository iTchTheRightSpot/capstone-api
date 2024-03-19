<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/product/entity/ProductDetail.java
package dev.webserver.product.entity;
========
package dev.capstone.product.entity;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/product/entity/ProductDetail.java

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Table(name = "product_detail")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id", nullable = false, unique = true)
    private Long productDetailId;

    @Column(name = "colour", nullable = false, length = 100)
    private String colour;

    @Column(name = "is_visible")
    private boolean isVisible;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false)
    private Product product;

    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "productDetails", orphanRemoval = true)
    private Set<ProductImage> productImages;

    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "productDetail", orphanRemoval = true)
    private Set<ProductSku> skus;

}
