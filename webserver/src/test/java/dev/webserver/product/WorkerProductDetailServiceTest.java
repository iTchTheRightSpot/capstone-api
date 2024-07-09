package dev.webserver.product;

import com.github.javafaker.Faker;
import dev.webserver.AbstractUnitTest;
import dev.webserver.data.TestData;
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

    @Mock private ProductRepository productRepository;
    @Mock private ProductSkuService skuService;
    @Mock private ProductImageRepository imageRepo;
    @Mock private ProductDetailRepository detailRepo;
    @Mock private ProductImageService productImageService;

    @BeforeEach
    void setUp() {
        this.detailService = new WorkerProductDetailService(
                this.detailRepo,
                this.skuService,
                this.imageRepo,
                this.productRepository,
                this.productImageService
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
        when(productRepository.productByUuid(anyString())).thenReturn(Optional.of(product));
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
        when(productRepository.productByUuid(anyString())).thenReturn(Optional.of(product));
        when(detailRepo.productDetailByColour(anyString())).thenReturn(Optional.of(detail));

        // Then
        detailService.create(dto, files);
        verify(this.skuService, times(1))
                .save(any(SizeInventoryDto[].class), any(ProductDetail.class));
        verify(this.detailRepo, times(0)).save(any(ProductDetail.class));
    }

}