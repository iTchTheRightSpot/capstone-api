package com.sarabrandserver.product.service;

import com.github.javafaker.Faker;
import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.data.TestData;
import com.sarabrandserver.product.dto.SizeInventoryDTO;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.entity.ProductDetail;
import com.sarabrandserver.product.repository.ProductDetailRepo;
import com.sarabrandserver.product.repository.ProductImageRepo;
import com.sarabrandserver.product.repository.ProductRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WorkerProductDetailServiceTest extends AbstractUnitTest {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private WorkerProductDetailService detailService;

    @Mock private ProductRepo productRepo;
    @Mock private ProductSKUService skuService;
    @Mock private ProductImageRepo imageRepo;
    @Mock private ProductDetailRepo detailRepo;
    @Mock private HelperService helperService;

    @BeforeEach
    void setUp() {
        this.detailService = new WorkerProductDetailService(
                this.detailRepo,
                this.skuService,
                this.imageRepo,
                this.productRepo,
                this.helperService
        );
        this.detailService.setBUCKET(BUCKET);
    }

    @Test
    @DisplayName(value = "Create a new ProductDetail.")
    void create() {
        // Given
        var dtos = TestData.sizeInventoryDTOArray(4);
        var files = TestData.files();
        var product = Product.builder().uuid("product uuid").build();
        var dto = TestData.productDetailDTO(product.getUuid(), "mat-black", dtos);

        // When
        when(productRepo.findByProductUuid(anyString())).thenReturn(Optional.of(product));
        when(detailRepo.productDetailByColour(anyString())).thenReturn(Optional.empty());

        // Then
        detailService.create(dto, files);
        verify(this.detailRepo, times(1)).save(any(ProductDetail.class));
    }

    @Test
    @DisplayName(value = "Create a new ProductDetail. Colour exists")
    void createE() {
        // Given
        var dtos = TestData.sizeInventoryDTOArray(4);
        var files = TestData.files();
        var product = Product.builder().uuid("product uuid").build();
        var detail = ProductDetail.builder().colour(new Faker().commerce().color()).build();
        var dto = TestData.productDetailDTO(product.getUuid(), detail.getColour(), dtos);

        // When
        when(productRepo.findByProductUuid(anyString())).thenReturn(Optional.of(product));
        when(detailRepo.productDetailByColour(anyString())).thenReturn(Optional.of(detail));

        // Then
        detailService.create(dto, files);
        verify(this.skuService, times(1))
                .save(any(SizeInventoryDTO[].class), any(ProductDetail.class));
        verify(this.detailRepo, times(0)).save(any(ProductDetail.class));
    }

}