package dev.webserver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping(path = "employee/test")
    public void workerpost() {
        System.out.println("POST REQUEST");
    }

}