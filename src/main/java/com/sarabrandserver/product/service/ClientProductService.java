package com.sarabrandserver.product.service;

import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomNotFoundException;
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
    @Value(value = "${spring.profiles.active}")
    private String ACTIVEPROFILE;

    private final ProductRepo productRepo;
    private final ProductDetailRepo productDetailRepo;
    private final PriceCurrencyRepo priceCurrencyRepo;
    private final S3Service s3Service;
    private final CustomUtil customUtil;

    /**
     * Returns a page of ProductResponse
     *
     * @param key  is based on the controller that called this method
     * @param currency of type SarreBrandCurrency
     * @param uuid is a unique string attached to either a Collection or Category
     * @param page number
     * @param size number of ProductResponse for each page
     * @return a Page of ProductResponse
     */
    public Page<ProductResponse> allProductsByUUID(
            String key,
            SarreCurrency currency,
            String uuid,
            int page,
            int size
    ) {
        return switch (key) {
            case "" -> this.productRepo
                    .allProductsByCurrencyClient(currency, PageRequest.of(page, size)) //
                    .map(pojo -> {
                        var url = this.s3Service.preSignedUrl(BUCKET, pojo.getKey());
                        return ProductResponse.builder()
                                .category(pojo.getCategory())
                                .collection(pojo.getCollection())
                                .id(pojo.getUuid())
                                .name(pojo.getName())
                                .desc(pojo.getDescription())
                                .price(pojo.getPrice())
                                .currency(pojo.getCurrency())
                                .imageUrl(url)
                                .build();
                    });

            case "category" -> this.productRepo
                    .productsByCategoryClient(uuid, currency, PageRequest.of(page, size)) //
                    .map(pojo -> {
                        var url = this.s3Service.preSignedUrl(BUCKET, pojo.getKey());
                        return ProductResponse.builder()
                                .category(pojo.getCategory())
                                .id(pojo.getUuid())
                                .name(pojo.getName())
                                .desc(pojo.getDescription())
                                .price(pojo.getPrice())
                                .currency(pojo.getCurrency())
                                .imageUrl(url)
                                .build();
                    });

            case "collection" -> this.productRepo
                    .productsByCollectionClient(currency, uuid, PageRequest.of(page, size))
                    .map(pojo -> {
                        var url = this.s3Service.preSignedUrl(BUCKET, pojo.getKey());
                        return ProductResponse.builder()
                                .collection(pojo.getCollection())
                                .id(pojo.getUuid())
                                .name(pojo.getName())
                                .desc(pojo.getDescription())
                                .price(pojo.getPrice())
                                .currency(pojo.getCurrency())
                                .imageUrl(url)
                                .build();
                    });

            default -> throw new CustomNotFoundException("Invalid key: " + key);
        };
    }

    /**
     * Returns a list of ProductDetails based on Product property UUID
     * */
    public List<DetailResponse> productDetailsByProductUUID(String uuid, SarreCurrency currency) {
        var object = this.priceCurrencyRepo
                .priceCurrencyByProductUUIDAndCurrency(uuid, currency)
                .orElse(null);

        if (object == null) return List.of();

        return this.productDetailRepo
                .productDetailsByProductUUIDClient(uuid) //
                .stream() //
                .map(pojo -> {
                    var urls = Arrays.stream(pojo.getImage().split(","))
                            .map(key -> this.s3Service.preSignedUrl(BUCKET, key))
                            .toList();

                    Variant[] variants = this.customUtil
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
     * Returns a list of ProductResponse based on user search param.
     * Initially a Page of page size 10 is returned to increase efficiency.
     *
     * @param param is the user input
     * @param currency is of type SarreCurrency. default is NGN
     * @return List of ProductResponse
     * */
    public List<ProductResponse> search(String param, SarreCurrency currency) {
        // SQL LIKE Operator
        // https://www.w3schools.com/sql/sql_like.asp
        return this.productRepo
                .productByNameAndCurrency(param + "%", currency, PageRequest.of(0, 10))
                .stream()
                .map(pojo -> {
                    var url = this.s3Service.preSignedUrl(BUCKET, pojo.getKey());
                    return ProductResponse.builder()
                            .category(pojo.getCategory())
                            .id(pojo.getUuid())
                            .name(pojo.getName())
                            .price(pojo.getPrice())
                            .currency(pojo.getCurrency())
                            .imageUrl(url)
                            .build();
                })
                .toList();
    }

}

