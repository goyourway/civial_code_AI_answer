package com.jiing.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestDemoController {

    @GetMapping("/demo")
    public String demo() {
        return "Hello from Spring Boot Demo Controller!";
    }
}