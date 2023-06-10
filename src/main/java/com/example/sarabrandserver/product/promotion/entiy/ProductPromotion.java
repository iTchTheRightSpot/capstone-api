package com.example.sarabrandserver.product.promotion.entiy;

import com.example.sarabrandserver.product.entity.ProductItem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Table(name = "product_promotion")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class ProductPromotion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_promotion_id", nullable = false, unique = true)
    private Long productPromotionId;

    @ManyToOne
    @JoinColumn(name = "product_item_id", referencedColumnName = "product_item_id")
    private ProductItem productItem;

    @ManyToOne
    @JoinColumn(name = "promotion_id", referencedColumnName = "promotion_id")
    private Promotion promotion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductPromotion that)) return false;
        return Objects.equals(getProductPromotionId(), that.getProductPromotionId())
                && Objects.equals(getProductItem(), that.getProductItem())
                && Objects.equals(getPromotion(), that.getPromotion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProductPromotionId(), getProductItem(), getPromotion());
    }

}
