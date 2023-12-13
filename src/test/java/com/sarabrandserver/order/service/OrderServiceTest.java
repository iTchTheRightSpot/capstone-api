package com.sarabrandserver.order.service;

import com.sarabrandserver.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

class OrderServiceTest extends AbstractUnitTest {

    @Value(value = "/${api.endpoint.baseurl}/order")
    private String path;

    @Test
    void orderHistory() {}

}