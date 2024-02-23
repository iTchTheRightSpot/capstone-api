package com.sarabrandserver.product.repository;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.product.entity.ProductImage;
import com.sarabrandserver.product.service.WorkerProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Transactional
class ProductImageRepoTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private WorkerProductService productService;
    @Autowired
    private ProductImageRepo imageRepo;
    @Autowired
    private ProductDetailRepo detailRepo;

    @Test
    void imagesByProductDetailId() {
        // given
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

        // when
        var details = detailRepo.findAll();
        assertFalse(details.isEmpty());

        // then
        var images = imageRepo
                .imagesByProductDetailId(details.getFirst().getProductDetailId());

        assertFalse(images.isEmpty());
    }

}