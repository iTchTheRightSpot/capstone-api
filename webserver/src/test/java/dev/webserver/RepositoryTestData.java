package dev.webserver;

import com.github.javafaker.Faker;
import dev.webserver.category.Category;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.*;
import dev.webserver.util.CustomUtil;

import java.math.BigDecimal;
import java.util.UUID;

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
                        .build()
        );

        productPriceCurrencyRepository
                .save(new ProductPriceCurrency(null, new BigDecimal(new Faker().commerce().price()), SarreCurrency.NGN, product.productId()));
        productPriceCurrencyRepository
                .save(new ProductPriceCurrency(null, new BigDecimal(new Faker().commerce().price()), SarreCurrency.USD, product.productId()));

        final ProductDetail detail = detailRepo.save(
                ProductDetail.builder()
                        .colour(UUID.randomUUID().toString())
                        .isVisible(true)
                        .createAt(CustomUtil.TO_GREENWICH.apply(null))
                        .productId(product.productId())
                        .build());

        for (int i = 0; i < numberOfChildren; i++) {
            imageRepo.save(
                    new ProductImage(
                            null,
                            UUID.randomUUID().toString(),
                            detail.detailId()
                    )
            );

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
