package com.dashboard.backend.controller;

import com.dashboard.backend.common.Result;
import com.dashboard.backend.scheduler.StatsScheduler;
import com.dashboard.backend.service.StatsCacheService;
import com.dashboard.backend.service.StatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @Autowired
    private StatsScheduler statsScheduler;

    @Autowired(required = false)
    private StatsCacheService statsCacheService;

    /**
     * PV/UV趋势数据
     * GET /api/stats/pv-uv?statType=hour&date=2026-05-25
     */
    @GetMapping("/pv-uv")
    public Result<List<Map<String, Object>>> getPvUv(
            @RequestParam(defaultValue = "hour") String statType,
            @RequestParam(required = false) String date) {

        if (date == null || date.isEmpty()) {
            date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        }

        String cacheKey = "pv-uv:" + statType + ":" + date;

        if (statsCacheService != null) {
            Object cached = statsCacheService.get(cacheKey);
            if (cached != null) {
                log.debug("缓存命中: {}", cacheKey);
                return Result.success((List<Map<String, Object>>) cached);
            }
        }

        List<Map<String, Object>> data;
        if ("day".equalsIgnoreCase(statType)) {
            String endDate = date;
            String startDate = LocalDate.parse(date).minusDays(7)
                    .format(DateTimeFormatter.ISO_DATE);
            data = statsService.getPvUvByDay(startDate, endDate);
        } else {
            data = statsService.getPvUvByHour(date);
        }

        
        if (statsCacheService != null) {
            statsCacheService.set(cacheKey, data);
        }

        return Result.success(data);
    }

    /**
     * 转化漏斗数据
     * GET /api/stats/funnel
     */
    @GetMapping("/funnel")
    public Result<List<Map<String, Object>>> getFunnel() {
        String cacheKey = "funnel:7d";

        if (statsCacheService != null) {
            Object cached = statsCacheService.get(cacheKey);
            if (cached != null) {
                return Result.success((List<Map<String, Object>>) cached);
            }
        }

        List<Map<String, Object>> data = statsService.getFunnelData();

        
        if (statsCacheService != null) {
            statsCacheService.set(cacheKey, data);
        }

        return Result.success(data);
    }

    /**
     * 地域分布数据
     * GET /api/stats/region
     */
    @GetMapping("/region")
    public Result<List<Map<String, Object>>> getRegion() {
        String cacheKey = "region:7d";

        if (statsCacheService != null) {
            Object cached = statsCacheService.get(cacheKey);
            if (cached != null) {
                return Result.success((List<Map<String, Object>>) cached);
            }
        }

        List<Map<String, Object>> data = statsService.getRegionDistribution();

        
        if (statsCacheService != null) {
            statsCacheService.set(cacheKey, data);
        }

        return Result.success(data);
    }

    /**
     * 今日概览数据
     * GET /api/stats/overview
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        String cacheKey = "overview:today";

        if (statsCacheService != null) {
            Object cached = statsCacheService.get(cacheKey);
            if (cached != null) {
                return Result.success((Map<String, Object>) cached);
            }
        }

        Map<String, Object> data = statsService.getTodayOverview();

        
        if (statsCacheService != null) {
            statsCacheService.set(cacheKey, data);
        }

        return Result.success(data);
    }

    /**
     * 定时任务执行状态
     * GET /api/stats/task-status
     */
    @GetMapping("/task-status")
    public Result<StatsScheduler.TaskStatus> getTaskStatus() {
        return Result.success(statsScheduler.getTaskStatus());
    }
}
