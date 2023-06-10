package com.example.sarabrandserver.product.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "product_colour")
@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ProductColour implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_colour_id", nullable = false, unique = true)
    private Long productColourId;

    @Column(name = "colour", nullable = false, length = 20)
    private String colour;

    @ManyToOne
    @JoinColumn(name = "product_detail_id", referencedColumnName = "product_detail_id")
    private ProductDetail productDetail;

    public ProductColour(String colour) {
        this.colour = colour;
    }
}
