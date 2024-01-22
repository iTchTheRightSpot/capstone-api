package com.sarabrandserver.product.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.payment.entity.OrderDetail;
import com.sarabrandserver.payment.repository.OrderDetailRepository;
import com.sarabrandserver.product.service.WorkerProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ProductSkuRepoTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private WorkerProductService productService;
    @Autowired
    private ProductSkuRepo skuRepo;
    @Autowired
    private OrderDetailRepository orderRepository;

    @Test
    void itemBeenBought() {
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        productService
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());
        var sku = skus.getFirst();

        assertEquals(0, skuRepo.skuHasBeenPurchased(sku.getSku()));
        orderRepository
                .save(new OrderDetail(sku.getSku(), sku.getInventory(), null));

        assertEquals(1, skuRepo.skuHasBeenPurchased(sku.getSku()));
    }

    @Test
    void itemContainsCart() {
    }

    @Test
    void updateInventoryOnMakingReservation() {
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        productService
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());
        var sku = skus.getFirst();

        assertNotEquals(0, sku.getInventory());

        skuRepo.updateInventoryOnMakingReservation(sku.getSku(), sku.getInventory());

        var optional = skuRepo.findBySku(sku.getSku());
        assertFalse(optional.isEmpty());
        assertEquals(0, optional.get().getInventory());
    }

    @Test
    void updateInventory() {
        var cat = categoryRepo
                .save(ProductCategory.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());

        productService
                .create(
                        TestData.createProductDTO(
                                new Faker().commerce().productName(),
                                cat.getCategoryId(),
                                TestData.sizeInventoryDTOArray(3)
                        ),
                        TestData.files()
                );

        var skus = skuRepo.findAll();
        assertFalse(skus.isEmpty());
        var sku = skus.getFirst();

        assertNotEquals(0, sku.getInventory());

        skuRepo.incrementInventory(sku.getSku(), sku.getInventory());

        var optional = skuRepo.findBySku(sku.getSku());
        assertFalse(optional.isEmpty());
        assertTrue(optional.get().getInventory() > sku.getInventory());
    }

}