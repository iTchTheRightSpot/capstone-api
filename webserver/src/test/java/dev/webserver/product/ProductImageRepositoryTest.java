package dev.webserver.product;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.TestUtility;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.RepositoryTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;

final class ProductImageRepositoryTest extends AbstractRepositoryTest {

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
    private ProductPriceCurrencyRepository productPriceCurrencyRepository;

    @Test
    void imagesByProductDetailId() {
        // given
        final var cat = categoryRepo
                .save(Category.builder()
                        .name("category")
                        .isVisible(true)
                        .build());
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        // when
        final var details = TestUtility.toList(detailRepo.findAll());
        assertFalse(details.isEmpty());

        // then
        final var images = imageRepo
                .imagesByProductDetailId(details.getFirst().detailId());

        assertFalse(images.isEmpty());
    }

}