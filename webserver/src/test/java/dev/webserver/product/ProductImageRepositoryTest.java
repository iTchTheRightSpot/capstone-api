package dev.webserver.product;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ProductImageRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductSkuRepository skuRepo;
    @Autowired
    private ProductImageRepository imageRepo;
    @Autowired
    private ProductDetailRepository detailRepo;
    @Autowired
    private PriceCurrencyRepository priceCurrencyRepository;

    @Test
    void imagesByProductDetailId() {
        // given
        var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .categories(new HashSet<>())
                        .product(new HashSet<>())
                        .build());
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, priceCurrencyRepository, imageRepo, skuRepo);

        // when
        var details = detailRepo.findAll();
        assertFalse(details.isEmpty());

        // then
        var images = imageRepo
                .imagesByProductDetailId(details.getFirst().getProductDetailId());

        assertFalse(images.isEmpty());
    }

}