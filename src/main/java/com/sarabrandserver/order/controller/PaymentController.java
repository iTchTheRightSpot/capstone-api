package com.sarabrandserver.order.controller;

import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.order.response.PaymentResponse;
import com.sarabrandserver.order.service.PaymentService;
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
    public PaymentResponse raceCondition(
            @RequestParam(name = "currency") String currency,
            HttpServletRequest req
    ) {
        var sc = SarreCurrency.valueOf(currency.toUpperCase());
        return this.paymentService.raceCondition(req, sc);
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
