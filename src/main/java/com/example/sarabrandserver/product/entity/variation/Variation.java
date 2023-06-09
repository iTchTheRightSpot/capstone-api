package com.example.sarabrandserver.product.entity.variation;

import com.example.sarabrandserver.product.entity.category.entity.ProductCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Table(name = "variation")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Variation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variation_id", nullable = false, unique = true)
    private Long variationId;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private ProductCategory productCategory;

    @OneToMany(cascade = ALL, fetch = LAZY, mappedBy = "variation", orphanRemoval = true)
    private Set<VariationOption> variationOptions = new HashSet<>();

    public void addVariationOption(VariationOption option) {
        this.variationOptions.add(option);
        option.setVariation(this);
    }

}
