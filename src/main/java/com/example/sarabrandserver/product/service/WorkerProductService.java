package com.example.sarabrandserver.product.service;

import com.example.sarabrandserver.category.service.WorkerCategoryService;
import com.example.sarabrandserver.collection.service.WorkerCollectionService;
import com.example.sarabrandserver.exception.CustomNotFoundException;
import com.example.sarabrandserver.product.dto.CreateProductDTO;
import com.example.sarabrandserver.product.dto.UpdateProductDTO;
import com.example.sarabrandserver.product.entity.*;
import com.example.sarabrandserver.product.repository.ProductRepository;
import com.example.sarabrandserver.product.response.WorkerProductResponse;
import com.example.sarabrandserver.util.DateUTC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

import static org.springframework.http.HttpStatus.*;

@Service
public class WorkerProductService {
    @Value(value = "${s3.pre-assigned.url}")
    private String PRE_ASSIGNED_URL;
    private final ProductRepository productRepository;
    private final WorkerCategoryService workerCategoryService;
    private final DateUTC dateUTC;
    private final WorkerCollectionService collectionService;

    public WorkerProductService(
            ProductRepository productRepository,
            WorkerCategoryService workerCategoryService,
            DateUTC dateUTC,
            WorkerCollectionService collectionService
    ) {
        this.productRepository = productRepository;
        this.workerCategoryService = workerCategoryService;
        this.dateUTC = dateUTC;
        this.collectionService = collectionService;
    }

    /**
     * Fetches all products with pagination in mind.
     * @param page is the UI page number
     * @param size is the max amount to be displayed on a page
     * @return List of type ProductResponse
     * */
    public List<WorkerProductResponse> fetchAll(int page, int size) {
        return this.productRepository
                .fetchAll(PageRequest.of(page, Math.max(size, 50))) //
                .stream() //
                .map(pojo -> WorkerProductResponse.builder()
                        .name(pojo.getName())
                        .desc(pojo.getDesc())
                        .price(pojo.getPrice())
                        .currency(pojo.getCurrency())
                        .sku(pojo.getSku())
                        .status(pojo.getStatus())
                        .size(pojo.getSizes())
                        .quantity(pojo.getQuantity())
                        .imageUrl(PRE_ASSIGNED_URL + pojo.getImage()) // Add pre-assigned URL S3 URL
                        .colour(pojo.getColour())
                        .build()
                ) //
                .toList();
    }

    /**
     * Method is responsible for creating and saving a Product.
     * @param file of type MultipartFile
     * @param dto of type CreateProductDTO
     * @throws CustomNotFoundException is thrown when category or collection name does not exist
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> create(CreateProductDTO dto, MultipartFile[] file) {
        var category = this.workerCategoryService.findByName(dto.getCategory().trim());
        var findProduct = this.productRepository.findByProductName(dto.getName().trim());

        ProductDetail detail;
        var date = this.dateUTC.toUTC(new Date()).isPresent() ? this.dateUTC.toUTC(new Date()).get() : new Date();

        // TODO REFACTOR
        if (findProduct.isPresent()) {
            // Build ProductDetail
            detail = productDetail(dto, file, date, date);
            findProduct.get().addDetails(detail);
            this.productRepository.save(findProduct.get());
            return new ResponseEntity<>("Added", OK);
        }

        // Build ProductDetail
        detail = productDetail(dto, file, date, null);
        // Create new Product
        var product = Product.builder()
                .name(dto.getName().trim())
                .description(dto.getDesc())
                .price(BigDecimal.valueOf(dto.getPrice()))
                .currency(dto.getCurrency()) // Default USD
                .defaultImageKey("")
                .productDetails(new HashSet<>())
                .build();
        product.addDetails(detail);
        var saved = this.productRepository.save(product);


        // Update Collection if it is not null
        if (dto.getCollection().trim().length() > 0) {
            var collection = this.collectionService.findByName(dto.getCollection().trim());
            collection.addProduct(product);
            this.collectionService.save(collection);
        }

        // Update Category of new Product
        category.addProduct(saved);
        this.workerCategoryService.save(category);

        return new ResponseEntity<>("Created", CREATED);
    }

    // Helper method to de-clutter create method
    private ProductDetail productDetail(CreateProductDTO dto, MultipartFile[] files, Date createdAt, Date modified) {
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
                .sku(UUID.randomUUID().toString())
                .isVisible(dto.getVisible())
                .createAt(createdAt)
                .modifiedAt(modified)
                .build();
        detail.setProductSize(size);
        detail.setProductInventory(inventory);
        detail.setProductColour(colour);

        // ProductImage
        for (MultipartFile file : files) {
            var image = ProductImage.builder()
                    .imageKey(UUID.randomUUID().toString())
                    .imagePath(Objects.requireNonNull(file.getOriginalFilename()).trim())
                    .productDetails(new HashSet<>())
                    .build();

            detail.setProductImage(image);
        }

        return detail;
    }

    /**
     * @param dto of type UpdateProductDTO
     * @param file of type MultipartFile
     * @throws CustomNotFoundException is thrown when Product name or sku does not exist
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> updateProduct(final UpdateProductDTO dto, final  MultipartFile file) {
        // TODO

        return new ResponseEntity<>("Updated", OK);
    }

    /**
     * Method permanently deletes all Product with param
     * @param name is the Product name
     * @return ResponseEntity of type HttpStatus (204).
     * */
    @Transactional
    public ResponseEntity<?> deleteAllProduct(final String name) {
        this.productRepository.custom_delete(name);
        return new ResponseEntity<>(NO_CONTENT);
    }

    /**
     * Method permanently deletes a Product
     * @param name is the Product name
     * @param sku is a unique String for each ProductDetail
     * @throws CustomNotFoundException is thrown when Product name or sku does not exist
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> deleteAProduct(final String name, final String sku) {
        var product = findByProductNameAndSku(name, sku);
        this.productRepository.delete(product);
        return new ResponseEntity<>("Deleted", OK);
    }

    private Product findProductByName(String name) {
        return this.productRepository.findByProductName(name)
                .orElseThrow(() -> new CustomNotFoundException(name + " does not exist"));
    }

    private Product findByProductNameAndSku(String name, String sku) {
        return this.productRepository
                .findByProductNameAndSku(name, sku)
                .orElseThrow(() -> new CustomNotFoundException("SKU does not exist"));
    }

}
