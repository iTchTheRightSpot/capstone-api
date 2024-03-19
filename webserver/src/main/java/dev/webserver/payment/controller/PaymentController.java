package dev.webserver.payment.controller;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.payment.response.PaymentResponse;
import dev.webserver.payment.service.RaceConditionService;
import dev.webserver.payment.service.WebhookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}payment")
@RequiredArgsConstructor
public class PaymentController {

    private final RaceConditionService raceConditionService;
    private final WebhookService webhookService;

    /**
     * Called before payment page appears
     * */
    @ResponseStatus(OK)
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public PaymentResponse raceCondition(
            @NotNull @NotEmpty @RequestParam(name = "currency") String currency,
            @NotNull @NotEmpty @RequestParam(name = "country") String country,
            HttpServletRequest req
    ) {
        var sc = SarreCurrency.valueOf(currency.toUpperCase());
        return this.raceConditionService.raceCondition(req, country, sc);
    }

    /**
     * Api called by Payment service to inform of a
     * complete transaction.
     * */
    @ResponseStatus(CREATED)
    @PostMapping
    public void webhook(HttpServletRequest req) {
        this.webhookService.webhook(req);
    }

}
