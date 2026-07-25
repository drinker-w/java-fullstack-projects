package com.dashboard.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 实时统计服务
 * 使用Redis进行实时PV/UV统计；Redis禁用或不可用时返回降级结果。
 */
@Slf4j
@Service
public class RealtimeStatsService {

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Value("${spring.redis.enabled:true}")
    private boolean redisEnabled;

    private static final String PV_KEY_PREFIX = "realtime:pv:";
    private static final String UV_KEY_PREFIX = "realtime:uv:";
    private static final String UV_SET_PREFIX = "realtime:uv_set:";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");

    public void recordPv(String page) {
        if (!isAvailable()) {
            return;
        }
        try {
            String today = LocalDate.now().format(DATE_FORMAT);
            String hour = LocalDateTime.now().format(HOUR_FORMAT);

            String dayKey = PV_KEY_PREFIX + "day:" + today;
            redisTemplate.opsForValue().increment(dayKey);
            redisTemplate.expire(dayKey, 7, TimeUnit.DAYS);

            String hourKey = PV_KEY_PREFIX + "hour:" + hour;
            redisTemplate.opsForValue().increment(hourKey);
            redisTemplate.expire(hourKey, 7, TimeUnit.DAYS);

            String pageKey = PV_KEY_PREFIX + "page:" + today + ":" + page;
            redisTemplate.opsForValue().increment(pageKey);
            redisTemplate.expire(pageKey, 7, TimeUnit.DAYS);
        } catch (RuntimeException e) {
            log.warn("Redis PV 统计不可用，跳过实时更新: {}", e.getMessage());
        }
    }

    public void recordUv(String userId, String page) {
        if (!isAvailable()) {
            return;
        }
        try {
            String today = LocalDate.now().format(DATE_FORMAT);
            String hour = LocalDateTime.now().format(HOUR_FORMAT);

            String dayKey = UV_SET_PREFIX + "day:" + today;
            redisTemplate.opsForSet().add(dayKey, userId);
            redisTemplate.expire(dayKey, 7, TimeUnit.DAYS);

            String hourKey = UV_SET_PREFIX + "hour:" + hour;
            redisTemplate.opsForSet().add(hourKey, userId);
            redisTemplate.expire(hourKey, 7, TimeUnit.DAYS);
        } catch (RuntimeException e) {
            log.warn("Redis UV 统计不可用，跳过实时更新: {}", e.getMessage());
        }
    }

    public long getTodayPv() {
        if (!isAvailable()) {
            return 0L;
        }
        try {
            String today = LocalDate.now().format(DATE_FORMAT);
            String key = PV_KEY_PREFIX + "day:" + today;
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Long.parseLong(value) : 0;
        } catch (RuntimeException e) {
            log.warn("Redis 今日 PV 查询失败，返回 0: {}", e.getMessage());
            return 0L;
        }
    }

    public long getTodayUv() {
        if (!isAvailable()) {
            return 0L;
        }
        try {
            String today = LocalDate.now().format(DATE_FORMAT);
            String key = UV_SET_PREFIX + "day:" + today;
            Long size = redisTemplate.opsForSet().size(key);
            return size != null ? size : 0;
        } catch (RuntimeException e) {
            log.warn("Redis 今日 UV 查询失败，返回 0: {}", e.getMessage());
            return 0L;
        }
    }

    public Map<String, Long> getHourlyPv() {
        String today = LocalDate.now().format(DATE_FORMAT);
        Map<String, Long> result = new HashMap<>();

        for (int i = 0; i < 24; i++) {
            String hour = String.format("%s-%02d", today, i);
            String key = PV_KEY_PREFIX + "hour:" + hour;
            long value = 0L;
            if (isAvailable()) {
                try {
                    String storedValue = redisTemplate.opsForValue().get(key);
                    value = storedValue != null ? Long.parseLong(storedValue) : 0L;
                } catch (RuntimeException e) {
                    log.warn("Redis 小时 PV 查询失败，返回 0: {}", e.getMessage());
                }
            }
            result.put(String.format("%02d:00", i), value);
        }

        return result;
    }

    public Map<String, Long> getHourlyUv() {
        String today = LocalDate.now().format(DATE_FORMAT);
        Map<String, Long> result = new HashMap<>();

        for (int i = 0; i < 24; i++) {
            String hour = String.format("%s-%02d", today, i);
            String key = UV_SET_PREFIX + "hour:" + hour;
            long value = 0L;
            if (isAvailable()) {
                try {
                    Long size = redisTemplate.opsForSet().size(key);
                    value = size != null ? size : 0L;
                } catch (RuntimeException e) {
                    log.warn("Redis 小时 UV 查询失败，返回 0: {}", e.getMessage());
                }
            }
            result.put(String.format("%02d:00", i), value);
        }

        return result;
    }

    public Map<String, Object> getRealtimeOverview() {
        Map<String, Object> data = new HashMap<>();
        data.put("todayPv", getTodayPv());
        data.put("todayUv", getTodayUv());
        data.put("hourlyPv", getHourlyPv());
        data.put("hourlyUv", getHourlyUv());
        return data;
    }

    private boolean isAvailable() {
        return redisEnabled && redisTemplate != null;
    }
}
