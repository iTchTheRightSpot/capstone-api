package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.AbstractUnitTest;
import com.emmanuel.sarabrandserver.aws.S3Service;
import com.emmanuel.sarabrandserver.category.entity.ProductCategory;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.collection.entity.ProductCollection;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.product.entity.Product;
import com.emmanuel.sarabrandserver.product.projection.ProductPojo;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductImageRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.repository.ProductSkuRepo;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.*;

import static com.emmanuel.sarabrandserver.util.TestingData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WorkerProductServiceTest extends AbstractUnitTest {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    @Value(value = "${spring.profiles.active}")
    private String ACTIVEPROFILE;

    private WorkerProductService workerProductService;

    @Mock private ProductRepository productRepository;
    @Mock private ProductSkuRepo productSkuRepo;
    @Mock private ProductImageRepo productImageRepo;
    @Mock private WorkerCategoryService workerCategoryService;
    @Mock private CustomUtil customUtil;
    @Mock private WorkerCollectionService collectionService;
    @Mock private ProductDetailRepo detailRepo;
    @Mock private S3Service s3Service;

    @BeforeEach
    void setUp() {
        this.workerProductService = new WorkerProductService(
                this.productRepository,
                this.detailRepo,
                this.productImageRepo,
                this.productSkuRepo,
                this.workerCategoryService,
                this.customUtil,
                this.collectionService,
                this.s3Service
        );
        this.workerProductService.setACTIVEPROFILE(ACTIVEPROFILE);
        this.workerProductService.setBUCKET(BUCKET);
    }

    /** Testing fetchAll method that returns a ProductResponse. */
    @Test
    void fetch() {
        // Given
        List<ProductPojo> productList = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            var pojo = mock(ProductPojo.class);
            when(pojo.getUuid()).thenReturn("custom uuid");
            when(pojo.getName()).thenReturn(new Faker().commerce().productName());
            when(pojo.getDesc()).thenReturn(new Faker().lorem().characters(0, 400));
            when(pojo.getPrice()).thenReturn(BigDecimal.valueOf(Double.parseDouble(new Faker().commerce().price(5, 300))));
            when(pojo.getCurrency()).thenReturn("USD");
            when(pojo.getKey()).thenReturn(UUID.randomUUID().toString());
            productList.add(pojo);
        }

        Page<ProductPojo> list = new PageImpl<>(productList);

        // When
        when(this.productRepository.fetchAllProductsWorker(any(PageRequest.class))).thenReturn(list);

        // Then
        assertEquals(30, this.workerProductService.fetchAll(0, 40).getSize());
    }

    @Test
    @DisplayName(value = "Create a new product. Product name none existing")
    void create() {
        // Given
        var sizeDtoArray = sizeInventoryDTOArray(3);
        var files = files(3);
        var dto = createProductDTO(sizeDtoArray, files);
        var category = ProductCategory.builder().categoryName(dto.getCategory()).build();

        // When
        when(this.workerCategoryService.findByName(anyString())).thenReturn(category);
        when(this.productRepository.findByProductName(anyString())).thenReturn(Optional.empty());
        when(this.customUtil.toUTC(any(Date.class))).thenReturn(Optional.empty());

        // Then
        this.workerProductService.create(dto, files);
        verify(this.productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName(value = "Create a new product. Exception is Product name exists")
    void createE() {
        // Given
        var sizeDtoArray = sizeInventoryDTOArray(3);
        var files = files(3);
        var dto = createProductDTO(sizeDtoArray, files);
        var category = ProductCategory.builder().categoryName(dto.getCategory()).build();
        var product = Product.builder().name(dto.getName()).uuid("uuid").build();

        // When
        when(this.workerCategoryService.findByName(anyString())).thenReturn(category);
        when(this.productRepository.findByProductName(anyString())).thenReturn(Optional.of(product));
        when(this.customUtil.toUTC(any(Date.class))).thenReturn(Optional.empty());

        // Then
        assertThrows(DuplicateException.class, () -> this.workerProductService.create(dto, files));
    }

    @Test
    @DisplayName(value = "Update a new product. category and collection are present in the payload")
    void update() {
        // Given
        var payload = updateProductDTO("collection", "collectionId");
        var category = ProductCategory.builder().categoryName(payload.getCategory()).build();
        var collection = ProductCollection.builder().collection("collection").build();

        // When
        when(this.productRepository.nameNotAssociatedToUuid(anyString(), anyString())).thenReturn(0);
        when(this.workerCategoryService.findByUuid(anyString())).thenReturn(category);
        when(this.collectionService.findByUuid(anyString())).thenReturn(collection);

        // Then
        this.workerProductService.updateProduct(payload);
        verify(this.collectionService, times(1)).findByUuid(anyString());
        verify(this.productRepository, times(1))
                .updateProductCategoryCollectionPresent(
                        anyString(),
                        anyString(),
                        anyString(),
                        any(BigDecimal.class),
                        any(ProductCategory.class),
                        any(ProductCollection.class)
                );
    }

    @Test
    @DisplayName(value = "Update a new product. collection and collection_id are empty")
    void updateEmpty() {
        // Given
        var payload = updateProductDTO("", "");
        var category = ProductCategory.builder().categoryName(payload.getCategory()).build();

        // When
        when(this.productRepository.nameNotAssociatedToUuid(anyString(), anyString())).thenReturn(0);
        when(this.workerCategoryService.findByUuid(anyString())).thenReturn(category);

        // Then
        this.workerProductService.updateProduct(payload);
        verify(this.collectionService, times(0)).findByUuid(anyString());
        verify(this.productRepository, times(1))
                .updateProductCollectionNotPresent(
                        anyString(),
                        anyString(),
                        anyString(),
                        any(BigDecimal.class),
                        any(ProductCategory.class)
                );
    }

}