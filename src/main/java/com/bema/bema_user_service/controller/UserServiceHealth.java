package com.bema.bema_user_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class UserServiceHealth {
    
    @GetMapping
    public String helath() {
        String appHealth = "The application is running normally";
        return appHealth;
    }
}
