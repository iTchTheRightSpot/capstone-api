package com.emmanuel.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Table(name = "product_colour")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class ProductColour implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "colour_id", nullable = false, unique = true)
    private Long productColourId;

    @Column(name = "colour", nullable = false, length = 50)
    private String colour;

    @OneToMany(mappedBy = "productColour")
    private Set<ProductDetail> productDetails;

    public ProductColour(String colour) {
        this.colour = colour;
    }

}
