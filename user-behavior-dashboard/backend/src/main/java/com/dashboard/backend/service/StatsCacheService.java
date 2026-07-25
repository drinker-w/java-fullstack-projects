package com.dashboard.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 统计数据缓存服务
 * 当Redis可用时提供缓存功能，不可用时自动降级
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
public class StatsCacheService {

    private static final String CACHE_PREFIX = "stats:";
    private static final long CACHE_TTL = 30; // 缓存30分钟

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

        public Object get(String key) {
        if (redisTemplate == null) {
            return null;
        }
        try {
            String cacheKey = CACHE_PREFIX + key;
            Object value = redisTemplate.opsForValue().get(cacheKey);
            log.debug("缓存{}: {}", value != null ? "命中" : "未命中", key);
            return value;
        } catch (Exception e) {
            log.warn("Redis读取失败，降级处理: {}", e.getMessage());
            return null;
        }
    }

        public void set(String key, Object value) {
        if (redisTemplate == null) {
            return;
        }
        try {
            String cacheKey = CACHE_PREFIX + key;
            redisTemplate.opsForValue().set(cacheKey, value, CACHE_TTL, TimeUnit.MINUTES);
            log.debug("缓存写入: {}", key);
        } catch (Exception e) {
            log.warn("Redis写入失败，降级处理: {}", e.getMessage());
        }
    }

        public void delete(String key) {
        if (redisTemplate == null) {
            return;
        }
        try {
            String cacheKey = CACHE_PREFIX + key;
            redisTemplate.delete(cacheKey);
            log.debug("缓存删除: {}", key);
        } catch (Exception e) {
            log.warn("Redis删除失败，降级处理: {}", e.getMessage());
        }
    }

        public void clearAll() {
        if (redisTemplate == null) {
            return;
        }
        try {
            var keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("清除所有统计缓存，共{}条", keys.size());
            }
        } catch (Exception e) {
            log.warn("Redis清除失败，降级处理: {}", e.getMessage());
        }
    }
}
