package com.example.demo.controller;

import com.example.demo.service.CounterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CounterController {

    private final CounterService counterService;

    public CounterController(CounterService counterService) {
        this.counterService = counterService;
    }

    @GetMapping("/api/counter")
    public Map<String, String> getCounter() {
        long value = counterService.incrementAndGet();
        Map<String, String> response = new HashMap<>();
        response.put("counter", String.valueOf(value));
        return response;
    }

    @GetMapping("/")
    public ResponseEntity<Void> redirectToCounter() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/api/counter"))
                .build();
    }
}