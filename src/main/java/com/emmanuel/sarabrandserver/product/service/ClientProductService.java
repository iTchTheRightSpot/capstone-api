package com.emmanuel.sarabrandserver.product.service;

import com.emmanuel.sarabrandserver.aws.S3Service;
import com.emmanuel.sarabrandserver.exception.CustomNotFoundException;
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

import java.util.Arrays;
import java.util.List;

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
    public Page<ProductResponse> fetchAllByUUID(String key, String uuid, int page, int size) {
        boolean bool = ACTIVEPROFILE.equals("prod") || ACTIVEPROFILE.equals("stage");

        return switch (key) {
            // Load or refresh of UI
            case "" -> this.productRepository
                    .fetchAllProductsClient(PageRequest.of(page, Math.min(size, 40))) //
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
                    .fetchProductByCategoryClient(uuid, PageRequest.of(page, Math.min(size, 30))) //
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
                    .fetchByProductByCollectionClient(uuid, PageRequest.of(page, Math.min(size, 30)))
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
     * Method list all ProductDetails bases on a Product uuid. A validation is made to make sure product visibility is
     * true and inventory count is greater than zero.
     *
     * @param uuid is the uuid of the product
     * @return List of DetailResponse
     */
    public List<DetailResponse> productDetailByUUID(String uuid) {

        boolean bool = ACTIVEPROFILE.equals("prod") || ACTIVEPROFILE.equals("stage");

        return this.productDetailRepo.fetchProductDetailByUUIDClient(uuid) //
                .stream() //
                .map(pojo -> {
                    var urls = Arrays.stream(pojo.getImage().split(","))
                            .map(key -> this.s3Service.getPreSignedUrl(bool, BUCKET, key))
                            .toList();

                    Variant[] variants = this.customUtil
                            .toVariantArray(pojo.getVariants(), ClientProductService.class);

                    return DetailResponse.builder()
                            .colour(pojo.getColour())
                            .url(urls)
                            .variants(variants)
                            .build();
                }) //
                .toList();
    }

}

