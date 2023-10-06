package com.sarabrandserver.product.service;

import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.SseException;
import com.sarabrandserver.product.repository.ProductDetailRepo;
import com.sarabrandserver.product.repository.ProductRepository;
import com.sarabrandserver.product.response.DetailResponse;
import com.sarabrandserver.product.response.ProductResponse;
import com.sarabrandserver.product.response.Variant;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class ClientProductService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    @Value(value = "${spring.profiles.active}")
    private String ACTIVEPROFILE;

    private final ProductRepository productRepository;
    private final ProductDetailRepo productDetailRepo;
    private final S3Service s3Service;
    private final CustomUtil customUtil;

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
     * Returns a List of DetailResponse as a SseEmitter. Note response is sent every
     * 10 mins after the initial handshake. Article on SseEmitter
     * <a href="https://howtodoinjava.com/spring-boot/spring-async-controller-sseemitter/">...</a>
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

        // TODO implement ExecutorService due to returning getPreSignedUrl
        SseEmitter sse = new SseEmitter(Duration.ofMinutes(10).toMillis());

        try {
            sse.send(details);
        } catch (IOException e) {
            sse.completeWithError(e);
            throw new SseException("Error retrieving Product Details. Please try again later");
        }

        return sse;
    }

}

