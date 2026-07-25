package com.dashboard.backend.controller;

import com.dashboard.backend.common.Result;
import com.dashboard.backend.service.RealtimeStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/realtime")
@RequiredArgsConstructor
public class RealtimeStatsController {

    private final RealtimeStatsService realtimeStatsService;

        @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> data = realtimeStatsService.getRealtimeOverview();
        return Result.success(data);
    }

        @GetMapping("/pv")
    public Result<Long> getTodayPv() {
        return Result.success(realtimeStatsService.getTodayPv());
    }

        @GetMapping("/uv")
    public Result<Long> getTodayUv() {
        return Result.success(realtimeStatsService.getTodayUv());
    }
}
