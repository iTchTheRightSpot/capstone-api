package dev.webserver.product;

import dev.webserver.AbstractUnitTest;
import dev.webserver.exception.CustomServerError;
import dev.webserver.external.aws.IS3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

final class ProductImageServiceTest extends AbstractUnitTest {

    private ProductImageService service;

    @Mock
    private ProductImageRepository repository;
    @Mock
    private IS3Service s3Service;

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
            .detailId(1L)
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
            if (i == (files.length - 1))
                doThrow(new CustomServerError("simulate exception"))
                        .when(s3Service)
                        .uploadToS3(any(File.class), anyMap(), anyString(), anyString());
        }

        // method to test and assert
        assertThrows(CustomServerError.class,
                () -> {
                    try {
                        service.saveProductImages(detail, files, "bucket");
                    } catch (CompletionException e) {
                        // Unwrap the exception and rethrow the cause if it's a CustomServerError
                        if (e.getCause() instanceof CustomServerError) {
                            throw e.getCause();
                        }
                        throw e;
                    }
                });
    }

}