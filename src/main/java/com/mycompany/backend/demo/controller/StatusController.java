package com.mycompany.backend.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


// check status，就这一个controller，不需要Repo Model  Service layer
@RestController
public class StatusController {

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("message", "AWS Demo server is up and running!");
    }
}
