package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.exception.CustomNotFoundException;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.product.entity.Product;
import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductImageRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.util.DetailResponse;
import com.emmanuel.sarabrandserver.product.util.ProductDetailDTO;
import com.emmanuel.sarabrandserver.product.util.UpdateProductDetailDTO;
import com.emmanuel.sarabrandserver.product.util.Variant;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Service
public class WorkerProductDetailService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    @Value(value = "${spring.profiles.active}")
    private String ACTIVEPROFILE;

    private final ProductDetailRepo detailRepo;
    private final ProductImageRepo productImageRepo;
    private final ProductRepository productRepository;
    private final CustomUtil customUtil;
    private final HelperService helperService;

    public WorkerProductDetailService(
            ProductDetailRepo detailRepo,
            ProductImageRepo productImageRepo,
            ProductRepository productRepository,
            CustomUtil customUtil,
            HelperService helperService
    ) {
        this.detailRepo = detailRepo;
        this.productImageRepo = productImageRepo;
        this.productRepository = productRepository;
        this.customUtil = customUtil;
        this.helperService = helperService;
    }

    /**
     * Returns a List of ProductDetail based on Product uuid
     *
     * @param uuid is the uuid of the product
     * @return List of DetailResponse
     */
    public List<DetailResponse> fetch(String uuid) {
        boolean bool = this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage");

        return this.detailRepo
                .findProductDetailsByProductUuidWorker(uuid) //
                .stream() //
                .map(pojo -> {
                    var urls = Arrays.stream(pojo.getImage().split(","))
                            .map(key -> this.helperService.generatePreSignedUrl(bool, BUCKET, key))
                            .toList();

                    Variant[] variants = this.customUtil
                            .toVariantArray(pojo.getVariants(), WorkerProductDetailService.class);

                    return DetailResponse.builder()
                            .isVisible(pojo.getVisible())
                            .colour(pojo.getColour())
                            .url(urls)
                            .variants(variants)
                            .build();
                })
                .toList();
    }

    /**
     * Create new ProductDetail
     *
     * @param dto of type ProductDetailDTO
     * @throws CustomNotFoundException is thrown if product uuid does not exist
     * @throws DuplicateException      is thrown if product colour exists
     */
    @Transactional
    public void create(ProductDetailDTO dto, MultipartFile[] multipartFiles) {
        var product = this.productRepository
                .findByProductUuid(dto.getUuid())
                .orElseThrow(() -> new CustomNotFoundException("Product does not exist"));

        // TODO validate colour does not exist
        boolean bool = this.detailRepo.colourExist(dto.getUuid(), dto.getColour()) > 0;

        if (bool) {
            throw new DuplicateException("Colour %s exists".formatted(dto.getColour()));
        }

        var date = this.customUtil.toUTC(new Date()).orElse(new Date());

        // Validate MultipartFile[] are all images
        var files = this.helperService.customMultiPartFiles(multipartFiles, new StringBuilder());

        // Save ProductDetail
        var detail = ProductDetail.builder()
                .product(product)
                .colour(dto.getColour())
                .createAt(date)
                .isVisible(dto.getVisible())
                .productImages(new HashSet<>())
                .skus(new HashSet<>())
                .build();

        // Save ProductDetail
        var saved = this.detailRepo.save(detail);

        // Save ProductSKUs
        this.helperService.saveProductSKUs(dto.getSizeInventory(), saved);

        // Save ProductImages (save to s3)
        this.helperService.productImages(
                detail,
                files,
                this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage"),
                BUCKET
        );
    }

    /**
     * Updates a ProductDetail based on ProductSKU sku
     *
     * @param dto of type DetailDTO
     */
    @Transactional
    public void update(final UpdateProductDetailDTO dto) {
        this.detailRepo.updateProductDetail(
                dto.getSku(),
                dto.getIsVisible(),
                dto.getQty(),
                dto.getSize()
        );
    }

    /**
     * Permanently deletes ProductDetail and its relationship with ProductImages and ProductSKU
     *
     * @param sku is ProductSKU property. ProductSKU has a many to 1 relationship with ProductDetail
     * @throws CustomNotFoundException is thrown when sku does not exist
     * @throws S3Exception             is thrown when deleting from s3
     */
    @Transactional
    public void delete(final String sku) {
        var detail = productDetailByProductSku(sku);

        if (this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage")) {
            var images = this.productImageRepo.imagesByProductDetailID(detail.getProductDetailId());

            List<ObjectIdentifier> keys = images //
                    .stream() //
                    .map(image -> ObjectIdentifier.builder().key(image.getImageKey()).build())
                    .toList();

            if (!keys.isEmpty()) {
                this.helperService.deleteFromS3(keys, BUCKET);
            }
        }

        // Remove detail from Product and Save Product
        this.detailRepo.delete(detail);
    }

    /**
     * Save ProductDetail
     */
    public ProductDetail productDetail(
            final Product product,
            final String colour,
            final boolean visible,
            final Date date
    ) {
        var detail = ProductDetail.builder()
                .product(product)
                .colour(colour)
                .createAt(date)
                .isVisible(visible)
                .productImages(new HashSet<>())
                .skus(new HashSet<>())
                .build();

        // Save ProductDetail
        return this.detailRepo.save(detail);
    }

    /**
     * Find ProductDetail by sku
     */
    public ProductDetail productDetailByProductSku(final String sku) {
        return this.detailRepo
                .findDetailBySku(sku)
                .orElseThrow(() -> new CustomNotFoundException("SKU does not exist"));
    }

}