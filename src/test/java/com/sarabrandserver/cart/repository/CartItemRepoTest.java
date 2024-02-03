package com.sarabrandserver.cart.repository;

import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import com.sarabrandserver.product.service.WorkerProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class CartItemRepoTest extends AbstractRepositoryTest {

    @Autowired
    private ShoppingSessionRepo sessionRepo;
    @Autowired
    private CartItemRepo cartItemRepo;
    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private WorkerProductService service;
    @Autowired
    private ProductSkuRepo skuRepo;

    @Test
    void cart_item_by_shopping_session_id_and_sku() {
    }

    @Test
    void updateCartQtyByCartId() {
    }

    @Test
    void delete_cartItem_by_cookie_and_sku() {
    }

    @Test
    void deleteByParentID() {
    }

    @Test
    void cart_items_by_shopping_session_cookie() {
    }

    @Test
    void total_amount_in_default_currency() {
    }
}