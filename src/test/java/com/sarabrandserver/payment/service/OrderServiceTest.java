package com.sarabrandserver.payment.service;

import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.payment.dto.PayloadMapper;
import com.sarabrandserver.payment.repository.OrderDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OrderServiceTest extends AbstractUnitTest {

    @Mock
    private OrderDetailRepository repository;
    @Mock
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        OrderService orderService = new OrderService(repository, s3Service);
        orderService.setBUCKET("dummy");
    }

    @Test
    void transform() {
        // given
        String str = """
                [
                  { "name": "ProductA", "key": "custom-key", "colour": "red" },
                  { "name": "ProductB", "key": "custom-key", "colour": "green" },
                  { "name": "ProductC", "key": "custom-key", "colour": "brown" }
                ]
                """;

        // when
        when(s3Service.preSignedUrl(anyString(), anyString()))
                .thenReturn("custom-key");

        // then
        PayloadMapper[] arr = OrderService
                .transform(s3Service, "dummy", str);

        assertNotNull(arr);
        verify(s3Service, times(3))
                .preSignedUrl(anyString(), anyString());

        assertEquals("ProductA", arr[0].name());
        assertEquals("custom-key", arr[0].key());
        assertEquals("red", arr[0].colour());

        assertEquals("ProductB", arr[1].name());
        assertEquals("custom-key", arr[1].key());
        assertEquals("green", arr[1].colour());

        assertEquals("ProductC", arr[2].name());
        assertEquals("custom-key", arr[2].key());
        assertEquals("brown", arr[2].colour());
    }

}