<<<<<<<< HEAD:webserver/src/test/java/dev/webserver/product/service/WorkerProductDetailServiceTest.java
package dev.webserver.product.service;

import com.github.javafaker.Faker;
import dev.webserver.AbstractUnitTest;
import dev.webserver.data.TestData;
import dev.webserver.product.dto.SizeInventoryDTO;
import dev.webserver.product.entity.Product;
import dev.webserver.product.entity.ProductDetail;
import dev.webserver.product.repository.ProductDetailRepo;
import dev.webserver.product.repository.ProductImageRepo;
import dev.webserver.product.repository.ProductRepo;
========
package dev.capstone.product.service;

import com.github.javafaker.Faker;
import dev.capstone.AbstractUnitTest;
import dev.capstone.data.TestData;
import dev.capstone.product.dto.SizeInventoryDTO;
import dev.capstone.product.entity.Product;
import dev.capstone.product.entity.ProductDetail;
import dev.capstone.product.repository.ProductDetailRepo;
import dev.capstone.product.repository.ProductImageRepo;
import dev.capstone.product.repository.ProductRepo;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/test/java/dev/capstone/product/service/WorkerProductDetailServiceTest.java
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
    @Mock private ProductSkuService skuService;
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
        when(productRepo.productByUuid(anyString())).thenReturn(Optional.of(product));
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
        when(productRepo.productByUuid(anyString())).thenReturn(Optional.of(product));
        when(detailRepo.productDetailByColour(anyString())).thenReturn(Optional.of(detail));

        // Then
        detailService.create(dto, files);
        verify(this.skuService, times(1))
                .save(any(SizeInventoryDTO[].class), any(ProductDetail.class));
        verify(this.detailRepo, times(0)).save(any(ProductDetail.class));
    }

}