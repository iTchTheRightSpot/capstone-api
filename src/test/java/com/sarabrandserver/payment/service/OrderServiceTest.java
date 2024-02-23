package com.sarabrandserver.payment.service;

import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.payment.repository.OrderDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

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
    void shouldSuccessfullyRetrieveAUsersOrderHistory() {}

}