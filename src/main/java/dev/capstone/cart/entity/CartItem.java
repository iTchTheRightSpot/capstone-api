package dev.capstone.cart.entity;

import dev.capstone.product.entity.ProductSku;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "cart_item")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id", nullable = false, unique = true)
    private Long cartId;

    @Column(nullable = false)
    private int qty;

    @ManyToOne
    @JoinColumn(name = "session_id", referencedColumnName = "session_id", nullable = false)
    private ShoppingSession shoppingSession;

    @ManyToOne
    @JoinColumn(name = "sku_id", referencedColumnName = "sku_id", nullable = false)
    private ProductSku productSku;

    public CartItem(int qty, ShoppingSession session, ProductSku sku) {
        this.qty = qty;
        this.shoppingSession = session;
        this.productSku = sku;
    }

    public boolean quantityIsGreaterThanProductSkuInventory() {
        return this.qty > this.productSku.getInventory();
    }

}
