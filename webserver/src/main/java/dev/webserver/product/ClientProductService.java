package dev.webserver.product;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.external.aws.S3Service;
import dev.webserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static java.math.RoundingMode.FLOOR;

@Service
@RequiredArgsConstructor
public class ClientProductService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final PriceCurrencyRepository priceCurrencyRepository;
    private final S3Service s3Service;

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
                .allProductsByCurrencyClient(currency, PageRequest.of(page, size));

        var futures = pageOfProducts.stream()
                .map(p -> (Supplier<ProductResponse>) () ->
                        ProductResponse.builder()
                                .id(p.getUuid())
                                .name(p.getName())
                                .desc(p.getDescription())
                                .price(p.getPrice())
                                .currency(p.getCurrency())
                                .imageUrl(p.getImage())
                                .category(p.getCategory())
                                .build())
                .toList();

        final var products = CustomUtil.asynchronousTasks(futures).join();
        return new PageImpl<>(products, pageOfProducts.getPageable(), pageOfProducts.getTotalElements());
    }

    public List<DetailResponse> productDetailsByProductUuid(
            String uuid,
            SarreCurrency currency
    ) {
        var optional = this.priceCurrencyRepository.priceCurrencyByProductUuidAndCurrency(uuid, currency);

        if (optional.isEmpty())
            return List.of();

        PriceCurrencyProjection object = optional.get();

        var futures = productDetailRepository
                .productDetailsByProductUuidClientFront(uuid)
                .stream()
                .map(pojo -> (Supplier<DetailResponse>) () -> {
                    var suppliers = Arrays
                            .stream(pojo.getImage().split(","))
                            .map(key -> (Supplier<String>) () -> s3Service.preSignedUrl(BUCKET, key))
                            .toList();

                    var urls = CustomUtil
                            .asynchronousTasks(suppliers)
                            .join();

                    var variants = CustomUtil
                            .toVariantArray(pojo.getVariants(), ClientProductService.class);

                    return DetailResponse.builder()
                            .name(object.getName())
                            .currency(object.getCurrency().name())
                            .price(object.getPrice().setScale(2, FLOOR))
                            .desc(object.getDescription())
                            .colour(pojo.getColour())
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
        var pageOfProducts = productRepository
                .productsByNameAndCurrency(param + "%", currency, PageRequest.of(0, size));

        var futures = pageOfProducts.stream()
                .map(p -> (Supplier<ProductResponse>) () ->
                        ProductResponse.builder()
                                .id(p.getUuid())
                                .name(p.getName())
                                .price(p.getPrice())
                                .currency(p.getCurrency())
                                .imageUrl(p.getImage())
                                .category(p.getCategory())
                                .build())
                .toList();

        final var products = CustomUtil.asynchronousTasks(futures).join();
        return new PageImpl<>(products, pageOfProducts.getPageable(), pageOfProducts.getTotalElements());
    }

}
