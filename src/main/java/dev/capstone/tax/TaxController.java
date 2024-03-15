package dev.capstone.tax;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}tax")
@RequiredArgsConstructor
public class TaxController {

    private final TaxService service;

    @GetMapping(produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    List<TaxDto> taxes () {
        return service.taxes();
    }

    @PutMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void update (@Valid @RequestBody TaxDto dto) {
        service.update(dto);
    }

}