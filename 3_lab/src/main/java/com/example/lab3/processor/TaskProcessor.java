package com.example.lab3.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;

@Component
public class TaskProcessor {
    private static final String IN_QUEUE = "task:in";
    private static final String REPLY_CHANNEL = "task:reply";
    private static final Logger logger = LoggerFactory.getLogger(TaskProcessor.class);

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void startProcessing() {
        logger.info("TaskProcessor started, listening to queue {}", IN_QUEUE);
        Flux.defer(() -> redisTemplate.opsForList().leftPop(IN_QUEUE, Duration.ofSeconds(1)))
                .repeatWhen(repeat -> repeat.delayElements(Duration.ofSeconds(1)))
                .filter(Objects::nonNull)
                .flatMap(taskMessage -> {
                    logger.info("Processing task: {}", taskMessage);
                    String processed = process(taskMessage);
                    logger.info("Processed result: {}", processed);
                    return redisTemplate.convertAndSend(REPLY_CHANNEL, processed);
                })
                .subscribe(
                        null,
                        e -> logger.error("Error in task processor", e),
                        () -> logger.info("TaskProcessor completed")
                );
    }

    private String process(String taskMessage) {
        int colonIndex = taskMessage.indexOf(':');
        String taskId = taskMessage.substring(0, colonIndex);
        String data = taskMessage.substring(colonIndex + 1);
        String result = "Processed: " + data.toUpperCase();
        return taskId + ":" + result;
    }
}