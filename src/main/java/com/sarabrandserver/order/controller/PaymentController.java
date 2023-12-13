package com.sarabrandserver.order.controller;

import com.sarabrandserver.order.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}payment")
@PreAuthorize(value = "hasRole('CLIENT')")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

}
