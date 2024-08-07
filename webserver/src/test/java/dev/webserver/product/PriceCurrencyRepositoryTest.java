package dev.webserver.product;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.data.RepositoryTestData;
import dev.webserver.enumeration.SarreCurrency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PriceCurrencyRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailRepository detailRepo;
    @Autowired
    private PriceCurrencyRepository priceCurrencyRepository;
    @Autowired
    private ProductImageRepository imageRepo;
    @Autowired
    private ProductSkuRepository skuRepo;
    @Autowired
    private PriceCurrencyRepository currencyRepo;

    @Test
    void priceCurrencyByProductUUIDAndCurrency() {
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
        var products = productRepository.findAll();
        assertFalse(products.isEmpty());

        // then
        var optional = currencyRepo
                .priceCurrencyByProductUuidAndCurrency(products.getFirst().getUuid(), SarreCurrency.NGN);
        assertFalse(optional.isEmpty());

        PriceCurrencyProjection pojo = optional.get();
        assertNotNull(pojo.getName());
        assertNotNull(pojo.getDescription());
        assertNotNull(pojo.getCurrency());
        assertNotNull(pojo.getPrice());
    }

    @Test
    void updateProductPriceByProductUuidAndCurrency() {
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
        var products = productRepository.findAll();
        assertFalse(products.isEmpty());

        // then
        currencyRepo
                .updateProductPriceByProductUuidAndCurrency(
                        products.getFirst().getUuid(),
                        new BigDecimal("10.52"),
                        SarreCurrency.USD
                );

        var optional = currencyRepo
                .priceCurrencyByProductUuidAndCurrency(products.getFirst().getUuid(), SarreCurrency.USD);
        assertFalse(optional.isEmpty());
        assertNotNull(optional.get().getPrice());
        Assertions.assertEquals(new BigDecimal("10.52"), optional.get().getPrice());
    }

}