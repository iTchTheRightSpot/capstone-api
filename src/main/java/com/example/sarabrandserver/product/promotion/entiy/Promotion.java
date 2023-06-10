package com.example.sarabrandserver.product.promotion.entiy;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

/**
 * Look at the link as far as understanding choice of date
 * <a href="https://vladmihalcea.com/date-timestamp-jpa-hibernate/">...</a>
 * */
@Table(name = "promotion")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Promotion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id", nullable = false, unique = true)
    private Long promotionId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "discount_rate", nullable = false)
    private BigDecimal discountRate;

    @Column(name = "start_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "promotion", orphanRemoval = true)
    private Set<ProductPromotion> promotionCategories = new HashSet<>();

    public void addPromotionCategory(ProductPromotion category) {
        this.promotionCategories.add(category);
        category.setPromotion(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Promotion promotion)) return false;
        return Objects.equals(getPromotionId(), promotion.getPromotionId())
                && Objects.equals(getName(), promotion.getName())
                && Objects.equals(getDescription(), promotion.getDescription())
                && Objects.equals(getDiscountRate(), promotion.getDiscountRate())
                && Objects.equals(getStartDate(), promotion.getStartDate())
                && Objects.equals(getEndDate(), promotion.getEndDate())
                && Objects.equals(getPromotionCategories(), promotion.getPromotionCategories());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPromotionId(),
                getName(),
                getDescription(),
                getDiscountRate(),
                getStartDate(),
                getEndDate(),
                getPromotionCategories()
        );
    }
}
