package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.aws.S3Service;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
import com.emmanuel.sarabrandserver.exception.CustomNotFoundException;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.product.dto.CreateProductDTO;
import com.emmanuel.sarabrandserver.product.dto.DetailDTO;
import com.emmanuel.sarabrandserver.product.dto.ProductDTO;
import com.emmanuel.sarabrandserver.product.entity.*;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.response.DetailResponse;
import com.emmanuel.sarabrandserver.product.response.ProductResponse;
import com.emmanuel.sarabrandserver.util.DateUTC;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.springframework.http.HttpStatus.*;

@Service @Slf4j
public class WorkerProductService {
    @Value(value = "${aws.bucket}")
    private String bucketName;

    private String defaultKey; // Represents the default_image_key for Product property

    private final ProductRepository productRepository;
    private final ProductDetailRepo detailRepo;
    private final WorkerCategoryService categoryService;
    private final DateUTC dateUTC;
    private final WorkerCollectionService collectionService;
    private final S3Service s3Service;
    private final Environment environment;

    public WorkerProductService(
            ProductRepository productRepository,
            ProductDetailRepo detailRepo,
            WorkerCategoryService categoryService,
            DateUTC dateUTC,
            WorkerCollectionService collectionService,
            S3Service s3Service,
            Environment environment
    ) {
        this.productRepository = productRepository;
        this.detailRepo = detailRepo;
        this.categoryService = categoryService;
        this.dateUTC = dateUTC;
        this.collectionService = collectionService;
        this.s3Service = s3Service;
        this.environment = environment;
    }

    /**
     * Method fetches a list of ProductResponse. Note fetchAllProductsWorker query method returns a list of
     * ProductPojo using spring jpa projection. It only returns a Product not Including its details.
     * @param page is the UI page number
     * @param size is the max amount to be displayed on a page
     * @return List of type ProductResponse
     * */
    public Page<ProductResponse> fetchAll(int page, int size) {
        return this.productRepository
                .fetchAllProductsWorker(PageRequest.of(page, Math.min(size, 30)))
                .map(pojo -> new ProductResponse(
                        pojo.getId(),
                        pojo.getName(),
                        pojo.getDesc(),
                        pojo.getPrice().doubleValue(),
                        pojo.getCurrency(),
                        pojo.getKey()
                ));
    }

    /**
     * Method returns a list of DetailResponse. Note findDetailByProductNameWorker query method returns a list of
     * DetailPojo using spring jpa projection.
     * @param name is the name of the product
     * @param page is amount of size based on the page
     * @param size is the amount of items list. Note the max is set to 30 as to not overload Heap memory
     * @return List of DetailResponse
     * */
    public Page<DetailResponse> fetchAll(String name, int page, int size) {
        return this.productRepository
                .findDetailByProductNameWorker(name, PageRequest.of(page, Math.min(size, 30))) //
                .map(p -> new DetailResponse(
                        p.getSku(),
                        p.getVisible(),
                        p.getSize(),
                        p.getQty(),
                        p.getColour(),
                        p.getKey()
                ));
    }

    /**
     * Method is responsible for creating and saving a Product. Note the gotcha is we are only persisting a new
     * ProductDetail if Product exists.
     * @param files of type MultipartFile
     * @param dto of type CreateProductDTO
     * @throws CustomNotFoundException is thrown when category or collection name does not exist
     * @return ResponseEntity of type HttpStatus
     * */
    @Transactional
    public ResponseEntity<HttpStatus> create(CreateProductDTO dto, MultipartFile[] files) {
        var category = this.categoryService.findByName(dto.getCategory().trim());
        var _product = this.productRepository.findByProductName(dto.getName().trim());
        var date = this.dateUTC.toUTC(new Date()).isPresent() ? this.dateUTC.toUTC(new Date()).get() : new Date();

        // Persist new ProductDetail if Product exist
        if (_product.isPresent()) {
            // Build ProductDetail
            var detail = productDetail(dto, files, date);
            detail.setProduct(_product.get());

            // Add ProductDetail to Product, save and return response
            this.detailRepo.save(detail);
            return new ResponseEntity<>(CREATED);
        }

        // Build/Save ProductDetail and Product
        var detail = productDetail(dto, files, date);
        var product = Product.builder()
                .name(dto.getName().trim())
                .description(dto.getDesc().trim())
                .defaultKey(this.defaultKey)
                .price(BigDecimal.valueOf(dto.getPrice()))
                .currency(dto.getCurrency()) // default is USD
                .productDetails(new HashSet<>())
                .build();
        product.addDetail(detail);
        var saved = this.productRepository.save(product);

        // Update Collection if it is not blank
        if (!dto.getCollection().isBlank()) {
            var collection = this.collectionService.findByName(dto.getCollection().trim());
            collection.setModifiedAt(date);
            collection.addProduct(saved);
            this.collectionService.save(collection);
        }

        // Update Category of new Product
        category.addProduct(saved);
        category.setModifiedAt(date);
        this.categoryService.save(category);

        return new ResponseEntity<>(CREATED);
    }

    /**
     * Creates a ProductDetail obj.
     * @param dto represents the details of a Product. i.e. image, colour
     * @param files represents the array of images to upload
     * @param createdAt time of the ProductDetail created
     * @throws S3Exception if an exception happens when uploading to s3
     * @throws IOException if a file does not exist
     * @return ProductDetail
     * */
    private ProductDetail productDetail(CreateProductDTO dto, MultipartFile[] files, Date createdAt) {
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
        for (MultipartFile file : files) {
            String key = UUID.randomUUID().toString();
            setDefaultKey(key); // Set default key
            var image = ProductImage.builder()
                    .imageKey(key)
                    .imagePath(Objects.requireNonNull(file.getOriginalFilename()).trim())
                    .build();

            // Only upload to s3 in prod profile
            if (Arrays.asList(this.environment.getActiveProfiles()).contains("prod")) {
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucketName) // pass as env variable
                        .key(key)
                        .build();
                this.s3Service.uploadToS3(file, request);
            }

            detail.addImages(image);
        }

        return detail;
    }

    /**
     * Method updates just a Product only if the id exists. Not ProductDetail is not updated.
     * @param dto of type UpdateProductDTO
     * @throws CustomNotFoundException when dto product_id does not exist
     * @throws DuplicateException when dto name exist
     * @return ResponseEntity of type HttpStatus
     * */
    @Transactional
    public ResponseEntity<?> updateProduct(final ProductDTO dto) {
        var product = this.productRepository.findProductByProductId(dto.getId())
                .orElseThrow(() -> new CustomNotFoundException("Product does not exist"));

        // Validate if dto name does not equal ProductName and dto does not exist in DB.
        if (!product.getName().equals(dto.getName().trim())
                && this.productRepository.findByProductName(dto.getName().trim()).isPresent()) {
            throw new  DuplicateException(dto.getName() + " exists");
        }

        this.productRepository.updateProduct(
                dto.getId(),
                dto.getName().trim(),
                dto.getDesc().trim(),
                BigDecimal.valueOf(dto.getPrice())
        );

        return new ResponseEntity<>(OK);
    }

    /**
     * Method updates just a ProductDetail. We are not updating the colour because image will have to be updated if
     * it is a different colour.
     * @param dto of type DetailDTO
     * @throws CustomNotFoundException is thrown when Product name or sku does not exist
     * @return ResponseEntity of type HttpStatus
     * */
    @Transactional
    public ResponseEntity<HttpStatus> updateProductDetail(final DetailDTO dto) {
        // Find ProductDetail by it sku
        var detail = findByDetailBySku(dto.getSku().trim());
        var date = this.dateUTC.toUTC(new Date()).isPresent() ? this.dateUTC.toUTC(new Date()).get() : new Date();

        // Fetch type is eager for the properties I am updating
        detail.setModifiedAt(date);
        detail.setVisible(dto.getVisible());
        detail.getProductInventory().setQuantity(dto.getQty());
        detail.getProductSize().setSize(dto.getSize());

        // Save detail
        this.detailRepo.save(detail);

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
        // Only upload to s3 in prod profile
        if (Arrays.asList(this.environment.getActiveProfiles()).contains("prod")) {
            // Get all Images
            List<ObjectIdentifier> keys = this.productRepository.images(name.trim())
                    .stream() //
                    .map(img -> ObjectIdentifier.builder().key(img.getImage()).build()) //
                    .toList();

            this.s3Service.deleteImagesFromS3(keys, bucketName);
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

        if (Arrays.asList(this.environment.getActiveProfiles()).contains("prod")) {
            List<ObjectIdentifier> keys = detail.getProductImages() //
                    .stream() //
                    .map(image -> ObjectIdentifier.builder().key(image.getImageKey()).build())
                    .toList();

            this.s3Service.deleteImagesFromS3(keys, bucketName);
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

    // Set Default Image Key
    private void setDefaultKey(String str) {
        this.defaultKey = str;
    }

}
