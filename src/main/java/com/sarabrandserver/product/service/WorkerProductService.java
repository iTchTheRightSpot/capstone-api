package com.sarabrandserver.product.service;

import com.sarabrandserver.category.service.WorkerCategoryService;
import com.sarabrandserver.collection.service.WorkerCollectionService;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomAwsException;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.exception.ResourceAttachedException;
import com.sarabrandserver.product.dto.CreateProductDTO;
import com.sarabrandserver.product.dto.UpdateProductDTO;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.repository.ProductRepository;
import com.sarabrandserver.product.response.ProductResponse;
import com.sarabrandserver.stripe.PriceCurrencyPair;
import com.sarabrandserver.stripe.StripeService;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Setter
public class WorkerProductService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    @Value(value = "${spring.profiles.active}")
    private String ACTIVEPROFILE;

    private final StripeService stripeService;
    private final ProductRepository productRepository;
    private final WorkerProductDetailService workerProductDetailService;
    private final ProductSKUService productSKUService;
    private final WorkerCategoryService categoryService;
    private final WorkerCollectionService collectionService;
    private final CustomUtil customUtil;
    private final HelperService helperService;

    /**
     * Method fetches a list of ProductResponse. Note fetchAllProductsWorker query method returns a list of
     * ProductPojo using spring jpa projection. It only returns a Product not Including its details.
     *
     * @param page is the UI page number
     * @param size is the max amount to be displayed on a page
     * @return Page of ProductResponse
     */
    public Page<ProductResponse> fetchAll(int page, int size) {
        boolean bool = this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage");

        return this.productRepository
                .fetchAllProductsWorker(PageRequest.of(page, size))
                .map(pojo -> {
                    var url = this.helperService.generatePreSignedUrl(bool, BUCKET, pojo.getKey());
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
    }

    /**
     * Create a new Product.
     *
     * @param files of type MultipartFile
     * @param dto   of type CreateProductDTO
     * @throws CustomNotFoundException is thrown when category or collection name does not exist
     * @throws CustomAwsException      is thrown if File is not an image
     * @throws DuplicateException      is thrown if dto image exists in for Product
     */
    @Transactional
    public void create(CreateProductDTO dto, MultipartFile[] files) {
        var category = this.categoryService.findByName(dto.category().trim());
        var _product = this.productRepository.findByProductName(dto.name().trim());

        // throw error if product exits
        if (_product.isPresent()) {
            throw new DuplicateException(dto.name() + " exists");
        }

        // Validate MultipartFile[] are all images
        StringBuilder defaultImageKey = new StringBuilder();
        var file = this.helperService.customMultiPartFiles(files, defaultImageKey);

        var productID = this.stripeService
                .createProduct(
                        dto.name(),
                        new PriceCurrencyPair(45000L, SarreCurrency.NGN),
                        new PriceCurrencyPair(1000L, SarreCurrency.USD)
                );

        // Build Product
        var product = Product.builder()
                .productCategory(category)
                .uuid(productID)
                .name(dto.name().trim())
                .description(dto.desc().trim())
                .defaultKey(defaultImageKey.toString())
                .price(dto.price())
                .currency(dto.currency()) // default is NGN
                .productDetails(new HashSet<>())
                .build();

        // Set ProductCollection to Product
        if (!dto.collection().isBlank()) {
            var collection = this.collectionService.findByName(dto.collection().trim());
            product.setProductCollection(collection);
        }

        // Save Product
        var saved = this.productRepository.save(product);

        // Save ProductDetails
        var date = this.customUtil.toUTC(new Date()).orElse(new Date());
        var detail = this.workerProductDetailService.
                productDetail(saved, dto.colour(), dto.visible(), date);

        // Save ProductSKUs
        this.productSKUService.saveProductSKUs(dto.sizeInventory(), detail);

        // Build ProductImages (save to s3)
        this.helperService.productImages(
                detail,
                file,
                this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage"),
                BUCKET
        );
    }

    /**
     * Method updates a Product obj based on its UUID.
     *
     * @param dto of type UpdateProductDTO
     * @throws CustomNotFoundException when dto category_id or collection_id does not exist
     * @throws DuplicateException      when new product name exist but not associated to product uuid
     */
    @Transactional
    public void update(final UpdateProductDTO dto) {
        boolean bool = this.productRepository
                .nameNotAssociatedToUuid(dto.uuid(), dto.name()) > 0;

        if (bool) {
            throw new DuplicateException(dto.name() + " exists");
        }

        var category = this.categoryService.findByUuid(dto.categoryId());

        if (!dto.collection().isEmpty()) {
            // Find ProductCollection by uuid
            var collection = this.collectionService.findByUuid(dto.collectionId());

            this.productRepository.updateProductCategoryCollectionPresent(
                    dto.uuid(),
                    dto.name().trim(),
                    dto.desc().trim(),
                    dto.price(),
                    category,
                    collection
            );

            return;
        }

        this.productRepository.updateProductCollectionNotPresent(
                dto.uuid(),
                dto.name().trim(),
                dto.desc().trim(),
                dto.price(),
                category
        );
    }

    /**
     * Permanently deletes a Product db.
     *
     * @param uuid is the product uuid
     * @throws CustomNotFoundException   is thrown when Product id does not exist
     * @throws ResourceAttachedException is thrown if Product has ProductDetails attached
     * @throws S3Exception               is thrown when deleting from s3
     */
    @Transactional
    public void delete(final String uuid) {
        var product = this.productRepository.findByProductUuid(uuid)
                .orElseThrow(() -> new CustomNotFoundException(uuid + " does not exist"));

        boolean bool = this.productRepository.productDetailAttach(uuid) > 1;

        if (bool) {
            String message = "Cannot delete %s as it has many Variants".formatted(product.getName());
            throw new ResourceAttachedException(message);
        }

        // Delete from S3
        if (this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage")) {
            // Get all Images
            List<ObjectIdentifier> keys = this.productRepository.productImagesByProductUUID(uuid)
                    .stream() //
                    .map(img -> ObjectIdentifier.builder().key(img.getImage()).build()) //
                    .toList();

            if (!keys.isEmpty()) {
                this.helperService.deleteFromS3(keys, BUCKET);
            }
        }

        // Delete from Database
        this.productRepository.delete(product);
    }

}