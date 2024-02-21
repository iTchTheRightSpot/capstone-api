package com.sarabrandserver.product.service;

import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.enumeration.SarreCurrency;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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
     * Returns a page of {@link ProductResponse}
     *
     * @param currency of type {@link SarreCurrency}
     * @param page number
     * @param size number of ProductResponse for each page
     * @return a Page of {@link ProductResponse}
     */
    public Page<ProductResponse> allProductsByProductUuid(SarreCurrency currency, int page, int size) {
        return this.productRepo
                .allProductsByCurrencyClient(currency, PageRequest.of(page, size)) //
                .map(p -> {
                    var url = this.s3Service.preSignedUrl(BUCKET, p.getImage());
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

    /**
     * Returns a list of {@link com.sarabrandserver.product.entity.ProductDetail} based on
     * {@link com.sarabrandserver.product.entity.Product} uuid.
     * */
    public List<DetailResponse> productDetailsByProductUUID(String uuid, SarreCurrency currency) {
        var object = this.priceCurrencyRepo
                .priceCurrencyByProductUuidAndCurrency(uuid, currency)
                .orElse(null);

        if (object == null) return List.of();

        return this.productDetailRepo
                .productDetailsByProductUuidClient(uuid) //
                .stream() //
                .map(pojo -> {
                    var urls = Arrays.stream(pojo.getImage().split(","))
                            .map(key -> this.s3Service.preSignedUrl(BUCKET, key))
                            .toList();

                    Variant[] variants = CustomUtil
                            .toVariantArray(pojo.getVariants(), ClientProductService.class);

                    return DetailResponse.builder()
                            .name(object.getName())
                            .currency(object.getCurrency().name())
                            .price(object.getPrice().setScale(2, FLOOR))
                            .desc(object.getDescription())
                            .colour(pojo.getColour())
                            .url(urls)
                            .variants(variants)
                            .build();
                }) //
                .toList();
    }

    /**
     * Returns a list of {@link ProductResponse} based on user search param.
     * Initially a Page of page size 10 is returned to increase efficiency.
     *
     * @param param is the user input.
     * @param currency is of type {@link SarreCurrency}.
     * @return A List of {@link ProductResponse}.
     * */
    public List<ProductResponse> search(String param, SarreCurrency currency) {
        // SQL LIKE Operator
        // https://www.w3schools.com/sql/sql_like.asp
        return this.productRepo
                .productsByNameAndCurrency(param + "%", currency, PageRequest.of(0, 10))
                .stream()
                .map(p -> {
                    var url = this.s3Service.preSignedUrl(BUCKET, p.getImage());
                    return new ProductResponse(
                            p.getUuid(),
                            p.getName(),
                            p.getPrice(),
                            p.getCurrency(),
                            url,
                            p.getCategory()
                    );
                })
                .toList();
    }

}

