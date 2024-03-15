package dev.capstone.product.repository;

import dev.capstone.AbstractRepositoryTest;
import dev.capstone.category.entity.ProductCategory;
import dev.capstone.category.repository.CategoryRepository;
import dev.capstone.data.RepositoryTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Transactional
class ProductImageRepoTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductSkuRepo skuRepo;
    @Autowired
    private ProductImageRepo imageRepo;
    @Autowired
    private ProductDetailRepo detailRepo;
    @Autowired
    private PriceCurrencyRepo priceCurrencyRepo;

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
        RepositoryTestData
                .createProduct(3, cat, productRepo, detailRepo, priceCurrencyRepo, imageRepo, skuRepo);

        // when
        var details = detailRepo.findAll();
        assertFalse(details.isEmpty());

        // then
        var images = imageRepo
                .imagesByProductDetailId(details.getFirst().getProductDetailId());

        assertFalse(images.isEmpty());
    }

}