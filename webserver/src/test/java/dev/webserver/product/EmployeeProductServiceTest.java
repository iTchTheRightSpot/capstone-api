package dev.webserver.product;

import dev.webserver.AbstractUnitTest;
import dev.webserver.category.Category;
import dev.webserver.category.EmployeeCategoryService;
import dev.webserver.TestData;
import dev.webserver.exception.DuplicateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

final class EmployeeProductServiceTest extends AbstractUnitTest {

    private EmployeeProductService productService;

    @Mock private Environment environment;
    @Mock private ProductPriceCurrencyRepository currencyRepo;
    @Mock private ProductRepository productRepository;
    @Mock private EmployeeProductDetailService detailService;
    @Mock private ProductImageService productImageService;
    @Mock private ProductSkuService skuService;
    @Mock private EmployeeCategoryService categoryService;

    @BeforeEach
    void setUp() {
        productService = new EmployeeProductService(
                environment,
                currencyRepo,
                productRepository,
                detailService,
                skuService,
                categoryService,
                productImageService
        );
        super.setUpEnvironmentVariables(productService);
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
        when(categoryService.findById(anyLong())).thenReturn(category);
        when(productRepository.productByName(anyString())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(Product.builder().productId(1L).build());

        // Then
        productService.create(dto, files);
        verify(productRepository, times(1)).save(any(Product.class));
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
        when(categoryService.findById(anyLong())).thenReturn(category);
        when(productRepository.productByName(anyString())).thenReturn(Optional.of(product));

        // Then
        assertThrows(DuplicateException.class, () -> productService.create(dto, files));
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
        when(productRepository.nameNotAssociatedToUuid(anyString(), anyString())).thenReturn(0);
        when(categoryService.findById(anyLong())).thenReturn(category);

        // Then
        productService.update(payload);
        verify(productRepository, times(1))
                .updateProduct(
                        anyString(),
                        anyString(),
                        anyString(),
                        any(BigDecimal.class),
                        anyLong()
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
        when(productRepository.nameNotAssociatedToUuid(anyString(), anyString())).thenReturn(0);
        when(categoryService.findById(anyLong())).thenReturn(category);

        // Then
        productService.update(payload);
        verify(productRepository, times(1))
                .updateProduct(
                        anyString(),
                        anyString(),
                        anyString(),
                        any(BigDecimal.class),
                        anyLong()
                );
    }

}