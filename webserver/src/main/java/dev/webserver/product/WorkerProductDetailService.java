package dev.webserver.product;

import dev.webserver.exception.CustomNotFoundException;
import dev.webserver.exception.DuplicateException;
import dev.webserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class WorkerProductDetailService {

    @Value(value = "${aws.bucket}")
    @Setter
    private String bucket;

    private final ProductDetailRepository detailRepo;
    private final ProductSkuService skuService;
    private final ProductImageRepository imageRepo;
    private final ProductRepository productRepository;
    private final ProductImageService productImageService;

    /**
     * Retrieves {@link ProductDetail} asynchronously by the specified {@link Product} uuid.
     * Each {@link ProductDetail} consists of various attributes such as visibility, color,
     * URLs, and variants.
     *
     * @param uuid The uuid of the {@link Product} for which details are to be retrieved.
     * @return A list of {@link DetailResponse} objects, representing the {@link ProductDetail}.
     * Each {@link DetailResponse} object encapsulates information such as visibility, color, URLs, and variants.
     */
    public List<DetailResponse> productDetailsByProductUuid(String uuid) {
        var futures = detailRepo
                .productDetailsByProductUuidAdminFront(uuid)
                .stream()
                .map(pojo -> (Supplier<DetailResponse>) () -> {
                    var req = Arrays
                            .stream(pojo.imageKey().split(","))
                            .map(key -> (Supplier<String>) () -> productImageService.preSignedUrl(bucket, key))
                            .toList();

                    var urls = CustomUtil.asynchronousTasks(req).join();

                    var variants = CustomUtil.toVariantArray(pojo.variants(), WorkerProductDetailService.class);

                    return DetailResponse.builder()
                            .isVisible(pojo.isVisible())
                            .colour(pojo.colour())
                            .urls(urls)
                            .variants(variants)
                            .build();
                })
                .toList();

        return CustomUtil.asynchronousTasks(futures).join();
    }

    /**
     * Create new {@link ProductDetail}.
     *
     * @param dto of type {@link ProductDetailDto}.
     * @throws CustomNotFoundException is thrown if product uuid does not exist.
     * @throws DuplicateException      is thrown if product colour exists.
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(ProductDetailDto dto, MultipartFile[] multipartFiles) {
        var product = productRepository
                .productByUuid(dto.uuid())
                .orElseThrow(() -> new CustomNotFoundException("Product does not exist"));

        Optional<ProductDetail> exist = detailRepo.productDetailByColour(dto.colour());

        if (exist.isPresent()) {
            skuService.save(dto.sizeInventory(), exist.get());
            return;
        }

        var files = CustomUtil.transformMultipartFile.apply(multipartFiles, new StringBuilder());

        // save ProductDetail
        var detail = ProductDetail.builder()
                .productId(product.productId())
                .colour(dto.colour())
                .createAt(CustomUtil.TO_GREENWICH.apply(null))
                .isVisible(dto.visible())
                .build();

        // save ProductDetail
        var saved = detailRepo.save(detail);

        // save ProductSKU
        skuService.save(dto.sizeInventory(), saved);

        productImageService.saveProductImages(detail, files, bucket);
    }

    /**
     * Updates a {@link ProductDetail} based on {@link ProductSku} sku
     *
     * @param dto of type {@link UpdateProductDetailDto}.
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(final UpdateProductDetailDto dto) {
        detailRepo.updateProductSkuAndProductDetailByProductSku(
                dto.sku(),
                dto.colour(),
                dto.isVisible(),
                dto.qty(),
                dto.size()
        );
    }

    /**
     * Permanently deletes {@link ProductDetail} and its relationship with
     * {@link ProductImage}s and {@link ProductSku}.
     *
     * @param sku is {@link ProductSku} property.
     *            {@link ProductSku} has a many to 1 relationship with {@link ProductDetail}.
     * @throws CustomNotFoundException is thrown when sku does not exist.
     * @throws S3Exception             is thrown when deleting from s3.
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(final String sku) {
        var detail = productDetailByProductSku(sku);

        var images = imageRepo.imagesByProductDetailId(detail.detailId());

        List<ObjectIdentifier> keys = images //
                .stream() //
                .map(image -> ObjectIdentifier.builder().key(image.imageKey()).build())
                .toList();

        if (!keys.isEmpty()) {
            productImageService.deleteFromS3(keys, bucket);
        }

        // permanently delete
        detailRepo.delete(detail);
    }

    /**
     * Save ProductDetail
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductDetail productDetail(
            final Product product,
            final String colour,
            final boolean visible,
            final LocalDateTime date
    ) {
        var detail = ProductDetail.builder()
                .productId(product.productId())
                .colour(colour)
                .createAt(date)
                .isVisible(visible)
                .build();

        // save ProductDetail
        return detailRepo.save(detail);
    }

    /**
     * Returns a {@link ProductDetail} by a {@link ProductSku} property
     * sku.
     *
     * @param sku is a unique string for every {@link ProductSku} object.
     * @return a {@link ProductDetail} a parent of a {@link ProductSku}.
     * @throws CustomNotFoundException if no {@link ProductDetail} is found.
     */
    public ProductDetail productDetailByProductSku(final String sku) {
        return detailRepo
                .productDetailByProductSku(sku)
                .orElseThrow(() -> new CustomNotFoundException("SKU does not exist"));
    }

}