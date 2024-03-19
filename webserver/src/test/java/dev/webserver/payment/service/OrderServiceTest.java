package dev.webserver.payment.service;

import dev.webserver.AbstractUnitTest;
import dev.webserver.aws.S3Service;
import dev.webserver.payment.repository.OrderDetailRepository;
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