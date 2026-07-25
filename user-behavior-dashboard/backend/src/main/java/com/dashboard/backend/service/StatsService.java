package com.dashboard.backend.service;

import com.dashboard.backend.mapper.UserBehaviorMapper;
import com.dashboard.backend.entity.BehaviorStats;
import com.dashboard.backend.mapper.BehaviorStatsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StatsService {

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Autowired
    private BehaviorStatsMapper behaviorStatsMapper;

        public List<Map<String, Object>> getPvUvByHour(String date) {
        LocalDate targetDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        LocalDateTime startTime = targetDate.atStartOfDay();
        LocalDateTime endTime = targetDate.atTime(LocalTime.MAX);
        return userBehaviorMapper.statsByHour(startTime, endTime);
    }

        public List<Map<String, Object>> getPvUvByDay(String startDate, String endDate) {
        LocalDateTime startTime = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime endTime = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        return userBehaviorMapper.statsByDay(startTime, endTime);
    }

        public List<Map<String, Object>> getFunnelData() {
        // 查询所有数据，不设时间限制
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);
        LocalDateTime startTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        return userBehaviorMapper.statsFunnel(startTime, endTime);
    }

        public List<Map<String, Object>> getRegionDistribution() {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(7);
        return userBehaviorMapper.statsByRegion(startTime, endTime);
    }

        public Map<String, Object> getTodayOverview() {
        LocalDateTime startTime = LocalDate.now().atStartOfDay();
        LocalDateTime endTime = LocalDateTime.now();
        Map<String, Object> overview = userBehaviorMapper.statsOverview(startTime, endTime);

        if (overview == null) {
            overview = new HashMap<>();
            overview.put("totalPv", 0L);
            overview.put("totalUv", 0L);
            overview.put("conversionRate", 0.0);
        }
        return overview;
    }

    /**
     * 执行数据聚合（定时任务调用）
     * 按小时统计并记录到behavior_stats表
     */
    @Transactional
    public int aggregateHourlyStats() {
        // 查询最近7天的数据进行聚合
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(7);

        log.info("开始执行数据聚合：{} - {}", startTime, endTime);

        List<Map<String, Object>> hourlyStats = userBehaviorMapper.statsByHour(startTime, endTime);

        int processedCount = 0;
        for (Map<String, Object> stat : hourlyStats) {
            BehaviorStats behaviorStats = new BehaviorStats();
            behaviorStats.setStatTime(toLocalDateTime(stat.get("statTime")));
            behaviorStats.setStatType("HOUR");
            behaviorStats.setPvCount(toLong(stat.get("pvCount")));
            behaviorStats.setUvCount(toLong(stat.get("uvCount")));
            behaviorStats.setConversionRate(toBigDecimal(stat.get("conversionRate")));
            // 空字符串保证总体聚合命中唯一键，重复执行时更新而不是追加。
            behaviorStats.setRegion("");
            behaviorStats.setCreateTime(LocalDateTime.now());
            behaviorStatsMapper.upsert(behaviorStats);

            log.info("聚合数据 - 时间: {}, PV: {}, UV: {}",
                    stat.get("statTime"),
                    stat.get("pvCount"),
                    stat.get("uvCount"));
            processedCount++;
        }

        log.info("数据聚合完成，处理{}条记录", processedCount);
        return processedCount;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value == null) {
            throw new IllegalArgumentException("聚合时间不能为空");
        }
        return LocalDateTime.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }
}
