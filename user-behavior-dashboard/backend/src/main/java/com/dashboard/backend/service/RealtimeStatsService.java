package com.dashboard.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 使用Redis进行实时PV/UV统计
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeStatsService {

    private final StringRedisTemplate redisTemplate;

    private static final String PV_KEY_PREFIX = "realtime:pv:";
    private static final String UV_KEY_PREFIX = "realtime:uv:";
    private static final String UV_SET_PREFIX = "realtime:uv_set:";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");

        public void recordPv(String page) {
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
    }

        public void recordUv(String userId, String page) {
        String today = LocalDate.now().format(DATE_FORMAT);
        String hour = LocalDateTime.now().format(HOUR_FORMAT);

        String dayKey = UV_SET_PREFIX + "day:" + today;
        redisTemplate.opsForSet().add(dayKey, userId);
        redisTemplate.expire(dayKey, 7, TimeUnit.DAYS);

        
        String hourKey = UV_SET_PREFIX + "hour:" + hour;
        redisTemplate.opsForSet().add(hourKey, userId);
        redisTemplate.expire(hourKey, 7, TimeUnit.DAYS);
    }

        public long getTodayPv() {
        String today = LocalDate.now().format(DATE_FORMAT);
        String key = PV_KEY_PREFIX + "day:" + today;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0;
    }

        public long getTodayUv() {
        String today = LocalDate.now().format(DATE_FORMAT);
        String key = UV_SET_PREFIX + "day:" + today;
        Long size = redisTemplate.opsForSet().size(key);
        return size != null ? size : 0;
    }

        public Map<String, Long> getHourlyPv() {
        String today = LocalDate.now().format(DATE_FORMAT);
        Map<String, Long> result = new HashMap<>();

        for (int i = 0; i < 24; i++) {
            String hour = String.format("%s-%02d", today, i);
            String key = PV_KEY_PREFIX + "hour:" + hour;
            String value = redisTemplate.opsForValue().get(key);
            result.put(String.format("%02d:00", i), value != null ? Long.parseLong(value) : 0);
        }

        return result;
    }

        public Map<String, Long> getHourlyUv() {
        String today = LocalDate.now().format(DATE_FORMAT);
        Map<String, Long> result = new HashMap<>();

        for (int i = 0; i < 24; i++) {
            String hour = String.format("%s-%02d", today, i);
            String key = UV_SET_PREFIX + "hour:" + hour;
            Long size = redisTemplate.opsForSet().size(key);
            result.put(String.format("%02d:00", i), size != null ? size : 0);
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
}
