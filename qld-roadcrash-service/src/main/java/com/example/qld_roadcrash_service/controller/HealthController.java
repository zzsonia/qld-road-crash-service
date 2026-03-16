package com.example.qld_roadcrash_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/status")
    public String healthCheck(){
        return "QLD Road Crash Service is up and running!";
    }
}
