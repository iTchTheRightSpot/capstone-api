package dev.webserver;

import com.github.javafaker.Faker;
import dev.webserver.category.Category;
import dev.webserver.product.*;

import java.math.BigDecimal;
import java.util.UUID;

import static dev.webserver.enumeration.CapstoneCurrency.NGN;
import static dev.webserver.enumeration.CapstoneCurrency.USD;

public final class RepositoryTestData {

    public static void createProduct(
            final int numberOfChildren,
            final Category category,
            final ProductRepository repository,
            final ProductDetailRepository detailRepo,
            final ProductPriceCurrencyRepository productPriceCurrencyRepository,
            final ProductImageRepository imageRepo,
            final ProductSkuRepository skuRepo
    ) {
        final Product product = repository.save(
                Product.builder()
                        .uuid(UUID.randomUUID().toString())
                        .name(UUID.randomUUID().toString())
                        .description(new Faker().lorem().fixedString(500))
                        .defaultKey(UUID.randomUUID().toString())
                        .weight(BigDecimal.valueOf(new Faker().number().randomDouble(3, 1, 50)))
                        .weightType("kg")
                        .categoryId(category.categoryId())
                        .build());

        productPriceCurrencyRepository
                .save(new ProductPriceCurrency(null, new BigDecimal(new Faker().commerce().price()), NGN, product.productId()));
        productPriceCurrencyRepository
                .save(new ProductPriceCurrency(null, new BigDecimal(new Faker().commerce().price()), USD, product.productId()));

        final ProductDetail detail = detailRepo.save(
                ProductDetail.builder()
                        .colour(UUID.randomUUID().toString())
                        .isVisible(true)
                        .productId(product.productId())
                        .build());

        for (int i = 0; i < numberOfChildren; i++) {
            imageRepo.save(new ProductImage(null, UUID.randomUUID().toString(), detail.detailId()));

            skuRepo.save(
                    ProductSku.builder()
                            .sku(UUID.randomUUID().toString())
                            .size(UUID.randomUUID().toString())
                            .inventory(new Faker().number().numberBetween(10, 20))
                            .detailId(detail.detailId())
                            .build());
        }
    }

    public static void createProductAndMultipleDetails(
            final int numberOfChildren,
            final Category category,
            final ProductRepository repository,
            final ProductDetailRepository detailRepo,
            final ProductPriceCurrencyRepository currencyRepository,
            final ProductImageRepository imageRepo,
            final ProductSkuRepository skuRepo
    ) {
        final Product product = repository.save(
                Product.builder()
                        .uuid(UUID.randomUUID().toString())
                        .name(UUID.randomUUID().toString())
                        .description(new Faker().lorem().fixedString(500))
                        .defaultKey(UUID.randomUUID().toString())
                        .weight(BigDecimal.valueOf(new Faker().number().randomDouble(3, 1, 50)))
                        .weightType("kg")
                        .categoryId(category.categoryId())
                        .build());

        currencyRepository
                .save(new ProductPriceCurrency(null, new BigDecimal(new Faker().commerce().price()), NGN, product.productId()));
        currencyRepository
                .save(new ProductPriceCurrency(null, new BigDecimal(new Faker().commerce().price()), USD, product.productId()));

        for (int i = 0; i < numberOfChildren; i++) {
            final ProductDetail detail = detailRepo.save(
                    ProductDetail.builder()
                            .colour(UUID.randomUUID().toString())
                            .isVisible(true)
                            .productId(product.productId())
                            .build());

            for (int j = 0; j < numberOfChildren; j++) {
                imageRepo.save(new ProductImage(null, UUID.randomUUID().toString(), detail.detailId()));

                skuRepo.save(
                        ProductSku.builder()
                                .sku(UUID.randomUUID().toString())
                                .size(UUID.randomUUID().toString())
                                .inventory(new Faker().number().numberBetween(10, 20))
                                .detailId(detail.detailId())
                                .build());
            }
        }
    }

}
