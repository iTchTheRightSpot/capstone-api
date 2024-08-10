package dev.webserver.payment;

import dev.webserver.enumeration.SarreCurrency;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

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
    @PostMapping
    public PaymentResponse raceCondition(
            @NotNull(message = "currency cannot be null")
            @NotEmpty(message = "currency cannot be empty")
            @RequestParam(name = "currency")
            final String currency,
            @NotNull(message = "country cannot be null")
            @NotEmpty(message = "country cannot be empty")
            @RequestParam(name = "country")
            final String country,
            final HttpServletRequest req
    ) {
        final var sc = SarreCurrency.valueOf(currency.toUpperCase());
        return raceConditionService.raceCondition(req, country, sc);
    }

    /**
     * Api called by Payment service to inform of a
     * complete transaction.
     * */
    @ResponseStatus(CREATED)
    @PostMapping(path = "/webhook")
    public void webhook(final HttpServletRequest req) {
        webhookService.webhook(req);
    }

}
