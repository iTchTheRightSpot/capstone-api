package com.sarabrandserver.thirdparty;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CheckoutController {

    @GetMapping(path = "/index")
    String index() {
        return "index";
    }

}
