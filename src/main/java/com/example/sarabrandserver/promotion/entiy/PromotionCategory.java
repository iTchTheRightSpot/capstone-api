package com.example.sarabrandserver.promotion.entiy;

import com.example.sarabrandserver.product.entity.category.entity.ProductCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Table(name = "promotion_category")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class PromotionCategory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_category_id", nullable = false, unique = true)
    private Long promotionCategoryId;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private ProductCategory productCategory;

    @ManyToOne
    @JoinColumn(name = "promotion_id", referencedColumnName = "promotion_id")
    private Promotion promotion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PromotionCategory that)) return false;
        return Objects.equals(getPromotionCategoryId(), that.getPromotionCategoryId())
                && Objects.equals(getProductCategory(), that.getProductCategory())
                && Objects.equals(getPromotion(), that.getPromotion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPromotionCategoryId(), getProductCategory(), getPromotion());
    }

}
