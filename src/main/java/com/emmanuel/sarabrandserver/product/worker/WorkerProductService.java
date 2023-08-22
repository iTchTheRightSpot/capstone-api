package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.aws.S3Service;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
import com.emmanuel.sarabrandserver.exception.CustomAwsException;
import com.emmanuel.sarabrandserver.exception.CustomNotFoundException;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.product.entity.*;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
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

@Service @Slf4j
public class WorkerProductService {
    private final ProductRepository productRepository;
    private final ProductDetailRepo detailRepo;
    private final WorkerCategoryService categoryService;
    private final CustomUtil customUtil;
    private final WorkerCollectionService collectionService;
    private final S3Service s3Service;
    private final Environment environment;

    public WorkerProductService(
            ProductRepository productRepository,
            ProductDetailRepo detailRepo,
            WorkerCategoryService categoryService,
            CustomUtil customUtil,
            WorkerCollectionService collectionService,
            S3Service s3Service,
            Environment environment
    ) {
        this.productRepository = productRepository;
        this.detailRepo = detailRepo;
        this.categoryService = categoryService;
        this.customUtil = customUtil;
        this.collectionService = collectionService;
        this.s3Service = s3Service;
        this.environment = environment;
    }

    private record CustomProductDetail(ProductDetail detail, String imageDefaultKey) { }

    /**
     * Method fetches a list of ProductResponse. Note fetchAllProductsWorker query method returns a list of
     * ProductPojo using spring jpa projection. It only returns a Product not Including its details.
     * @param page is the UI page number
     * @param size is the max amount to be displayed on a page
     * @return Page of ProductResponse
     * */
    public Page<ProductResponse> fetchAll(int page, int size) {
        var profile = this.environment.getProperty("spring.profiles.active", "");
        boolean bool = profile.equals("prod") || profile.equals("stage");
        var bucket = this.environment.getProperty("aws.bucket", "");

        return this.productRepository
                .fetchAllProductsWorker(PageRequest.of(page, size))
                .map(pojo -> {

                    var url = this.s3Service.getPreSignedUrl(bool, bucket, pojo.getKey());
                    return ProductResponse.builder()
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
     * Method returns a list of DetailResponse. Note findDetailByProductNameWorker query method returns a list of
     * DetailPojo using spring jpa projection.
     * @param name is the name of the product
     * @param page is amount of size based on the page
     * @param size is the amount of items list. Note the max is set to 30 as to not overload Heap memory
     * @return Page of DetailResponse
     * */
    public Page<DetailResponse> fetchAll(String name, int page, int size) {
        var profile = this.environment.getProperty("spring.profiles.active", "");
        boolean bool = profile.equals("prod") || profile.equals("stage");
        var bucket = this.environment.getProperty("aws.bucket", "");

        return this.productRepository
                .findDetailByProductNameWorker(name, PageRequest.of(page, size)) //
                .map(pojo -> {
                    var urls = Arrays.stream(pojo.getKey().split(","))
                            .map(key -> this.s3Service.getPreSignedUrl(bool, bucket, key))
                            .toList();

                    return DetailResponse.builder()
                            .sku(pojo.getSku())
                            .isVisible(pojo.getVisible())
                            .size(pojo.getSize())
                            .qty(pojo.getQty())
                            .colour(pojo.getColour())
                            .url(urls)
                            .build();
                });
    }

    /**
     * Method is responsible for creating and saving a Product. Note the gotcha is we are only persisting a new
     * ProductDetail if Product exists.
     * @param files of type MultipartFile
     * @param dto of type CreateProductDTO
     * @throws CustomNotFoundException is thrown when category or collection name does not exist
     * @throws CustomAwsException is thrown if File is not an image
     * @return ResponseEntity of type HttpStatus
     * */
    @Transactional
    public ResponseEntity<HttpStatus> create(CreateProductDTO dto, MultipartFile[] files) {
        var category = this.categoryService.findByName(dto.getCategory().trim());
        var _product = this.productRepository.findByProductName(dto.getName().trim());
        var date = this.customUtil.toUTC(new Date()).orElse(new Date());

        // Validate MultipartFile[] are all images
        CustomMultiPart[] list = validateMultiPartFile(files);
        StringBuilder defaultImageKey = new StringBuilder();

        // Persist new ProductDetail if Product exist
        if (_product.isPresent()) {
            // Build ProductDetail
            var detail = productDetail(dto, defaultImageKey, list, date).detail();
            detail.setProduct(_product.get());

            // Add ProductDetail to Product, save and return response
            this.detailRepo.save(detail);
            return new ResponseEntity<>(CREATED);
        }

        // Build/Save ProductDetail and Product
        var customProductDetail = productDetail(dto, defaultImageKey, list, date);
        var product = Product.builder()
                .productCategory(category)
                .uuid(UUID.randomUUID().toString())
                .name(dto.getName().trim())
                .description(dto.getDesc().trim())
                .defaultKey(customProductDetail.imageDefaultKey())
                .price(dto.getPrice())
                .currency(dto.getCurrency()) // default is USD
                .productDetails(new HashSet<>())
                .build();
        product.addDetail(customProductDetail.detail());

        // Set ProductCollection to Product
        if (!dto.getCollection().isBlank()) {
            var collection = this.collectionService.findByName(dto.getCollection().trim());
            product.setProductCollection(collection);
        }

        this.productRepository.save(product);

        return new ResponseEntity<>(CREATED);
    }

    /**
     * Creates a ProductDetail obj.
     * @param dto represents the details of a Product. i.e. image, colour
     * @param defaultKey is the default image key for a product
     * @param files represents a list of CustomMultiPart
     * @param createdAt time of the ProductDetail created
     * @throws S3Exception if an exception happens when uploading to s3
     * @return ProductDetail
     * */
    private CustomProductDetail productDetail(
            CreateProductDTO dto,
            StringBuilder defaultKey,
            CustomMultiPart[] files,
            Date createdAt
    ) {
        var bucket = this.environment.getProperty("aws.bucket", "");
        var profile = this.environment.getProperty("spring.profiles.active", "");

        // ProductSize
        var size = ProductSize.builder()
                .size(dto.getSize())
                .productDetails(new HashSet<>())
                .build();
        // ProductInventory
        var inventory = ProductInventory.builder()
                .quantity(dto.getQty())
                .productDetails(new HashSet<>())
                .build();

        // ProductColour
        var colour = ProductColour.builder()
                .colour(dto.getColour())
                .productDetails(new HashSet<>())
                .build();
        // ProductDetail
        var detail = ProductDetail.builder()
                .createAt(createdAt)
                .modifiedAt(null)
                .sku(UUID.randomUUID().toString())
                .isVisible(dto.getVisible())
                .productImages(new HashSet<>())
                .build();
        detail.setProductSize(size);
        detail.setProductInventory(inventory);
        detail.setProductColour(colour);

        // Add ProductImage to ProductDetail as ProductDetail has a one-to-many relationship with ProductImage
        for (CustomMultiPart obj : files) {
            // Set default key
            if (defaultKey.isEmpty()) {
                defaultKey.append(obj.key());
            }
            var image = ProductImage.builder()
                    .imageKey(obj.key())
                    .imagePath(obj.file().getAbsolutePath())
                    .build();

            // Only upload to s3 in prod profile
            if (profile.equals("prod") || profile.equals("stage")) {
                this.s3Service.uploadToS3(obj.file(), obj.metadata(), bucket, obj.key());
            }

            detail.addImages(image);
        }

        return new CustomProductDetail(detail, defaultKey.toString());
    }

    /**
     * Method updates a Product obj based on its UUID. Note only a product is updated not a product detail.
     * @param dto of type UpdateProductDTO
     * @throws CustomNotFoundException when dto product_id does not exist
     * @throws DuplicateException when dto name exist
     * @return ResponseEntity of type HttpStatus
     * */
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
     * @param dto of type DetailDTO
     * @return ResponseEntity of type HttpStatus
     * */
    @Transactional
    public ResponseEntity<HttpStatus> updateProductDetail(final DetailDTO dto) {
        var date = this.customUtil.toUTC(new Date()).orElse(new Date());
        this.detailRepo.updateProductDetail(
                dto.getSku(),
                date,
                dto.getIsVisible(),
                dto.getQty(),
                dto.getSize()
        );
        return new ResponseEntity<>(OK);
    }

    /**
     * Method permanently deletes a Product and children from db.
     * @param name is the product name
     * @throws CustomNotFoundException is thrown when Product id does not exist
     * @throws S3Exception is thrown when deleting from s3
     * @return ResponseEntity of type HttpStatus
     * */
    @Transactional
    public ResponseEntity<?> deleteProduct(final String name) {
        var product = this.productRepository.findByProductName(name.trim())
                .orElseThrow(() -> new CustomNotFoundException(name + " does not exist"));;

        // Delete from S3
        var profile = this.environment.getProperty("spring.profiles.active", "");
        var bucket = this.environment.getProperty("aws.bucket", "");

        if (profile.equals("prod") || profile.equals("stage"))  {
            // Get all Images
            List<ObjectIdentifier> keys = this.productRepository.images(name.trim())
                    .stream() //
                    .map(img -> ObjectIdentifier.builder().key(img.getImage()).build()) //
                    .toList();

            if (!keys.isEmpty()) {
                this.s3Service.deleteFromS3(keys, bucket);
            }
        }

        this.productRepository.delete(product);
        return new ResponseEntity<>(NO_CONTENT);
    }

    /**
     * Method permanently deletes a ProductDetail there by deleting relationship with Product.
     * Note ProductDetail has an EAGER fetch time with ProductImage
     * @param sku is a unique String for each ProductDetail
     * @throws CustomNotFoundException is thrown when sku does not exist
     * @throws S3Exception is thrown when deleting from s3
     * @return ResponseEntity of type HttpStatus
     * */
    @Transactional
    public ResponseEntity<?> deleteProductDetail(final String sku) {
        var detail = findByDetailBySku(sku);

        var profile = this.environment.getProperty("spring.profiles.active", "");
        var bucket = this.environment.getProperty("aws.bucket", "");

        if (profile.equals("prod") || profile.equals("stage"))  {
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
     * @param multipartFiles is an array of MultipartFile
     * @return CustomMultiPart array
     * */
    private CustomMultiPart[] validateMultiPartFile(MultipartFile[] multipartFiles) {
        CustomMultiPart[] arr = new CustomMultiPart[multipartFiles.length];

        for (int i = 0; i < multipartFiles.length; i++) {
            String originalFileName = Objects
                    .requireNonNull(multipartFiles[i].getOriginalFilename()); // Possibly throws a NullPointerException
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

                // Copy into array
                arr[i] = new CustomMultiPart(file, metadata, UUID.randomUUID().toString());
            } catch (IOException ex) {
                log.error("Error either writing multipart to file or getting file type. {}", ex.getMessage());
                throw new CustomAwsException("Please verify file is an image");
            }
        }
        return arr;
    }

}
