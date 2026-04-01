package com.example.demo.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CounterService {

    private final StringRedisTemplate redisTemplate;
    private static final String COUNTER_KEY = "request_counter";

    public CounterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public long incrementAndGet() {
        return redisTemplate.opsForValue().increment(COUNTER_KEY);
    }

    public long getCurrentValue() {
        String val = redisTemplate.opsForValue().get(COUNTER_KEY);
        return val == null ? 0 : Long.parseLong(val);
    }
}


