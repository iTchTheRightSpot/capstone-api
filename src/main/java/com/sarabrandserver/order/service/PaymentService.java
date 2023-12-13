package com.sarabrandserver.order.service;

import com.sarabrandserver.order.dto.PaymentDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public Page<?> orders(int page, int min) {
        return null;
    }

    /**
     * Test implementation for purchasing products
     * */
    public void order(final PaymentDTO[] dto) {

    }

}
