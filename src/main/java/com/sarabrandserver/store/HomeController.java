package com.sarabrandserver.store;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** All routes do not require authentication */
@RestController
@RequestMapping(path = "api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /** Returns pre-signed url which is a background video. */
    @GetMapping
    public ResponseEntity<?> fetchHomeBackground() {
        return this.homeService.fetchHomeBackground();
    }

}
