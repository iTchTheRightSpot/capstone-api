package dev.webserver.product;

import dev.webserver.AbstractEnvironment;
import dev.webserver.enumeration.SarreCurrency;
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
class ClientProductService extends AbstractEnvironment {

    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final PriceCurrencyRepository priceCurrencyRepository;
    private final IS3Service s3Service;

    protected ClientProductService(final Environment environment, final ProductRepository productRepository, final ProductDetailRepository productDetailRepository, final PriceCurrencyRepository priceCurrencyRepository, final IS3Service s3Service) {
        super(environment);
        this.productRepository = productRepository;
        this.productDetailRepository = productDetailRepository;
        this.priceCurrencyRepository = priceCurrencyRepository;
        this.s3Service = s3Service;
    }

    public Pageable<ProductResponse> allProductsByCurrency(final SarreCurrency currency, final int page, final int size) {
        final Page of = dev.webserver.util.Page.of(page, size);
        final Integer count = productRepository.countAllProductsByCurrencyClient(currency);
        final var listOfProducts = productRepository.allProductsByCurrencyClient(of, currency);

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
            final SarreCurrency currency
    ) {
        final var optional = priceCurrencyRepository.priceCurrencyByProductUuidAndCurrency(uuid, currency);

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
                            .toVariantArray(pojo.variants(), ClientProductService.class);

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

    /**
     * Returns a {@link Page} of {@link ProductResponse} asynchronously.
     *
     * @param param is the user input.
     * @param currency is of type {@link SarreCurrency}.
     * @return A {@link Page} of {@link ProductResponse}.
     * */
    public Pageable<ProductResponse> search(final String param, final SarreCurrency currency, final int size) {
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
