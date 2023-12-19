package com.sarabrandserver.order.controller;

import com.sarabrandserver.order.dto.PaymentDTO;
import com.sarabrandserver.order.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    public void validate(HttpServletRequest req) {
        this.paymentService.validate(req);
    }

    /**
     * Api called when a client purchases an item test
     * */
    @ResponseStatus(OK)
    @PostMapping(path = "/test", consumes = APPLICATION_JSON_VALUE)
    public void test(@Valid @RequestBody PaymentDTO dto) {
        this.paymentService.test(dto, dto.address());
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
