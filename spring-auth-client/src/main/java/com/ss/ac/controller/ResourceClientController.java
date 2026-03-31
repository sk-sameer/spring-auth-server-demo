package com.ss.ac.controller;

import com.ss.ac.service.WebClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides an endpoint to access a protected resource from another service.
 * It uses the WebClientService to make an HTTP request to the resource server and returns the response.
 */
@RestController
@RequiredArgsConstructor
public class ResourceClientController {

    private final WebClientService webClientService;

    @GetMapping("/resource")
    public String resource() {
        return webClientService.resource();
    }

}
