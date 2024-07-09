package dev.webserver.product;

import dev.webserver.external.aws.S3Service;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.response.DetailResponse;
import dev.webserver.product.response.ProductResponse;
import dev.webserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    public CompletableFuture<Page<ProductResponse>> allProductsByCurrency(SarreCurrency currency, int page, int size) {
        var pageOfProducts = productRepository
                .allProductsByCurrencyClient(currency, PageRequest.of(page, size));

        var futures = createPageTasks(pageOfProducts);

        return CustomUtil.asynchronousTasks(futures, ClientProductService.class)
                .thenApply(v -> new PageImpl<>(
                        v.stream().map(Supplier::get).toList(),
                        pageOfProducts.getPageable(),
                        pageOfProducts.getTotalElements()
                ));
    }

    private List<Supplier<ProductResponse>> createPageTasks(Page<ProductProjection> page) {
        return page.stream()
                .map(p -> (Supplier<ProductResponse>) () -> new ProductResponse(
                        p.getUuid(),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getCurrency(),
                        s3Service.preSignedUrl(BUCKET, p.getImage()),
                        p.getCategory()
                ))
                .toList();
    }

    public CompletableFuture<List<DetailResponse>> productDetailsByProductUuid(
            String uuid,
            SarreCurrency currency
    ) {
        var optional = this.priceCurrencyRepository
                .priceCurrencyByProductUuidAndCurrency(uuid, currency);

        if (optional.isEmpty())
            return CompletableFuture.completedFuture(List.of());

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
                            .asynchronousTasks(suppliers, ClientProductService.class)
                            .thenApply(v -> v.stream().map(Supplier::get).toList())
                            .join();

                    var variants = CustomUtil
                            .toVariantArray(pojo.getVariants(), ClientProductService.class);

                    return new DetailResponse(
                            object.getName(),
                            object.getCurrency().name(),
                            object.getPrice().setScale(2, FLOOR),
                            object.getDescription(),
                            pojo.getColour(),
                            urls,
                            variants
                    );
                })
                .toList();

        return CustomUtil.asynchronousTasks(futures, ClientProductService.class)
                .thenApply(v -> v.stream().map(Supplier::get).toList());
    }

    /**
     * Returns a {@link Page} of {@link ProductResponse} asynchronously.
     *
     * @param param is the user input.
     * @param currency is of type {@link SarreCurrency}.
     * @return A {@link CompletableFuture} of {@link Page} containing
     * {@link ProductResponse}.
     * */
    public CompletableFuture<Page<ProductResponse>> search(String param, SarreCurrency currency, int size) {
        // SQL LIKE Operator
        // https://www.w3schools.com/sql/sql_like.asp
        var pageOfProducts = productRepository
                .productsByNameAndCurrency(param + "%", currency, PageRequest.of(0, size));

        var futures = createTasks(pageOfProducts);

        return CustomUtil.asynchronousTasks(futures, ClientProductService.class)
                .thenApply(v -> new PageImpl<>(
                        v.stream().map(Supplier::get).toList(),
                        pageOfProducts.getPageable(),
                        pageOfProducts.getTotalElements()
                ));
    }

    private List<Supplier<ProductResponse>> createTasks(Page<ProductProjection> page) {
        return page.stream()
                .map(p -> (Supplier<ProductResponse>) () -> new ProductResponse(
                        p.getUuid(),
                        p.getName(),
                        p.getPrice(),
                        p.getCurrency(),
                        s3Service.preSignedUrl(BUCKET, p.getImage()),
                        p.getCategory()
                ))
                .toList();
    }

}
