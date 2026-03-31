package com.ss.ac.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * It returns a simple welcome message when the root URL ("/") is accessed.
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to the Home Page!";
    }

}
