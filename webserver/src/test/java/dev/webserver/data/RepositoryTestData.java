package dev.webserver.data;

import com.github.javafaker.Faker;
import dev.webserver.category.Category;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

public class RepositoryTestData {

    public static void createProduct(
            int numberOfChildren,
            Category category,
            ProductRepository repository,
            ProductDetailRepository detailRepo,
            PriceCurrencyRepository priceCurrencyRepository,
            ProductImageRepository imageRepo,
            ProductSkuRepository skuRepo
    ) {
        Product product = repository.save(
                Product.builder()
                        .uuid(UUID.randomUUID().toString())
                        .name(UUID.randomUUID().toString())
                        .description(new Faker().lorem().fixedString(500))
                        .defaultKey(UUID.randomUUID().toString())
                        .weight(new Faker().number().randomDouble(3, 1, 50))
                        .weightType("kg")
                        .categoryId(category)
                        .productDetails(new HashSet<>())
                        .priceCurrency(new HashSet<>())
                        .build()
        );

        priceCurrencyRepository
                .save(new PriceCurrency(new BigDecimal(new Faker().commerce().price()), SarreCurrency.NGN, product));
        priceCurrencyRepository
                .save(new PriceCurrency(new BigDecimal(new Faker().commerce().price()), SarreCurrency.USD, product));

        ProductDetail detail = detailRepo.save(
                ProductDetail.builder()
                        .colour(UUID.randomUUID().toString())
                        .isVisible(true)
                        .createAt(new Date())
                        .product(product)
                        .productImages(new HashSet<>())
                        .skus(new HashSet<>())
                        .build()
        );

        for (int i = 0; i < numberOfChildren; i++) {
            imageRepo.save(
                    new ProductImage(
                            UUID.randomUUID().toString(),
                            "path " + new Faker().number().numberBetween(1, 100),
                            detail
                    )
            );

            skuRepo.save(
                    ProductSku.builder()
                            .sku(UUID.randomUUID().toString())
                            .size(UUID.randomUUID().toString())
                            .inventory(new Faker().number().numberBetween(10, 20))
                            .productDetailId(detail)
                            .orderDetails(new HashSet<>())
                            .reservations(new HashSet<>())
                            .cartItems(new HashSet<>())
                            .build()
            );
        }
    }

}
