package dev.webserver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}")
class TestController {

    @GetMapping(path = "/product/test")
    public String client() {
        return "client";
    }

    @GetMapping(path = "employee/test")
    public String worker() {
        return "worker";
    }

}