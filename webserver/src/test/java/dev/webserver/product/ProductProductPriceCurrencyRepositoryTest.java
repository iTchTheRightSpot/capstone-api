package dev.webserver.product;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.TestUtility;
import dev.webserver.category.Category;
import dev.webserver.category.CategoryRepository;
import dev.webserver.RepositoryTestData;
import dev.webserver.enumeration.CapstoneCurrency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductProductPriceCurrencyRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailRepository detailRepo;
    @Autowired
    private ProductPriceCurrencyRepository productPriceCurrencyRepository;
    @Autowired
    private ProductImageRepository imageRepo;
    @Autowired
    private ProductSkuRepository skuRepo;
    @Autowired
    private ProductPriceCurrencyRepository currencyRepo;

    @Test
    void priceCurrencyByProductUUIDAndCurrency() {
        // given
        var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());

        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = TestUtility.toList(productRepository.findAll());
        assertFalse(products.isEmpty());

        // then
        var optional = currencyRepo
                .priceCurrencyByProductUuidAndCurrency(products.getFirst().uuid(), CapstoneCurrency.NGN);
        assertFalse(optional.isEmpty());

        PriceCurrencyDbMapper pojo = optional.get();
        assertNotNull(pojo.name());
        assertNotNull(pojo.description());
        assertNotNull(pojo.currency());
        assertNotNull(pojo.price());
    }

    @Test
    void updateProductPriceByProductUuidAndCurrency() {
        // given
        var cat = categoryRepo.save(Category.builder().name("category").isVisible(true).build());
        RepositoryTestData
                .createProduct(3, cat, productRepository, detailRepo, productPriceCurrencyRepository, imageRepo, skuRepo);

        // when
        var products = TestUtility.toList(productRepository.findAll());
        assertFalse(products.isEmpty());

        // then
        currencyRepo
                .updateProductPriceByProductUuidAndCurrency(
                        products.getFirst().uuid(),
                        new BigDecimal("10.52"),
                        CapstoneCurrency.USD
                );

        var optional = currencyRepo
                .priceCurrencyByProductUuidAndCurrency(products.getFirst().uuid(), CapstoneCurrency.USD);
        assertFalse(optional.isEmpty());
        assertNotNull(optional.get().price());
        Assertions.assertEquals(new BigDecimal("10.52"), optional.get().price());
    }

}