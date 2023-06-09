package com.example.sarabrandserver.product.entity;

import com.example.sarabrandserver.product.entity.variation.VariationOption;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "product_configuration")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class ProductConfiguration implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_configuration_id", nullable = false, unique = true)
    private Long productConfigurationId;

    @ManyToOne
    @JoinColumn(name = "product_item_id", referencedColumnName = "product_item_id")
    private ProductItem productItem;

    @ManyToOne
    @JoinColumn(name = "variation_option_id", referencedColumnName = "variation_option_id")
    private VariationOption variationOption;

}
