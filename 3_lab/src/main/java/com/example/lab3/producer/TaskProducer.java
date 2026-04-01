package com.example.lab3.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

@Component
public class TaskProducer {
    private static final String IN_QUEUE = "task:in";
    private static final Logger logger = LoggerFactory.getLogger(TaskProducer.class);

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    private final Random random = new Random();

    @PostConstruct
    public void generateTasks() {
        logger.info("TaskProducer started");
        Flux.generate(sink -> sink.next(1))
                .concatMap(tick -> {
                    long delay = random.nextInt(5000);
                    logger.debug("Next task after {} ms", delay);
                    return Mono.delay(Duration.ofMillis(delay)).thenReturn(tick);
                })
                .concatMap(tick -> {
                    String taskId = UUID.randomUUID().toString();
                    String taskMessage = taskId + ":auto:" + System.currentTimeMillis();
                    logger.info("Pushing task to queue: {}", taskMessage);
                    return redisTemplate.opsForList().rightPush(IN_QUEUE, taskMessage)
                            .doOnSuccess(len -> logger.info("Task added, queue length: {}", len))
                            .doOnError(e -> logger.error("Failed to push task", e));
                })
                .subscribe(null, e -> logger.error("Error in task producer", e));
    }
}