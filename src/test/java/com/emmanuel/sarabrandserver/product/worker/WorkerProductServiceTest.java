package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.aws.S3Service;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
import com.emmanuel.sarabrandserver.product.entity.Product;
import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import com.emmanuel.sarabrandserver.product.entity.ProductImage;
import com.emmanuel.sarabrandserver.product.projection.ProductPojo;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductImageRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.repository.ProductSkuRepo;
import com.emmanuel.sarabrandserver.product.util.DetailDTO;
import com.emmanuel.sarabrandserver.product.util.ProductDTO;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class WorkerProductServiceTest {
    private WorkerProductService productService;

    @Mock private ProductRepository productRepository;
    @Mock private ProductSkuRepo productSkuRepo;
    @Mock private ProductImageRepo productImageRepo;
    @Mock private WorkerCategoryService workerCategoryService;
    @Mock private CustomUtil customUtil;
    @Mock private WorkerCollectionService collectionService;
    @Mock private ProductDetailRepo detailRepo;
    @Mock private S3Service s3Service;
    @Mock private Environment environment;

    @BeforeEach
    void setUp() {
        this.productService = new WorkerProductService(
                this.productRepository,
                this.detailRepo,
                this.productImageRepo,
                this.productSkuRepo,
                this.workerCategoryService,
                this.customUtil,
                this.collectionService,
                this.s3Service,
                this.environment
        );
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
        when(this.environment.getProperty(anyString(), anyString())).thenReturn("test");

        // Then
        assertEquals(30, this.productService.fetchAll(0, 40).getSize());
    }

    @Test
    @DisplayName(value = "update all product name only")
    void updateProductName() {
        // Given
        var product = products().get(0);
        var dto = ProductDTO.builder()
                .uuid(product.getUuid())
                .name(new Faker().commerce().productName())
                .desc(product.getDescription())
                .price(product.getPrice())
                .build();

        // Then
        assertEquals(HttpStatus.OK, this.productService.updateProduct(dto).getStatusCode());
        verify(this.productRepository, times(1))
                .updateProduct(any(String.class), any(String.class), any(String.class), any(BigDecimal.class));
    }

    @Test
    @DisplayName(value = "update all updatable variable")
    void updateAll() {
        // Given
        var product = products().get(1);
        var dto = ProductDTO.builder()
                .uuid(product.getUuid())
                .name(new Faker().commerce().productName())
                .desc(new Faker().lorem().characters(5, 200))
                .price(new BigDecimal(new Faker().commerce().price()))
                .build();

        // Then
        assertEquals(HttpStatus.OK, this.productService.updateProduct(dto).getStatusCode());
        verify(this.productRepository, times(1))
                .updateProduct(any(String.class), any(String.class), any(String.class), any(BigDecimal.class));
    }

    @Test
    void updateProductDetail() {
        // Given
        var dto = DetailDTO.builder()
                .sku(UUID.randomUUID().toString())
                .isVisible(true)
                .qty(new Faker().number().numberBetween(1, 29))
                .size(new Faker().commerce().material())
                .build();

        // Then
        assertEquals(HttpStatus.OK, this.productService.updateProductDetail(dto).getStatusCode());
        verify(this.detailRepo, times(1))
                .updateProductDetail(anyString(), anyBoolean(), anyInt(), anyString());
    }

    @Test
    void deleteProduct() {
        // Given
        var product = products().get(0);

        // When
        doReturn(Optional.of(product)).when(this.productRepository).findByProductUuid(anyString());
        when(this.environment.getProperty(anyString(), anyString())).thenReturn("test");

        // Then
        assertEquals(HttpStatus.NO_CONTENT, this.productService.deleteProduct(product.getName()).getStatusCode());
        verify(this.productRepository, times(1)).delete(any(Product.class));
    }

    @Test
    void deleteProductDetail() {
        // Given
        String sku = UUID.randomUUID().toString();
        var detail = ProductDetail.builder()
                .productDetailId(1L)
                .isVisible(true)
                .createAt(new Date())
                .product(products().get(0))
                .skus(new HashSet<>())
                .productImages(new HashSet<>())
                .build();

        // When
        doReturn(Optional.of(detail)).when(productRepository).findDetailBySku(anyString());
        when(this.environment.getProperty(anyString(), anyString())).thenReturn("test");

        // Then
        assertEquals(HttpStatus.NO_CONTENT, this.productService.deleteProductDetail(sku).getStatusCode());
        verify(this.detailRepo, times(1)).delete(any(ProductDetail.class));
    }

    private List<Product> products() {
        List<Product> list = new ArrayList<>();
        Set<String> set = new HashSet<>();

        for (int i = 0; i < 10; i++) {
            set.add(new Faker().commerce().productName());
        }

        for (String str : set) {
            var product = Product.builder()
                    .name(str)
                    .uuid(UUID.randomUUID().toString())
                    .description(new Faker().lorem().characters(50))
                    .price(new BigDecimal(new Faker().commerce().price()))
                    .currency(new Faker().currency().name())
                    .productDetails(new HashSet<>())
                    .build();

            for (int j = 0; j < 3; j++) {
                // ProductImage
                var image = ProductImage.builder()
                        .imageKey(UUID.randomUUID().toString())
                        .imagePath(new Faker().file().fileName())
                        .build();
                // ProductDetail
                var detail = ProductDetail.builder()
                        .isVisible(false)
                        .createAt(new Date())
                        .productImages(new HashSet<>())
                        .skus(new HashSet<>())
                        .build();
                detail.addImages(image);
            }
            list.add(product);
        }

        return list;
    }

}