package com.sarabrandserver.product.service;

import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.service.WorkerCategoryService;
import com.sarabrandserver.collection.entity.ProductCollection;
import com.sarabrandserver.collection.service.WorkerCollectionService;
import com.sarabrandserver.data.TestingData;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.repository.PriceCurrencyRepo;
import com.sarabrandserver.product.repository.ProductRepo;
import com.sarabrandserver.util.CustomUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WorkerProductServiceTest extends AbstractUnitTest {

    @Value(value = "${aws.bucket}") private String BUCKET;
    @Value(value = "${spring.profiles.active}") private String ACTIVEPROFILE;

    private WorkerProductService workerProductService;

    @Mock private PriceCurrencyRepo priceCurrencyRepo;
    @Mock private ProductRepo productRepo;
    @Mock private WorkerProductDetailService workerProductDetailService;
    @Mock private HelperService helperService;
    @Mock private ProductSKUService productSKUService;
    @Mock private WorkerCategoryService workerCategoryService;
    @Mock private CustomUtil customUtil;
    @Mock private WorkerCollectionService collectionService;

    @BeforeEach
    void setUp() {
        this.workerProductService = new WorkerProductService(
                this.priceCurrencyRepo,
                this.productRepo,
                this.workerProductDetailService,
                this.productSKUService,
                this.workerCategoryService,
                this.collectionService,
                this.customUtil,
                this.helperService
        );
        this.workerProductService.setACTIVEPROFILE(ACTIVEPROFILE);
        this.workerProductService.setBUCKET(BUCKET);
    }

    @Test
    @DisplayName(value = "Create a new product. Product name none existing")
    void create() {
        // Given
        var sizeDtoArray = TestingData.sizeInventoryDTOArray(3);
        var files = TestingData.files(3);
        var dto = TestingData.createProductDTO(sizeDtoArray);
        var category = ProductCategory.builder().categoryName(dto.category()).build();

        // When
        when(this.customUtil.validateContainsCurrencies(dto.priceCurrency())).thenReturn(true);
        when(this.workerCategoryService.findByName(anyString())).thenReturn(category);
        when(this.productRepo.findByProductName(anyString())).thenReturn(Optional.empty());
        when(this.customUtil.toUTC(any(Date.class))).thenReturn(null);

        // Then
        this.workerProductService.create(dto, files);
        verify(this.productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName(value = "Create a new product. Exception is Product name exists")
    void createE() {
        // Given
        var sizeDtoArray = TestingData.sizeInventoryDTOArray(3);
        var files = TestingData.files(3);
        var dto = TestingData.createProductDTO(sizeDtoArray);
        var category = ProductCategory.builder().categoryName(dto.category()).build();
        var product = Product.builder().name(dto.name()).uuid("uuid").build();

        // When
        when(this.customUtil.validateContainsCurrencies(dto.priceCurrency())).thenReturn(true);
        when(this.workerCategoryService.findByName(anyString())).thenReturn(category);
        when(this.productRepo.findByProductName(anyString())).thenReturn(Optional.of(product));

        // Then
        assertThrows(DuplicateException.class, () -> this.workerProductService.create(dto, files));
    }

    @Test
    @DisplayName(value = "Update a new product. category and collection are present in the payload")
    void update() {
        // Given
        var payload = TestingData
                .updateProductDTO(
                        "",
                        "",
                        "",
                        "",
                        "collection",
                        "collectionId"
                );
        var category = ProductCategory.builder().categoryName(payload.category()).build();
        var collection = ProductCollection.builder().collection("collection").build();

        // When
        when(this.productRepo.nameNotAssociatedToUuid(anyString(), anyString())).thenReturn(0);
        when(this.workerCategoryService.findByUuid(anyString())).thenReturn(category);
        when(this.collectionService.productCollectionByUUID(anyString())).thenReturn(collection);

        // Then
        this.workerProductService.update(payload);
        verify(this.collectionService, times(1)).productCollectionByUUID(anyString());
        verify(this.productRepo, times(1))
                .update_product_where_category_and_collection_are_present(
                        anyString(),
                        anyString(),
                        anyString(),
                        any(ProductCategory.class),
                        any(ProductCollection.class)
                );
    }

    @Test
    @DisplayName(value = "Update a new product. collection and collection_id are empty")
    void updateEmpty() {
        // Given
        var payload = TestingData
                .updateProductDTO(
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                );
        var category = ProductCategory.builder().categoryName(payload.category()).build();

        // When
        when(this.productRepo.nameNotAssociatedToUuid(anyString(), anyString())).thenReturn(0);
        when(this.workerCategoryService.findByUuid(anyString())).thenReturn(category);

        // Then
        this.workerProductService.update(payload);
        verify(this.collectionService, times(0)).productCollectionByUUID(anyString());
        verify(this.productRepo, times(1))
                .update_product_where_collection_not_present(
                        anyString(),
                        anyString(),
                        anyString(),
                        any(ProductCategory.class)
                );
    }

}