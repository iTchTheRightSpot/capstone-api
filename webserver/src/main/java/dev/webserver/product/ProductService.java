package dev.webserver.product;

import dev.webserver.AbstractEnvironment;
import dev.webserver.enumeration.CapstoneCurrency;
import dev.webserver.external.aws.IS3Service;
import dev.webserver.util.CustomUtil;
import dev.webserver.util.Page;
import dev.webserver.util.Pageable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static java.math.RoundingMode.FLOOR;

@Service
class ProductService extends AbstractEnvironment {

    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductPriceCurrencyRepository productPriceCurrencyRepository;
    private final IS3Service s3Service;

    protected ProductService(final Environment environment, final ProductRepository productRepository, final ProductDetailRepository productDetailRepository, final ProductPriceCurrencyRepository productPriceCurrencyRepository, final IS3Service s3Service) {
        super(environment);
        this.productRepository = productRepository;
        this.productDetailRepository = productDetailRepository;
        this.productPriceCurrencyRepository = productPriceCurrencyRepository;
        this.s3Service = s3Service;
    }

    public Pageable<ProductResponse> allProductsByCurrency(final CapstoneCurrency currency, final int page, final int size) {
        final Page of = dev.webserver.util.Page.of(page, size);
        final int count = productRepository.countAllProductsStoreFront();
        final var listOfProducts = productRepository.allProductsByCurrencyStoreFront(of, currency);

        final var futures = listOfProducts.stream()
                .map(p -> (Supplier<ProductResponse>) () ->
                        ProductResponse.builder()
                                .id(p.uuid())
                                .name(p.name())
                                .desc(p.description())
                                .price(p.price())
                                .currency(p.currency().getCurrency())
                                .imageKey(p.imageKey())
                                .category(p.categoryName())
                                .build())
                .toList();

        final var products = CustomUtil.asynchronousTasks(futures).join();
        return new Pageable<>(of, count, products);
    }

    public List<DetailResponse> productDetailsByProductUuid(
            final String uuid,
            final CapstoneCurrency currency
    ) {
        final var optional = productPriceCurrencyRepository.priceCurrencyByProductUuidAndCurrency(uuid, currency);

        if (optional.isEmpty())
            return List.of();

        final PriceCurrencyDbMapper object = optional.get();

        final var futures = productDetailRepository
                .productDetailsByProductUuidClientFront(uuid)
                .stream()
                .map(pojo -> (Supplier<DetailResponse>) () -> {
                    final var suppliers = Arrays
                            .stream(pojo.imageKey().split(","))
                            .map(key -> (Supplier<String>) () -> s3Service.preSignedUrl(super.awsbucket, key))
                            .toList();

                    final var urls = CustomUtil
                            .asynchronousTasks(suppliers)
                            .join();

                    final var variants = CustomUtil
                            .toVariantArray(pojo.variants(), ProductService.class);

                    return DetailResponse.builder()
                            .name(object.name())
                            .currency(object.currency().name())
                            .price(object.price().setScale(2, FLOOR))
                            .desc(object.description())
                            .colour(pojo.colour())
                            .urls(urls)
                            .variants(variants)
                            .build();
                })
                .toList();

        return CustomUtil.asynchronousTasks(futures).join();
    }

    public Pageable<ProductResponse> search(final String param, final CapstoneCurrency currency, final int size) {
        // SQL LIKE Operator
        // https://www.w3schools.com/sql/sql_like.asp
        final var listOfProducts = productRepository.productsByNameAndCurrency(param + "%", currency);

        final var futures = listOfProducts.stream()
                .map(p -> (Supplier<ProductResponse>) () ->
                        ProductResponse.builder()
                                .id(p.uuid())
                                .name(p.name())
                                .price(p.price())
                                .currency(p.currency().name())
                                .imageKey(p.imageKey())
                                .category(p.categoryName())
                                .build())
                .toList();

        final var products = CustomUtil.asynchronousTasks(futures).join();
        return new Pageable<>(Page.of(0, size), products.size(), products);
    }

}
