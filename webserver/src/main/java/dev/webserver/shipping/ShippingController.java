package dev.webserver.shipping;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Defines the routes for countries we can ship to.
 * */
@RestController
@RequestMapping(path = "${api.endpoint.baseurl}shipping")
@RequiredArgsConstructor
class ShippingController {

    private final ShippingService service;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = "application/json")
    public List<ShippingMapper> allShipping() {
        return service.shipping();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = "application/json")
    public void create(@Valid @RequestBody ShippingDto dto) {
        service.create(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(consumes = "application/json")
    public void update(@Valid @RequestBody ShippingMapper dto) {
        service.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{shipping_id}")
    public void delete(@NotNull @PathVariable("shipping_id") Long id) {
        service.delete(id);
    }

}
