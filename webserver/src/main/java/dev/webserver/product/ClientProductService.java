package dev.webserver.product;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.external.aws.IS3Service;
import dev.webserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static java.math.RoundingMode.FLOOR;

@Service
@RequiredArgsConstructor
class ClientProductService {

    @Value(value = "${aws.bucket}")
    private String bucket;

    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final PriceCurrencyRepository priceCurrencyRepository;
    private final IS3Service s3Service;

    /**
     * Returns a {@link Page} of {@link ProductResponse}
     *
     * @param currency of type {@link SarreCurrency}
     * @param page number
     * @param size number of ProductResponse for each page.
     * @return a Page of {@link ProductResponse}.
     */
    public Page<ProductResponse> allProductsByCurrency(SarreCurrency currency, int page, int size) {
        var pageOfProducts = productRepository
                .allProductsByCurrencyClient(currency);

        var futures = pageOfProducts.stream()
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
        return new PageImpl<>(products, pageOfProducts.getPageable(), pageOfProducts.getTotalElements());
    }

    public List<DetailResponse> productDetailsByProductUuid(
            String uuid,
            SarreCurrency currency
    ) {
        var optional = priceCurrencyRepository.priceCurrencyByProductUuidAndCurrency(uuid, currency);

        if (optional.isEmpty())
            return List.of();

        PriceCurrencyDbMapper object = optional.get();

        var futures = productDetailRepository
                .productDetailsByProductUuidClientFront(uuid)
                .stream()
                .map(pojo -> (Supplier<DetailResponse>) () -> {
                    var suppliers = Arrays
                            .stream(pojo.imageKey().split(","))
                            .map(key -> (Supplier<String>) () -> s3Service.preSignedUrl(bucket, key))
                            .toList();

                    var urls = CustomUtil
                            .asynchronousTasks(suppliers)
                            .join();

                    var variants = CustomUtil
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
    public Page<ProductResponse> search(String param, SarreCurrency currency, int size) {
        // SQL LIKE Operator
        // https://www.w3schools.com/sql/sql_like.asp
        var pageOfProducts = productRepository.productsByNameAndCurrency(param + "%", currency);

        var futures = pageOfProducts.stream()
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
        return new PageImpl<>(products, pageOfProducts.getPageable(), pageOfProducts.getTotalElements());
    }

}
