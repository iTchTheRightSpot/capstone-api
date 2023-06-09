package com.example.sarabrandserver.product.entity.variation;

import com.example.sarabrandserver.product.entity.ProductConfiguration;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

@Table(name = "variation_option")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class VariationOption implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variation_option_id", nullable = false, unique = true)
    private Long variationOptionId;

    @Column(name = "value", nullable = false)
    private int value;

    @ManyToOne
    @JoinColumn(name = "variation_id", referencedColumnName = "variation_id")
    private Variation variation;

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "variationOption", orphanRemoval = true)
    private Set<ProductConfiguration> productConfigurations = new HashSet<>();

}
