package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.AbstractUnitTest;
import com.emmanuel.sarabrandserver.category.entity.ProductCategory;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.collection.entity.ProductCollection;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.product.entity.Product;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.service.HelperService;
import com.emmanuel.sarabrandserver.product.service.WorkerProductDetailService;
import com.emmanuel.sarabrandserver.product.service.WorkerProductService;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static com.emmanuel.sarabrandserver.util.TestingData.*;
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
    @Mock private WorkerProductDetailService workerProductDetailService;
    @Mock private HelperService helperService;
    @Mock private WorkerCategoryService workerCategoryService;
    @Mock private CustomUtil customUtil;
    @Mock private WorkerCollectionService collectionService;

    @BeforeEach
    void setUp() {
        this.workerProductService = new WorkerProductService(
                this.productRepository,
                this.workerCategoryService,
                this.workerProductDetailService,
                this.customUtil,
                this.collectionService,
                this.helperService
        );
        this.workerProductService.setACTIVEPROFILE(ACTIVEPROFILE);
        this.workerProductService.setBUCKET(BUCKET);
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
        this.workerProductService.update(payload);
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
        this.workerProductService.update(payload);
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