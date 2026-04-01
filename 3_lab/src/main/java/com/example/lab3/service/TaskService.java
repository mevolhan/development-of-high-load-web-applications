package com.example.lab3.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

@Service
public class TaskService {
    private static final String IN_QUEUE = "task:in";
    private static final String REPLY_CHANNEL = "task:reply";

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    public Mono<String> processTask(String data) {
        String taskId = UUID.randomUUID().toString();
        String taskMessage = taskId + ":" + data;

        // Создаем поток ответов, но не подписываемся сразу
        Flux<String> replyFlux = redisTemplate.listenTo(ChannelTopic.of(REPLY_CHANNEL))
                .map(msg -> msg.getMessage());

        // Ожидаем ответа, фильтруем по taskId
        Mono<String> replyMono = replyFlux
                .filter(msg -> msg.startsWith(taskId + ":"))
                .next()
                .map(msg -> msg.substring(taskId.length() + 1))
                .timeout(Duration.ofSeconds(30));

        // Отправляем задачу в очередь
        Mono<Void> sendTask = redisTemplate.opsForList()
                .rightPush(IN_QUEUE, taskMessage)
                .then();

        // Используем Mono.create, чтобы подписаться на канал ДО отправки
        return Mono.create(sink -> {
            // Подписываемся на replyMono
            replyMono.subscribe(
                    result -> sink.success(result),
                    sink::error,
                    () -> sink.error(new RuntimeException("No reply"))
            );
            // Отправляем задачу
            sendTask.subscribe();
        });
    }

    public Flux<String> getQueue() {
        return redisTemplate.opsForList().range(IN_QUEUE, 0, -1);
    }

    public Mono<Long> getQueueLength() {
        return redisTemplate.opsForList().size(IN_QUEUE);
    }

    public Mono<String> getReplyChannel() {
        return redisTemplate
                .opsForList()
                .rightPop(REPLY_CHANNEL);
     }
}