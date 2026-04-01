package com.example.lab3.controller;
import com.example.lab3.dto.TaskRequest;
import com.example.lab3.dto.TaskResponse;
import com.example.lab3.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @PostMapping
    public Mono<TaskResponse> submitTask(@RequestBody TaskRequest request) {
        return taskService.processTask(request.getData())
                .map(result -> {
                    TaskResponse response = new TaskResponse();
                    response.setResult(result);
                    return response;
                });
    }

    @GetMapping
    public Flux<String> getTasks() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(second -> taskService.getReplyChannel().flatMap(result -> {
                    return Mono.just(result.toUpperCase());
        }) + "\n");
    }

    @GetMapping("/count")
    public Mono<Long> getQueueCount() {
        return taskService.getQueueLength();
    }
}
