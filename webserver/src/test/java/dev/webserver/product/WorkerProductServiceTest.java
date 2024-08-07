package dev.webserver.product;

import dev.webserver.AbstractUnitTest;
import dev.webserver.category.Category;
import dev.webserver.category.WorkerCategoryService;
import dev.webserver.data.TestData;
import dev.webserver.exception.DuplicateException;
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

    @Mock private PriceCurrencyRepository currencyRepo;
    @Mock private ProductRepository productRepository;
    @Mock private WorkerProductDetailService detailService;
    @Mock private ProductImageService productImageService;
    @Mock private ProductSkuService skuService;
    @Mock private WorkerCategoryService categoryService;

    @BeforeEach
    void setUp() {
        this.productService = new WorkerProductService(
                this.currencyRepo,
                this.productRepository,
                this.detailService,
                this.skuService,
                this.categoryService,
                this.productImageService
        );
        this.productService.setBUCKET(BUCKET);
    }

    @Test
    void shouldSuccessfullyCreateAProduct() {
        // Given
        var sizeDtoArray = TestData.sizeInventoryDTOArray(3);
        var files = TestData.files();
        var dto = TestData.createProductDTO(1, sizeDtoArray);
        var category = Category.builder()
                .categoryId(dto.categoryId())
                .name("category")
                .build();

        // When
        when(this.categoryService.findById(anyLong())).thenReturn(category);
        when(this.productRepository.productByName(anyString())).thenReturn(Optional.empty());

        // Then
        this.productService.create(dto, files);
        verify(this.productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingANewProductDueToDuplicateName() {
        // Given
        var sizeDtoArray = TestData.sizeInventoryDTOArray(3);
        var files = TestData.files();
        var dto = TestData.createProductDTO(1, sizeDtoArray);
        var category = Category.builder()
                .categoryId(dto.categoryId())
                .name("category")
                .build();
        var product = Product.builder().name(dto.name()).uuid("uuid").build();

        // When
        when(this.categoryService.findById(anyLong())).thenReturn(category);
        when(this.productRepository.productByName(anyString())).thenReturn(Optional.of(product));

        // Then
        assertThrows(DuplicateException.class, () -> this.productService.create(dto, files));
    }

    @Test
    void shouldSuccessfullyUpdateAProduct() {
        // Given
        var payload = TestData
                .updateProductDTO(
                        "",
                        "",
                        1
                );
        var category = Category.builder().categoryId(payload.categoryId()).build();

        // When
        when(this.productRepository.nameNotAssociatedToUuid(anyString(), anyString())).thenReturn(0);
        when(this.categoryService.findById(anyLong())).thenReturn(category);

        // Then
        this.productService.update(payload);
        verify(this.productRepository, times(1))
                .updateProduct(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyDouble(),
                        any(Category.class)
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
                        1
                );
        var category = Category.builder().categoryId(payload.categoryId()).build();

        // When
        when(this.productRepository.nameNotAssociatedToUuid(anyString(), anyString())).thenReturn(0);
        when(this.categoryService.findById(anyLong())).thenReturn(category);

        // Then
        this.productService.update(payload);
        verify(this.productRepository, times(1))
                .updateProduct(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyDouble(),
                        any(Category.class)
                );
    }

}