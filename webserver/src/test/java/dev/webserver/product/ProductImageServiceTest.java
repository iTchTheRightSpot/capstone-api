package dev.webserver.product;

import dev.webserver.AbstractUnitTest;
import dev.webserver.external.aws.S3Service;
import dev.webserver.exception.CustomServerError;
import dev.webserver.product.response.CustomMultiPart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductImageServiceTest extends AbstractUnitTest {

    private ProductImageService service;

    @Mock
    private ProductImageRepository repository;
    @Mock
    private S3Service s3Service;

    @BeforeEach
    void createInstance() {
        service = new ProductImageService(repository, s3Service);
    }

    private static final CustomMultiPart[] files = {
            new CustomMultiPart(new File("file1.txt"), createMetadata(), "key1"),
            new CustomMultiPart(new File("file2.txt"), createMetadata(), "key2"),
            new CustomMultiPart(new File("file3.txt"), createMetadata(), "key3")
    };

    private static Map<String, String> createMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", "value2");
        return metadata;
    }

    private static final ProductDetail detail = ProductDetail.builder()
            .productDetailId(1L)
            .colour("red")
            .build();

    @Test
    void shouldSuccessfullySaveProductImages() {
        // when
        service.saveProductImages(detail, files, "bucket");

        // then
        verify(s3Service, times(3))
                .uploadToS3(any(File.class), anyMap(), anyString(), anyString());
        verify(repository, times(3))
                .save(any(ProductImage.class));
    }

    @Test
    void shouldThrowErrorWhenExceptionOccursDuringMultiThreadedS3Upload() {
        // when
        for (int i = 0; i < files.length; i++) {
            if (i == 1)
                doThrow(new CustomServerError("simulate exception"))
                        .when(s3Service)
                        .uploadToS3(any(File.class), anyMap(), anyString(), anyString());
        }

        // then
        assertThrows(CustomServerError.class,
                () -> service.saveProductImages(detail, files, "bucket"));
    }

}