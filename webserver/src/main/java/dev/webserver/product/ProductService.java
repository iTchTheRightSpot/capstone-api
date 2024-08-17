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

import static dev.webserver.cache.CacheEnum.STORE_FRONT;
import static java.math.RoundingMode.FLOOR;

@Service
class ProductService extends AbstractEnvironment {

    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductPriceCurrencyRepository productPriceCurrencyRepository;
    private final IS3Service s3Service;
    private final IProductCachePublisher publisher;

    protected ProductService(
            final Environment environment,
            final ProductRepository productRepository,
            final ProductDetailRepository productDetailRepository,
            final ProductPriceCurrencyRepository productPriceCurrencyRepository,
            final IS3Service s3Service,
            final IProductCachePublisher publisher
    ) {
        super(environment);
        this.productRepository = productRepository;
        this.productDetailRepository = productDetailRepository;
        this.productPriceCurrencyRepository = productPriceCurrencyRepository;
        this.s3Service = s3Service;
        this.publisher = publisher;
    }

    public Pageable<ProductResponse> allProductsByCurrency(final CapstoneCurrency currency, final int page, final int size) {
        final String key = "allProductsByCurrency_%s_%s_%d_%d".formatted(STORE_FRONT, currency, page, size);

        final var cache = publisher.pageOfProductResponse(key);

        if (cache.isPresent()) return cache.get();

        final Page of = dev.webserver.util.Page.of(page, size);
        final int count = productRepository.countAllProductsStoreFront();

        final var futures = productRepository.allProductsByCurrencyStoreFront(of, currency)
                .stream()
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

        final var pageable = new Pageable<>(of, count, CustomUtil.asynchronousTasks(futures).join());

        publisher.addPageOfProductResponseToCache(key, pageable);

        return pageable;
    }

    public List<DetailResponse> productDetailsByProductUuid(final String uuid, final CapstoneCurrency currency) {
        final String key = "productDetailsByProductUuid_%s_%s_%s".formatted(STORE_FRONT, uuid, currency);
        final var cache = publisher.listOfDetailResponse(key);

        if (cache.isPresent()) return cache.get();

        final var optional = productPriceCurrencyRepository.priceCurrencyByProductUuidAndCurrency(uuid, currency);

        if (optional.isEmpty()) return List.of();

        final PriceCurrencyDbMapper object = optional.get();

        final var futures = productDetailRepository
                .productDetailsByProductUuidClientFront(uuid)
                .stream()
                .map(pojo -> (Supplier<DetailResponse>) () -> {
                    final var suppliers = Arrays
                            .stream(pojo.imageKey().split(","))
                            .map(imageKey -> (Supplier<String>) () -> s3Service.preSignedUrl(super.awsbucket, imageKey))
                            .toList();

                    final var urls = CustomUtil.asynchronousTasks(suppliers).join();

                    final var variants = CustomUtil.toVariantArray(pojo.variants(), ProductService.class);

                    return DetailResponse.builder()
                            .name(object.name())
                            .currency(object.currency().name())
                            .price(object.price().setScale(2, FLOOR))
                            .desc(object.description())
                            .colour(pojo.colour())
                            .imageKeys(urls)
                            .variants(variants)
                            .build();
                })
                .toList();

        final var list = CustomUtil.asynchronousTasks(futures).join();
        publisher.addListOfDetailResponseToCache(key, list);
        return list;
    }

    public Pageable<ProductResponse> search(final String param, final CapstoneCurrency currency, final int size) {
        final String key = "search_%s_%s_%d".formatted(STORE_FRONT, currency, size);

        final var cache = publisher.pageOfProductResponse(key);

        if (cache.isPresent()) return cache.get();

        // SQL LIKE Operator
        // https://www.w3schools.com/sql/sql_like.asp

        final var futures = productRepository.productsByNameAndCurrency(param + "%", currency)
                .stream()
                .map(p -> (Supplier<ProductResponse>) () -> ProductResponse.builder()
                        .id(p.uuid())
                        .name(p.name())
                        .price(p.price())
                        .currency(p.currency().name())
                        .imageKey(p.imageKey())
                        .category(p.categoryName())
                        .build())
                .toList();

        final var products = CustomUtil.asynchronousTasks(futures).join();
        final var pageable = new Pageable<>(Page.of(0, size), products.size(), products);

        publisher.addPageOfProductResponseToCache(key, pageable);
        return pageable;
    }

}
