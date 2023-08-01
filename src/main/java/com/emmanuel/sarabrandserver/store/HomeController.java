package com.emmanuel.sarabrandserver.store;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** All routes do not require authentication */
@RestController
@RequestMapping(path = "api/v1/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    /** Returns pre-signed url which is a background video. */
    @GetMapping
    public ResponseEntity<?> fetchHomeBackground() {
        return this.homeService.fetchHomeBackground();
    }

}
