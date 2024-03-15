package dev.capstone.shipping.controller;

import dev.capstone.shipping.ShippingDto;
import dev.capstone.shipping.ShippingMapper;
import dev.capstone.shipping.service.ShippingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Defines the routes for countries we can ship to.
 * */
@RestController
@RequestMapping(path = "${api.endpoint.baseurl}shipping")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
public class ShippingController {

    private final ShippingService service;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = "application/json")
    List<ShippingMapper> allShipping() {
        return service.shipping();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = "application/json")
    void create(@Valid @RequestBody ShippingDto dto) {
        service.create(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(consumes = "application/json")
    void update(@Valid @RequestBody ShippingMapper dto) {
        service.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{shipping_id}")
    void delete(@NotNull @PathVariable("shipping_id") Long id) {
        service.delete(id);
    }

}
