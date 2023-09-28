package com.emmanuel.sarabrandserver.product.service;

import com.emmanuel.sarabrandserver.aws.S3Service;
import com.emmanuel.sarabrandserver.exception.CustomNotFoundException;
import com.emmanuel.sarabrandserver.exception.SseException;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.util.DetailResponse;
import com.emmanuel.sarabrandserver.product.util.ProductResponse;
import com.emmanuel.sarabrandserver.product.util.Variant;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;

@Service
public class ClientProductService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    @Value(value = "${spring.profiles.active}")
    private String ACTIVEPROFILE;

    private final ProductRepository productRepository;
    private final ProductDetailRepo productDetailRepo;
    private final S3Service s3Service;
    private final CustomUtil customUtil;

    public ClientProductService(
            ProductRepository productRepository,
            ProductDetailRepo productDetailRepo,
            S3Service s3Service,
            CustomUtil customUtil
    ) {
        this.productRepository = productRepository;
        this.productDetailRepo = productDetailRepo;
        this.s3Service = s3Service;
        this.customUtil = customUtil;
    }

    /**
     * Returns a page of ProductResponse
     *
     * @param key  is based on the controller that called this method
     * @param uuid is a unique string attached to either a Collection or Category
     * @param page number
     * @param size number of ProductResponse for each page
     */
    public Page<ProductResponse> allProductsByUUID(String key, String uuid, int page, int size) {
        boolean bool = ACTIVEPROFILE.equals("prod") || ACTIVEPROFILE.equals("stage");

        return switch (key) {
            case "" -> this.productRepository
                    .fetchAllProductsClient(PageRequest.of(page, size)) //
                    .map(pojo -> {
                        var url = this.s3Service.getPreSignedUrl(bool, BUCKET, pojo.getKey());
                        return ProductResponse.builder()
                                .category(pojo.getCategory())
                                .collection(pojo.getCollection())
                                .id(pojo.getUuid())
                                .name(pojo.getName())
                                .desc(pojo.getDesc())
                                .price(pojo.getPrice())
                                .currency(pojo.getCurrency())
                                .imageUrl(url)
                                .build();
                    });

            case "category" -> this.productRepository
                    .fetchProductByCategoryClient(uuid, PageRequest.of(page, size)) //
                    .map(pojo -> {
                        var url = this.s3Service.getPreSignedUrl(bool, BUCKET, pojo.getKey());
                        return ProductResponse.builder()
                                .category(pojo.getCategory())
                                .id(pojo.getUuid())
                                .name(pojo.getName())
                                .desc(pojo.getDesc())
                                .price(pojo.getPrice())
                                .currency(pojo.getCurrency())
                                .imageUrl(url)
                                .build();
                    });

            case "collection" -> this.productRepository
                    .fetchByProductByCollectionClient(uuid, PageRequest.of(page, size))
                    .map(pojo -> {
                        var url = this.s3Service.getPreSignedUrl(bool, BUCKET, pojo.getKey());
                        return ProductResponse.builder()
                                .collection(pojo.getCollection())
                                .id(pojo.getUuid())
                                .name(pojo.getName())
                                .desc(pojo.getDesc())
                                .price(pojo.getPrice())
                                .currency(pojo.getCurrency())
                                .imageUrl(url)
                                .build();
                    });

            default -> throw new CustomNotFoundException("Invalid key: " + key);
        };
    }

    /**
     * Returns a SseEmitter. Where the payload is a List of DetailResponse objects
     *
     * @param uuid is the uuid of the product
     * @return SseEmitter
     */
    public SseEmitter productDetailsByProductUUID(String uuid) {
        boolean bool = ACTIVEPROFILE.equals("prod") || ACTIVEPROFILE.equals("stage");

        var details = this.productDetailRepo
                .productDetailsByProductUUIDClient(uuid) //
                .stream() //
                .map(pojo -> {
                    var urls = Arrays.stream(pojo.getImage().split(","))
                            .map(key -> this.s3Service.getPreSignedUrl(bool, BUCKET, key))
                            .toList();

                    Variant[] variants = this.customUtil
                            .toVariantArray(pojo.getVariants(), ClientProductService.class);

                    return new DetailResponse(pojo.getColour(), urls, variants);
                }) //
                .toList();

        // Send updates every 10 minutes
        SseEmitter sse = new SseEmitter(Duration.ofMinutes(10).toMillis());

        try {
            sse.send(details);
        } catch (IOException e) {
            throw new SseException("Error retrieving Product Details. Please try again later");
        }

        return sse;
    }

}

