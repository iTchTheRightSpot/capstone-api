package com.emmanuel.sarabrandserver.unit;

import com.emmanuel.sarabrandserver.AbstractUnitTest;
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
import com.emmanuel.sarabrandserver.product.worker.WorkerProductService;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class WorkerProductServiceTest extends AbstractUnitTest {
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

}