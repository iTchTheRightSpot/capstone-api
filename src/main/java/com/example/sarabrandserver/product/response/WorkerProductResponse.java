package com.example.sarabrandserver.product.response;

import com.example.sarabrandserver.product.entity.Product;
import com.example.sarabrandserver.product.entity.ProductColour;
import com.example.sarabrandserver.product.entity.ProductDetail;
import com.example.sarabrandserver.product.entity.ProductSize;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WorkerProductResponse {
    private record ImageParser(String key, String path) {}

    // Primitive
    private String name;
    private String sku;
    private BigDecimal price;
    private int qty;
    private long createdAt;
    private long modifiedAt;
    private long deletedAt;

    // None primitive
    private Product product;
    private Set<ProductDetail> productDetail;
    private Set<String> productSize;
    private Set<String> productColour;
    private Set<ImageParser> imageParsers;
    private Set<ImageResponse> imageResponses = new HashSet<>();

    public WorkerProductResponse(Product product) {
        this.product = product;

        // Update primitive data
        setName(product.getName());
        this.product.getProductDetails().forEach(e -> {
            setPrice(e.getPrice());
            setQty(e.getQuantity());
            setSku(e.getSku());
            setCreatedAt(e.getCreateAt().getTime());
            setModifiedAt(e.getModifiedAt().getTime());
            setDeletedAt(e.getDeletedAt().getTime());
        });

        // Update None primitive
        this.productDetail = product.getProductDetails();
        this.productSize = this.productDetail.stream()
                .flatMap(detail -> detail.getProductSizes().stream().map(ProductSize::getSize)) //
                .collect(Collectors.toSet());

        this.productColour = this.productDetail.stream()
                .flatMap(detail -> detail.getProductColours().stream().map(ProductColour::getColour)) //
                .collect(Collectors.toSet());

        this.imageParsers = this.productDetail.stream()
                .flatMap(detail -> detail.getProductImages() //
                        .stream() //
                        .map(productImage -> new ImageParser(productImage.getImageKey(), productImage.getImagePath()))
                ) //
                .collect(Collectors.toSet());
    }

    @JsonProperty(value = "colours")
    public Set<String> colours() {
        return this.productColour;
    }

    @JsonProperty(value = "sizes")
    public Set<String> sized() {
        return this.productSize;
    }

    public Set<ImageParser> imageParsers() {
        return this.imageParsers;
    }

    @JsonProperty(value = "product_name")
    public String getName() {
        return name;
    }

    @JsonProperty(value = "created_at")
    public long getCreatedAt() {
        return createdAt;
    }

    @JsonProperty(value = "modified_at")
    public long getModifiedAt() {
        return modifiedAt;
    }

    @JsonProperty(value = "deleted_at")
    public long getDeletedAt() {
        return deletedAt;
    }

    // TODO fetch from S3 Bucket
    @JsonProperty(value = "image_response")
    public Set<ImageResponse> imageResponses() {
        this.imageParsers.forEach(e -> {
//            ImageResponse imageResponse;
//            try {
//                imageResponse = s3_object_request(product, e.key);
//            } catch (S3Exception | IOException ex) {
//                // This would never be hit because there would always be an image in S3
//                imageResponse = new ImageResponse("", null);
//            }
//            this.imageResponses.add(imageResponse);
        });
        return this.imageResponses;
    }

//    @NotNull
//    private ImageResponse s3_object_request(Product product, String key) throws IOException {
//        ImageResponse imageResponse;
//        GetObjectRequest request = GetObjectRequest.builder()
//                .key(key)
//                .bucket(BUCKET_NAME)
//                .build();
//
//        ResponseBytes<GetObjectResponse> bytes = this.s3Client.getObjectAsBytes(request);
//
//        imageResponse = new ImageResponse(
//                this.fileService.getImageName(product.getPath()),
//                fileService.getMediaType(product.getPath()),
//                bytes.asByteArray()
//        );
//        return imageResponse;
//    }

}
