package dev.webserver;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}")
class TestController {

    @GetMapping(path = "order/test")
    public String client() {
        return "client";
    }

    @GetMapping(path = "employee/test")
    public String worker() {
        return "worker";
    }

    @GetMapping(path = "employee/test/with-param")
    public void workerparam(
            @NotEmpty(message = "parameter cannot be empty")
            @RequestParam(value = "parameter") final String parameter
    ) {
        System.out.println("test get request parameter " + parameter);
    }

    record MockBody(
            @NotEmpty(message = "parameter cannot be empty")
            @Size.List({@Size(max = 9, message = "parameter max length of 9")})
            String parameter,
            @NotEmpty(message = "numbers cannot be empty")
            Long[] numbers,
            @NotNull(message = "number is required")
            Long number
    ) {
    }

    @PostMapping(path = "employee/test/with-param", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void workerpostparam(@Valid @RequestBody final MockBody body) {
        System.out.println("test get request parameter " + body);
    }

    @PostMapping(path = "employee/test")
    public void workerpost() {
        System.out.println("POST REQUEST");
    }

}