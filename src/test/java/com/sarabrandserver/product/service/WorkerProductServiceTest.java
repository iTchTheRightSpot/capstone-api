package com.sarabrandserver.product.service;

import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.category.entity.ProductCategory;
import com.sarabrandserver.category.service.WorkerCategoryService;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.repository.PriceCurrencyRepo;
import com.sarabrandserver.product.repository.ProductRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WorkerProductServiceTest extends AbstractUnitTest {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private WorkerProductService productService;

    @Mock private PriceCurrencyRepo currencyRepo;
    @Mock private ProductRepo productRepo;
    @Mock private WorkerProductDetailService detailService;
    @Mock private HelperService helperService;
    @Mock private ProductSKUService skuService;
    @Mock private WorkerCategoryService categoryService;

    @BeforeEach
    void setUp() {
        this.productService = new WorkerProductService(
                this.currencyRepo,
                this.productRepo,
                this.detailService,
                this.skuService,
                this.categoryService,
                this.helperService
        );
        this.productService.setBUCKET(BUCKET);
    }

    @Test
    @DisplayName(value = "Create a new product. Product name none existing")
    void create() {
        // Given
        var sizeDtoArray = TestData.sizeInventoryDTOArray(3);
        var files = TestData.files();
        var dto = TestData.createProductDTO(1, sizeDtoArray);
        var category = ProductCategory.builder()
                .categoryId(dto.categoryId())
                .name("category")
                .build();

        // When
        when(this.categoryService.findById(anyLong())).thenReturn(category);
        when(this.productRepo.findByProductName(anyString())).thenReturn(Optional.empty());

        // Then
        this.productService.create(dto, files);
        verify(this.productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName(value = "Create a new product. Exception is Product name exists")
    void createE() {
        // Given
        var sizeDtoArray = TestData.sizeInventoryDTOArray(3);
        var files = TestData.files();
        var dto = TestData.createProductDTO(1, sizeDtoArray);
        var category = ProductCategory.builder()
                .categoryId(dto.categoryId())
                .name("category")
                .build();
        var product = Product.builder().name(dto.name()).uuid("uuid").build();

        // When
        when(this.categoryService.findById(anyLong())).thenReturn(category);
        when(this.productRepo.findByProductName(anyString())).thenReturn(Optional.of(product));

        // Then
        assertThrows(DuplicateException.class, () -> this.productService.create(dto, files));
    }

    @Test
    @DisplayName(value = "Update a new product. categoryId and collection are present in the payload")
    void update() {
        // Given
        var payload = TestData
                .updateProductDTO(
                        "",
                        "",
                        "",
                        -1
                );
        var category = ProductCategory.builder().name(payload.category()).build();

        // When
        when(this.productRepo.nameNotAssociatedToUuid(anyString(), anyString())).thenReturn(0);
        when(this.categoryService.findById(anyLong())).thenReturn(category);

        // Then
        this.productService.update(payload);
        verify(this.productRepo, times(1))
                .updateProduct(
                        anyString(),
                        anyString(),
                        anyString(),
                        any(ProductCategory.class)
                );
    }

    @Test
    @DisplayName(value = "Update a new product. collection and collection_id are empty")
    void updateEmpty() {
        // Given
        var payload = TestData
                .updateProductDTO(
                        "",
                        "",
                        "", -1
                );
        var category = ProductCategory.builder().name(payload.category()).build();

        // When
        when(this.productRepo.nameNotAssociatedToUuid(anyString(), anyString())).thenReturn(0);
        when(this.categoryService.findById(anyLong())).thenReturn(category);

        // Then
        this.productService.update(payload);
        verify(this.productRepo, times(1))
                .updateProduct(
                        anyString(),
                        anyString(),
                        anyString(),
                        any(ProductCategory.class)
                );
    }

}