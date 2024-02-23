package com.sarabrandserver.product.service;

import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.projection.PriceCurrencyPojo;
import com.sarabrandserver.product.projection.ProductPojo;
import com.sarabrandserver.product.repository.PriceCurrencyRepo;
import com.sarabrandserver.product.repository.ProductDetailRepo;
import com.sarabrandserver.product.repository.ProductRepo;
import com.sarabrandserver.product.response.DetailResponse;
import com.sarabrandserver.product.response.ProductResponse;
import com.sarabrandserver.product.response.Variant;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    private final ProductRepo productRepo;
    private final ProductDetailRepo productDetailRepo;
    private final PriceCurrencyRepo priceCurrencyRepo;
    private final S3Service s3Service;

    /**
     * Returns a {@link Page} of {@link ProductResponse}
     *
     * @param currency of type {@link SarreCurrency}
     * @param page number
     * @param size number of ProductResponse for each page
     * @return a Page of {@link ProductResponse}
     */
    public CompletableFuture<Page<ProductResponse>> allProductsByCurrency(SarreCurrency currency, int page, int size) {
        Page<ProductPojo> dbRes = productRepo
                .allProductsByCurrencyClient(currency, PageRequest.of(page, size));

        List<Supplier<ProductResponse>> futures = createPageTasks(dbRes);

        return CustomUtil.asynchronousTasks(futures)
                .thenApply(v -> new PageImpl<>(
                        v.stream().map(Supplier::get).toList(),
                        dbRes.getPageable(),
                        dbRes.getTotalElements()
                ));
    }

    private List<Supplier<ProductResponse>> createPageTasks(Page<ProductPojo> dbRes) {
        List<Supplier<ProductResponse>> futures = new ArrayList<>();

        for (ProductPojo p : dbRes) {
            futures.add(() -> {
                var url = s3Service.preSignedUrl(BUCKET, p.getImage());
                return new ProductResponse(
                        p.getUuid(),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getCurrency(),
                        url,
                        p.getCategory()
                );
            });
        }
        return futures;
    }

    public CompletableFuture<List<DetailResponse>> productDetailsByProductUuid(
            String uuid,
            SarreCurrency currency
    ) {
        var optional = this.priceCurrencyRepo
                .priceCurrencyByProductUuidAndCurrency(uuid, currency);

        if (optional.isEmpty())
            return CompletableFuture.completedFuture(List.of());

        PriceCurrencyPojo object = optional.get();

        List<CompletableFuture<DetailResponse>> futures = productDetailRepo
                .productDetailsByProductUuidClientFront(uuid)
                .stream()
                .map(pojo -> CompletableFuture.supplyAsync(() -> {
                    var suppliers = Arrays
                            .stream(pojo.getImage().split(","))
                            .map(key -> (Supplier<String>) () -> s3Service.preSignedUrl(BUCKET, key))
                            .toList();

                    List<String> urls = CustomUtil.asynchronousTasks(suppliers) //
                            .thenApply(v -> v.stream().map(Supplier::get).toList()) //
                            .join();

                    Variant[] variants = CustomUtil
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
                }))
                .toList();

        return CustomUtil.asynchronousTasks(futures)
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());
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
        Page<ProductPojo> dbRes = productRepo
                .productsByNameAndCurrency(param + "%", currency, PageRequest.of(0, size));

        List<Supplier<ProductResponse>> futures = createTasks(dbRes);

        return CustomUtil.asynchronousTasks(futures)
                .thenApply(v -> new PageImpl<>(
                        v.stream().map(Supplier::get).toList(),
                        dbRes.getPageable(),
                        dbRes.getTotalElements()
                ));
    }

    private List<Supplier<ProductResponse>> createTasks(Page<ProductPojo> dbRes) {
        List<Supplier<ProductResponse>> futures = new ArrayList<>();
        for (ProductPojo p : dbRes) {
            futures.add(() -> {
                var url = s3Service.preSignedUrl(BUCKET, p.getImage());
                return new ProductResponse(
                        p.getUuid(),
                        p.getName(),
                        p.getPrice(),
                        p.getCurrency(),
                        url,
                        p.getCategory()
                );
            });
        }
        return futures;
    }

}

