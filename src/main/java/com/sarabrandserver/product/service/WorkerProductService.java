package com.sarabrandserver.product.service;

import com.sarabrandserver.category.service.WorkerCategoryService;
import com.sarabrandserver.collection.service.WorkerCollectionService;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.*;
import com.sarabrandserver.product.dto.CreateProductDTO;
import com.sarabrandserver.product.dto.UpdateProductDTO;
import com.sarabrandserver.product.entity.PriceCurrency;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.repository.PriceCurrencyRepo;
import com.sarabrandserver.product.repository.ProductRepo;
import com.sarabrandserver.product.response.ProductResponse;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static java.math.RoundingMode.FLOOR;

@Service
@RequiredArgsConstructor
@Setter
public class WorkerProductService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private final PriceCurrencyRepo priceCurrencyRepo;
    private final ProductRepo productRepo;
    private final WorkerProductDetailService workerProductDetailService;
    private final ProductSKUService productSKUService;
    private final WorkerCategoryService categoryService;
    private final WorkerCollectionService collectionService;
    private final CustomUtil customUtil;
    private final HelperService helperService;

    /**
     * Method returns a list of ProductResponse. Note fetchAllProductsWorker query method returns a list of
     * ProductPojo using spring jpa projection. It only returns a Product not Including its details.
     *
     * @param currency is of SarreCurrency
     * @param page is the UI page number
     * @param size is the max amount to be displayed on a page
     * @return Page of ProductResponse
     */
    public Page<ProductResponse> allProducts(SarreCurrency currency, int page, int size) {
        return this.productRepo
                .fetchAllProductsWorker(currency, PageRequest.of(page, size))
                .map(pojo -> {
                    var url = this.helperService.preSignedURL(BUCKET, pojo.getKey());
                    return ProductResponse.builder()
                            .category(pojo.getCategory())
                            .collection(pojo.getCollection())
                            .id(pojo.getUuid())
                            .name(pojo.getName())
                            .desc(pojo.getDescription())
                            .price(pojo.getPrice().setScale(2, FLOOR))
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
     * @throws CustomNotFoundException is thrown if category or collection name does not exist in database
     * or currency passed in truncateAmount does not contain in dto property priceCurrency
     * @throws CustomAwsException      is thrown if File is not an image
     * @throws DuplicateException      is thrown if dto image exists in for Product
     */
    @Transactional
    public void create(CreateProductDTO dto, MultipartFile[] files) {
        if (!this.customUtil.validateContainsCurrencies(dto.priceCurrency())) {
            throw new CustomInvalidFormatException("please check currencies and prices");
        }

        var category = this.categoryService.findByName(dto.category().trim());

        // throw error if product exits
        if (this.productRepo.findByProductName(dto.name().trim()).isPresent()) {
            throw new DuplicateException(dto.name() + " exists");
        }

        // Validate MultipartFile[] are all images
        StringBuilder defaultImageKey = new StringBuilder();
        var file = this.helperService.customMultiPartFiles(files, defaultImageKey);

        // Build Product
        var p = Product.builder()
                .productCategory(category)
                .uuid(UUID.randomUUID().toString())
                .name(dto.name().trim())
                .description(dto.desc().trim())
                .defaultKey(defaultImageKey.toString())
                .productDetails(new HashSet<>())
                .build();

        // Set ProductCollection to Product
        if (!dto.collection().isBlank()) {
            var collection = this.collectionService.findByName(dto.collection().trim());
            p.setProductCollection(collection);
        }

        // Save Product
        var product = this.productRepo.save(p);

        // save ngn & usd price
        BigDecimal ngn = this.customUtil.truncateAmount(dto.priceCurrency(), NGN);
        BigDecimal usd = this.customUtil.truncateAmount(dto.priceCurrency(), USD);
        this.priceCurrencyRepo.save(new PriceCurrency(ngn, NGN, product));
        this.priceCurrencyRepo.save(new PriceCurrency(usd, USD, product));

        // Save ProductDetails
        var date = this.customUtil.toUTC(new Date());
        var detail = this.workerProductDetailService.
                productDetail(product, dto.colour(), dto.visible(), date);

        // Save ProductSKUs
        this.productSKUService.save(dto.sizeInventory(), detail);

        // build and save ProductImages (save to s3)
        this.helperService.productImages(
                detail,
                file,
                BUCKET
        );
    }

    /**
     * Method updates a Product obj based on its UUID.
     *
     * @param dto of type UpdateProductDTO
     * @throws CustomNotFoundException when dto category_id or collection_id does not exist
     * @throws DuplicateException      when new product name exist but not associated to product uuid
     * @throws CustomInvalidFormatException if price is less than zero
     */
    @Transactional
    public void update(final UpdateProductDTO dto) {
        if (dto.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomInvalidFormatException("price cannot be zero");
        }

        BigDecimal price = dto.price().setScale(2, RoundingMode.FLOOR);

        boolean bool = this.productRepo
                .nameNotAssociatedToUuid(dto.uuid(), dto.name()) > 0;

        if (bool) {
            throw new DuplicateException(dto.name() + " exists");
        }

        var category = this.categoryService.findByUuid(dto.categoryId());

        if (dto.collection().isEmpty()) {
            this.productRepo.update_product_where_collection_not_present(
                    dto.uuid(),
                    dto.name().trim(),
                    dto.desc().trim(),
                    category
            );

        } else {
            var collection = this.collectionService.productCollectionByUUID(dto.collectionId());

            this.productRepo.update_product_where_category_and_collection_are_present(
                    dto.uuid(),
                    dto.name().trim(),
                    dto.desc().trim(),
                    category,
                    collection
            );
        }

        // update price
        var currency = SarreCurrency.valueOf(dto.currency().toUpperCase());
        this.priceCurrencyRepo.updatePriceByProductUUID(dto.uuid(), price, currency);
    }

    // TODO validate product isn't in user session and it is not in order detail
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
        var product = this.productRepo.findByProductUuid(uuid)
                .orElseThrow(() -> new CustomNotFoundException(uuid + " does not exist"));

        boolean bool = this.productRepo.productDetailAttach(uuid) > 1;

        if (bool) {
            String message = "cannot delete %s as it has many variants".formatted(product.getName());
            throw new ResourceAttachedException(message);
        }

        // Delete from S3
        List<ObjectIdentifier> keys = this.productRepo.productImagesByProductUUID(uuid)
                .stream() //
                .map(img -> ObjectIdentifier.builder().key(img.getImage()).build()) //
                .toList();

        if (!keys.isEmpty()) {
            this.helperService.deleteFromS3(keys, BUCKET);
        }

        // Delete from Database
        this.productRepo.delete(product);
    }

}