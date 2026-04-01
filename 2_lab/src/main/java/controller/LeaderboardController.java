package com.example.demo.controller;

import com.example.demo.service.LeaderboardService;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @PostMapping("/{playerId}")
    public Map<String, Object> addScore(@PathVariable String playerId, @RequestParam double score) {
        leaderboardService.addScore(playerId, score);
        Map<String, Object> response = new HashMap<>();
        response.put("player", playerId);
        response.put("score", score);
        return response;
    }

    @GetMapping("/top")
    public List<Map<String, Object>> getTop(@RequestParam(defaultValue = "10") int limit) {
        Set<ZSetOperations.TypedTuple<String>> top = leaderboardService.getTopPlayers(limit);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> entry : top) {
            Map<String, Object> map = new HashMap<>();
            map.put("player", entry.getValue());
            map.put("score", entry.getScore());
            result.add(map);
        }
        return result;
    }

    @GetMapping("/rank/{playerId}")
    public Map<String, Object> getRank(@PathVariable String playerId) {
        Long rank = leaderboardService.getRank(playerId);
        Double score = leaderboardService.getScore(playerId);
        Map<String, Object> response = new HashMap<>();
        response.put("player", playerId);
        response.put("rank", rank == null ? null : rank + 1);
        response.put("score", score);
        return response;
    }
}

