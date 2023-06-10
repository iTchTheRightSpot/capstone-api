package com.example.sarabrandserver.order.entity;

import com.example.sarabrandserver.product.entity.Product;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

@Table(name = "order_item")
@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class OrderItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id", nullable = false, unique = true)
    private Long orderItemId;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column(name = "modified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "order_detail_id", referencedColumnName = "order_detail_id", nullable = false)
    private OrderDetail orderDetail;

    @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "orderItem", orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

}
