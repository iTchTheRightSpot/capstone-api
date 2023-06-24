package com.emmanuel.sarabrandserver.product.service;

import com.emmanuel.sarabrandserver.category.entity.ProductCategory;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.collection.entity.ProductCollection;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
import com.emmanuel.sarabrandserver.product.dto.CreateProductDTO;
import com.emmanuel.sarabrandserver.product.entity.*;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.response.WorkerProductResponse;
import com.emmanuel.sarabrandserver.util.DateUTC;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.CREATED;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("dev")
@TestPropertySource(locations = "classpath:application-dev.properties")
class WorkerProductServiceTest {
    private WorkerProductService productService;

    @Mock private ProductRepository productRepository;
    @Mock private WorkerCategoryService workerCategoryService;
    @Mock private DateUTC dateUTC;
    @Mock private WorkerCollectionService collectionService;

    @BeforeEach
    void setUp() {
        this.productService = new WorkerProductService(
                this.productRepository,
                this.workerCategoryService,
                this.dateUTC,
                this.collectionService
        );
    }

    @Test
    void fetchAll() {
        // Given
        int page = 0, size = 50;
        var given = pageRequest();

        // When
        doReturn(given).when(this.productRepository).fetchAll(PageRequest.of(page, size));

        // Then
        var fetch = this.productService.fetchAll(page, size);
        assertEquals(given.size(), fetch.size());
        Assertions.assertEquals(given.get(0).getClass(), fetch.get(0).getClass());
        Assertions.assertEquals(given.get(0).getPrice(), fetch.get(0).getPrice());
        Assertions.assertEquals(given.get(0).getName(), fetch.get(0).getName());
    }

    @Test
    void create() {
        // Given
        MockMultipartFile[] arr = {
                new MockMultipartFile(
                        "file",
                        "uploads/image1.jpeg",
                        "image/jpeg",
                        "Test image content".getBytes()
                )
        };
        var pojo = pageRequest().get(0);
        var product = products().get(0);
        var dto = CreateProductDTO.builder()
                .category("Example Category")
                .collection("Example Collection")
                .name(product.getName())
                .desc(product.getDescription())
                .price(product.getPrice().doubleValue())
                .currency(product.getCurrency())
                .visible(true)
                .qty(pojo.getQuantity())
                .size(pojo.getSize())
                .colour(pojo.getColour())
                .build();
        var category = ProductCategory.builder()
                .categoryName("Example Category")
                .createAt(new Date())
                .modifiedAt(null)
                .isVisible(true)
                .productCategories(new HashSet<>())
                .product(new HashSet<>())
                .build();
        category.addProduct(product);
        var collection = ProductCollection.builder()
                .collection("Example Collection")
                .products(new HashSet<>())
                .createAt(new Date())
                .modifiedAt(null)
                .isVisible(true)
                .build();
        collection.addProduct(product);

        // When
        doReturn(category).when(this.workerCategoryService).findByName(anyString());
        doReturn(Optional.empty()).when(this.productRepository).findByProductName(anyString());
        doReturn(Optional.of(new Date())).when(this.dateUTC).toUTC(any(Date.class));
        Mockito.doReturn(product).when(this.productRepository).save(any(Product.class));
        doReturn(collection).when(this.collectionService).findByName(dto.getCollection());

        // Then
        var res = this.productService.create(dto, arr);
        assertEquals(CREATED, res.getStatusCode());
        verify(this.workerCategoryService, times(1)).save(any(ProductCategory.class));
        verify(this.collectionService, times(1)).save(any(ProductCollection.class));
    }

    @Test
    void updateProduct() {}

    @Test
    void deleteProduct() {}

    @Test
    void deleteProductDetail() {}

    private List<WorkerProductResponse> pageRequest() {
        List<WorkerProductResponse> list = new ArrayList<>();

        for (Product pojo : products()) {
            var res = WorkerProductResponse.builder()
                    .name(pojo.getName())
                    .desc(pojo.getDescription())
                    .price(pojo.getPrice())
                    .currency(pojo.getCurrency())
                    .sku(pojo.getProductDetails().stream().map(ProductDetail::getSku).findAny().get())
                    .status(pojo.getProductDetails().stream().map(ProductDetail::isVisible).findAny().get())
                    .size(pojo.getProductDetails()
                            .stream()
                            .map(ProductDetail::getProductSize)
                            .map(ProductSize::getSize)
                            .findAny()
                            .get()
                    )
                    .quantity(pojo.getProductDetails()
                            .stream()
                            .map(ProductDetail::getProductInventory)
                            .map(ProductInventory::getQuantity)
                            .findAny()
                            .get()
                    )
                    .imageUrl(pojo.getProductDetails()
                            .stream()
                            .map(ProductDetail::getProductImage)
                            .map(ProductImage::getImageKey)
                            .findAny()
                            .get()
                    ) // Add S3 URL
                    .colour(pojo.getProductDetails()
                            .stream()
                            .map(ProductDetail::getProductColour)
                            .map(ProductColour::getColour)
                            .findAny()
                            .get())
                    .build();
            list.add(res);
        }

        return list;
    }

    private List<Product> products() {
        List<Product> list = new ArrayList<>();
        Set<String> set = new HashSet<>();

        for (int i = 0; i < 50; i++) {
            set.add(new Faker().commerce().productName());
        }

        for (String str : set) {
            var product = Product.builder()
                    .name(str)
                    .description(new Faker().lorem().characters(50))
                    .price(new BigDecimal(new Faker().commerce().price()))
                    .currency(new Faker().currency().name())
                    .defaultImageKey(new Faker().file().fileName())
                    .productDetails(new HashSet<>())
                    .build();

            for (int j = 0; j < 20; j++) {
                // ProductSize
                var size = ProductSize.builder()
                        .size(new Faker().numerify(String.valueOf(j)))
                        .productDetails(new HashSet<>())
                        .build();
                // ProductInventory
                var inventory = ProductInventory.builder()
                        .quantity(new Faker().number().numberBetween(10, 40))
                        .productDetails(new HashSet<>())
                        .build();
                // ProductImage
                var image = ProductImage.builder()
                        .imageKey(UUID.randomUUID().toString())
                        .imagePath(new Faker().file().fileName())
                        .productDetails(new HashSet<>())
                        .build();
                // ProductColour
                var colour = ProductColour.builder()
                        .colour(new Faker().color().name())
                        .productDetails(new HashSet<>())
                        .build();
                // ProductDetail
                var detail = ProductDetail.builder()
                        .sku(UUID.randomUUID().toString())
                        .isVisible(false)
                        .createAt(new Date())
                        .modifiedAt(null)
                        .build();
                detail.setProductSize(size);
                detail.setProductInventory(inventory);
                detail.setProductImage(image);
                detail.setProductColour(colour);
                // Add detail to product
                product.addDetails(detail);
            }
            list.add(product);
        }

        return list;
    }

}