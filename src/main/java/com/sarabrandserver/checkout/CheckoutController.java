package com.sarabrandserver.checkout;

import com.sarabrandserver.enumeration.SarreCurrency;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService service;

    /**
     * Route called after a user enters country in
     * checkout page.
     * */
    @GetMapping(produces = "application/json")
    Checkout checkout(
            @NotNull @RequestParam("country") String country,
            @NotNull @RequestParam("currency") String currency,
            HttpServletRequest req
    ) {
        var c = SarreCurrency.valueOf(currency.toUpperCase().trim());
        return service.checkout(req, country, c);
    }

}
