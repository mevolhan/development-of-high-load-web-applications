package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CounterController {

    private final AtomicLong counter = new AtomicLong(0);

    @GetMapping("/api/counter") // Полный путь
    public Map<String, String> getCounter() {
        long value = counter.incrementAndGet();
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