package com.sarabrandserver.order.controller;

import com.sarabrandserver.address.AddressDTO;
import com.sarabrandserver.order.dto.PaymentDTO;
import com.sarabrandserver.order.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Returns a Page
     * */
    @PreAuthorize(value = "hasRole('WORKER')")
    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Page<?> orders(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        return this.paymentService.orders(page, Math.min(size, 20));
    }

    /**
     * Api called when a client purchases an item.
     * */
    @ResponseStatus(OK)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void order(@Valid @RequestBody PaymentDTO dto) {
        this.paymentService.order(dto, dto.address());
    }

}
