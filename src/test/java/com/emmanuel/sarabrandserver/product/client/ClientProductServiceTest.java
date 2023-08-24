package com.emmanuel.sarabrandserver.product.client;

import com.emmanuel.sarabrandserver.aws.S3Service;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class ClientProductServiceTest {
    private ClientProductService clientService;

    @Mock private ProductRepository productRepository;
    @Mock private ProductDetailRepo productDetailRepo;
    @Mock private S3Service s3Service;
    @Mock private Environment environment;

    @BeforeEach
    void setUp() {
        this.clientService = new ClientProductService(this.productRepository, productDetailRepo, s3Service, environment);
    }

    @Test
    void fetchAll() {
        // Given
        // When
        // Then
    }

}