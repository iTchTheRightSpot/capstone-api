package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.aws.S3Service;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
import com.emmanuel.sarabrandserver.exception.CustomAwsException;
import com.emmanuel.sarabrandserver.exception.CustomNotFoundException;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.product.entity.Product;
import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import com.emmanuel.sarabrandserver.product.entity.ProductImage;
import com.emmanuel.sarabrandserver.product.entity.ProductSku;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductImageRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.repository.ProductSkuRepo;
import com.emmanuel.sarabrandserver.product.util.*;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static org.springframework.http.HttpStatus.*;

@Service
@Slf4j
public class WorkerProductService {
    private final ProductRepository productRepository;
    private final ProductDetailRepo detailRepo;
    private final ProductImageRepo productImageRepo;
    private final ProductSkuRepo productSkuRepo;
    private final WorkerCategoryService categoryService;
    private final CustomUtil customUtil;
    private final WorkerCollectionService collectionService;
    private final S3Service s3Service;
    private final Environment environment;

    public WorkerProductService(
            ProductRepository productRepository,
            ProductDetailRepo detailRepo,
            ProductImageRepo productImageRepo,
            ProductSkuRepo productSkuRepo,
            WorkerCategoryService categoryService,
            CustomUtil customUtil,
            WorkerCollectionService collectionService,
            S3Service s3Service,
            Environment environment
    ) {
        this.productRepository = productRepository;
        this.detailRepo = detailRepo;
        this.productImageRepo = productImageRepo;
        this.productSkuRepo = productSkuRepo;
        this.categoryService = categoryService;
        this.customUtil = customUtil;
        this.collectionService = collectionService;
        this.s3Service = s3Service;
        this.environment = environment;
    }

    /**
     * Method fetches a list of ProductResponse. Note fetchAllProductsWorker query method returns a list of
     * ProductPojo using spring jpa projection. It only returns a Product not Including its details.
     *
     * @param page is the UI page number
     * @param size is the max amount to be displayed on a page
     * @return Page of ProductResponse
     */
    public Page<ProductResponse> fetchAll(int page, int size) {
        var profile = this.environment.getProperty("spring.profiles.active", "");
        boolean bool = profile.equals("prod") || profile.equals("stage");
        var bucket = this.environment.getProperty("aws.bucket", "");

        return this.productRepository
                .fetchAllProductsWorker(PageRequest.of(page, size))
                .map(pojo -> {
                    var url = this.s3Service.getPreSignedUrl(bool, bucket, pojo.getKey());
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
     * Returns a List of ProductDetail based on Product uuid
     *
     * @param uuid is the uuid of the product
     * @return List of DetailResponse
     */
    public List<DetailResponse> productDetailsByProductUUID(String uuid) {
        var profile = this.environment.getProperty("spring.profiles.active", "");
        boolean bool = profile.equals("prod") || profile.equals("stage");
        var bucket = this.environment.getProperty("aws.bucket", "");

        return this.detailRepo
                .findProductDetailsByProductUuidWorker(uuid) //
                .stream()
                .map(pojo -> {
                    var urls = pojo.getImage() //
                            .stream() //
                            .map(image -> this.s3Service.getPreSignedUrl(bool, bucket, image.getImageKey()))
                            .toList();

                    // TODO make more efficient
                    var variants = pojo.getSkus() //
                            .stream() //
                            .map(sku -> new ProductSKUResponse(sku.getSku(), sku.getSize(), sku.getInventory()))
                            .toList();

                    return DetailResponse.builder()
                            .isVisible(pojo.getVisible())
                            .colour(pojo.getColour())
                            .url(urls)
                            .variants(variants)
                            .build();
                }).toList();
    }

    /**
     * Update to creating a new Product.
     *
     * @param files      of type MultipartFile
     * @param dto        of type CreateProductDTO
     * @return ResponseEntity of type HttpStatus
     * @throws CustomNotFoundException is thrown when category or collection name does not exist
     * @throws CustomAwsException      is thrown if File is not an image
     * @throws DuplicateException      is thrown if dto image exists in for Product
     */
    @Transactional
    public ResponseEntity<HttpStatus> create(CreateProductDTO dto, MultipartFile[] files) {
        var category = this.categoryService.findByName(dto.getCategory().trim());
        var _product = this.productRepository.findByProductName(dto.getName().trim());
        var date = this.customUtil.toUTC(new Date()).orElse(new Date());
        var bucket = this.environment.getProperty("aws.bucket", "");
        var profile = this.environment.getProperty("spring.profiles.active", "");
        boolean bool = profile.equals("prod") || profile.equals("stage");

        StringBuilder defaultImageKey = new StringBuilder();
        // Validate MultipartFile[] are all images
        CustomMultiPart[] multiPartFile = validateMultiPartFile(files, defaultImageKey);

        // Persist new ProductDetail if Product exist
        if (_product.isPresent()) {
            // Throw error if Product colour exist
            if (this.detailRepo.colourExist(_product.get().getUuid(), dto.getColour()) > 0) {
                String message = "%s with colour ***** %s ***** exists".formatted(dto.getName(), dto.getColour());
                throw new DuplicateException(message);
            }

            // Existing Product
            var product = _product.get();

            // Save ProductDetails
            var detail = productDetail(product, dto, date);

            // Save ProductSKUs
            saveProductSKUs(dto, detail);

            // Build/Save ProductImages (save to s3)
            productImages(detail, multiPartFile, bool, bucket);

            return new ResponseEntity<>(CREATED);
        }

        // Build Product
        var product = Product.builder()
                .productCategory(category)
                .uuid(UUID.randomUUID().toString())
                .name(dto.getName().trim())
                .description(dto.getDesc().trim())
                .defaultKey(defaultImageKey.toString())
                .price(dto.getPrice())
                .currency(dto.getCurrency()) // default is USD
                .productDetails(new HashSet<>())
                .build();

        // Set ProductCollection to Product
        if (!dto.getCollection().isBlank()) {
            var collection = this.collectionService.findByName(dto.getCollection().trim());
            product.setProductCollection(collection);
        }

        // Save Product
        var saved = this.productRepository.save(product);

        // Save ProductDetails
        var detail = productDetail(saved, dto, date);

        // Save ProductSKUs
        saveProductSKUs(dto, detail);

        // Build ProductImages (save to s3)
        productImages(detail, multiPartFile, bool, bucket);

        return new ResponseEntity<>(CREATED);
    }

    /**
     * Save Product sku. Look in db diagram in read me in case of confusion
     */
    private void saveProductSKUs(CreateProductDTO dto, ProductDetail detail) {
        for (SizeInventoryDTO sizeDto : dto.getSizeInventory()) {
            var sku = ProductSku.builder()
                    .productDetail(detail)
                    .sku(UUID.randomUUID().toString())
                    .size(sizeDto.getSize())
                    .inventory(sizeDto.getQty())
                    .build();
            this.productSkuRepo.save(sku);
        }
    }

    /**
     * Save ProductDetail
     */
    private ProductDetail productDetail(Product product, CreateProductDTO dto, Date date) {
        // ProductDetail
        var detail = ProductDetail.builder()
                .product(product)
                .colour(dto.getColour())
                .createAt(date)
                .isVisible(dto.getVisible())
                .productImages(new HashSet<>())
                .skus(new HashSet<>())
                .build();

        // Save ProductDetail
        return this.detailRepo.save(detail);
    }

    /**
     * Create ProductImage obj and save to s3
     */
    private void productImages(ProductDetail detail, CustomMultiPart[] files, boolean profile, String bucket) {
        for (CustomMultiPart file : files) {
            var obj = ProductImage.builder()
                    .productDetails(detail)
                    .imageKey(file.key())
                    .imagePath(file.file().getAbsolutePath())
                    .build();

            // Upload image to S3 if in desired profile
            if (profile) {
                this.s3Service.uploadToS3(file.file(), file.metadata(), bucket, file.key());
            }

            // Save ProductImage
            this.productImageRepo.save(obj);
        }
    }

    /**
     * Method updates a Product obj based on its UUID. Note only a product is updated not a product detail.
     *
     * @param dto of type UpdateProductDTO
     * @return ResponseEntity of type HttpStatus
     * @throws CustomNotFoundException when dto product_id does not exist
     * @throws DuplicateException      when dto name exist
     */
    @Transactional
    public ResponseEntity<?> updateProduct(final ProductDTO dto) {
        this.productRepository.updateProduct(
                dto.getUuid(),
                dto.getName().trim(),
                dto.getDesc().trim(),
                dto.getPrice()
        );
        return new ResponseEntity<>(OK);
    }

    /**
     * Updates a ProductDetail and its relationship with other tables except ProductImage
     *
     * @param dto of type DetailDTO
     * @return ResponseEntity of type HttpStatus
     */
    @Transactional
    public ResponseEntity<HttpStatus> updateProductDetail(final DetailDTO dto) {
        this.detailRepo.updateProductDetail(
                dto.getSku(),
                dto.getIsVisible(),
                dto.getQty(),
                dto.getSize()
        );
        return new ResponseEntity<>(OK);
    }

    /**
     * Method permanently deletes a Product and children from db.
     *
     * @param uuid is the product uuid
     * @return ResponseEntity of type HttpStatus
     * @throws CustomNotFoundException is thrown when Product id does not exist
     * @throws S3Exception             is thrown when deleting from s3
     */
    @Transactional
    public ResponseEntity<?> deleteProduct(final String uuid) {
        var product = this.productRepository.findByProductUuid(uuid)
                .orElseThrow(() -> new CustomNotFoundException(uuid + " does not exist"));

        var profile = this.environment.getProperty("spring.profiles.active", "");
        var bucket = this.environment.getProperty("aws.bucket", "");

        // Delete from S3
        if (profile.equals("prod") || profile.equals("stage")) {
            // Get all Images
            List<ObjectIdentifier> keys = this.productRepository.images(uuid)
                    .stream() //
                    .map(img -> ObjectIdentifier.builder().key(img.getImage()).build()) //
                    .toList();

            if (!keys.isEmpty()) {
                this.s3Service.deleteFromS3(keys, bucket);
            }
        }

        // Delete from Database
        this.productRepository.delete(product);
        return new ResponseEntity<>(NO_CONTENT);
    }

    /**
     * Method permanently deletes a ProductDetail there by deleting relationship with Product.
     * Note ProductDetail has an EAGER fetch time with ProductImage
     *
     * @param sku is a unique String for each ProductDetail
     * @return ResponseEntity of type HttpStatus
     * @throws CustomNotFoundException is thrown when sku does not exist
     * @throws S3Exception             is thrown when deleting from s3
     */
    @Transactional
    public ResponseEntity<?> deleteProductDetail(final String sku) {
        var detail = findByDetailBySku(sku);

        var profile = this.environment.getProperty("spring.profiles.active", "");
        var bucket = this.environment.getProperty("aws.bucket", "");

        if (profile.equals("prod") || profile.equals("stage")) {
            List<ObjectIdentifier> keys = detail.getProductImages() //
                    .stream() //
                    .map(image -> ObjectIdentifier.builder().key(image.getImageKey()).build())
                    .toList();

            if (!keys.isEmpty()) {
                this.s3Service.deleteFromS3(keys, bucket);
            }
        }

        // Remove detail from Product and Save Product
        this.detailRepo.delete(detail);
        return new ResponseEntity<>(NO_CONTENT);
    }

    // Find ProductDetail by sku
    private ProductDetail findByDetailBySku(String sku) {
        return this.productRepository.findDetailBySku(sku)
                .orElseThrow(() -> new CustomNotFoundException("SKU does not exist"));
    }

    /**
     * Validates if items in MultipartFile array are all images, else an error is thrown.
     * Note I am returning an array as it is a bit more efficient than arraylist in terms of memory
     *
     * @param multipartFiles is an array of MultipartFile
     * @return CustomMultiPart array
     */
    private CustomMultiPart[] validateMultiPartFile(MultipartFile[] multipartFiles, StringBuilder defaultKey) {
        CustomMultiPart[] arr = new CustomMultiPart[multipartFiles.length];

        for (int i = 0; i < multipartFiles.length; i++) {
            String originalFileName = Objects.requireNonNull(multipartFiles[i].getOriginalFilename());

            File file = new File(originalFileName);

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                // write MultipartFile to file
                outputStream.write(multipartFiles[i].getBytes());

                // Validate file is an image
                String contentType = Files.probeContentType(file.toPath());
                if (!contentType.startsWith("image/")) {
                    log.error("File is not an image");
                    throw new CustomAwsException("File is not an image");
                }

                // Create image metadata for storing in AWS
                Map<String, String> metadata = new HashMap<>();
                metadata.put("Content-Type", contentType);
                metadata.put("Title", originalFileName);
                metadata.put("Type", StringUtils.getFilenameExtension(originalFileName));

                // Default key
                String key = UUID.randomUUID().toString();
                if (defaultKey.isEmpty()) {
                    defaultKey.append(key);
                }

                // Copy into array
                arr[i] = new CustomMultiPart(file, metadata, key);
            } catch (IOException ex) {
                log.error("Error either writing multipart to file or getting file type. {}", ex.getMessage());
                throw new CustomAwsException("Please verify file is an image");
            }
        }
        return arr;
    }

}
