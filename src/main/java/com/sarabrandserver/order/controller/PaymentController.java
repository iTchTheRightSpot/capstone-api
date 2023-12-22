package com.sarabrandserver.order.controller;

import com.sarabrandserver.order.service.PaymentService;
import com.sarabrandserver.thirdparty.PaymentCredentialObj;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Called before payment page appears
     * */
    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public PaymentCredentialObj validate(HttpServletRequest req) {
        return this.paymentService.validate(req);
    }

    /**
     * Called via Flutterwave webhook
     * */
    @ResponseStatus(OK)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void order(HttpServletRequest req) {
        this.paymentService.order(req);
    }

}
