package com.dashboard.backend.controller;

import com.dashboard.backend.entity.UserBehavior;
import com.dashboard.backend.mapper.UserBehaviorMapper;
import com.dashboard.backend.service.RealtimeStatsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 数据采集接口
 * 接收前端埋点SDK上报的用户行为数据
 */
@Slf4j
@RestController
@RequestMapping("/api/track")
@RequiredArgsConstructor
public class TrackController {

    private final UserBehaviorMapper userBehaviorMapper;
    private final RealtimeStatsService realtimeStatsService;
    private final ObjectMapper objectMapper;

    private static final int MAX_BODY_LENGTH = 64 * 1024;
    private static final int MAX_USER_ID_LENGTH = 128;
    private static final int MAX_URL_LENGTH = 512;
    private static final int MAX_REGION_LENGTH = 32;
    private static final Pattern EVENT_TYPE_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9_]{0,31}");
    private static final Set<String> DEVICE_TYPES = Set.of("PC", "MOBILE", "TABLET");

    @PostMapping
    public ResponseEntity<Void> track(@RequestBody(required = false) String body) {
        JsonNode json = parseAndValidate(body);
        JsonNode data = json.get("data");

        String eventType = requiredText(data, "eventType", 32);
        if (!EVENT_TYPE_PATTERN.matcher(eventType).matches()) {
            throw new IllegalArgumentException("eventType 格式不正确");
        }
        String userId = requiredText(json, "userId", MAX_USER_ID_LENGTH);
        String url = requiredText(json, "url", MAX_URL_LENGTH);
        String deviceType = optionalText(json, "deviceType", "PC", 20);
        if (!DEVICE_TYPES.contains(deviceType)) {
            throw new IllegalArgumentException("deviceType 不受支持");
        }
        String region = optionalText(json, "region", "未知", MAX_REGION_LENGTH);

        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        // 保留 SDK 原始事件类型，避免将页面浏览误计为登录。
        behavior.setEventType(eventType);
        behavior.setPageUrl(url);
        behavior.setDeviceType(deviceType);
        behavior.setRegion(region);
        behavior.setCreateTime(LocalDateTime.now());

        userBehaviorMapper.insert(behavior);

        try {
            realtimeStatsService.recordPv(url);
            realtimeStatsService.recordUv(userId, url);
        } catch (RuntimeException e) {
            // Redis 仅用于实时指标，不能让埋点明细因缓存故障丢失。
            log.warn("实时统计更新失败，已保留埋点明细: {}", e.getMessage());
        }

        log.debug("埋点数据已记录: {} - {} - {}", userId, eventType, url);
        return ResponseEntity.noContent().build();
    }

    private JsonNode parseAndValidate(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (body.length() > MAX_BODY_LENGTH) {
            throw new IllegalArgumentException("请求体超过大小限制");
        }
        JsonNode json;
        try {
            json = objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("请求体不是合法 JSON", e);
        }
        if (json == null || !json.isObject()) {
            throw new IllegalArgumentException("请求体必须是 JSON 对象");
        }
        JsonNode data = json.get("data");
        if (data == null || !data.isObject()) {
            throw new IllegalArgumentException("data 必须是 JSON 对象");
        }
        return json;
    }

    private String requiredText(JsonNode parent, String field, int maxLength) {
        JsonNode value = parent.get(field);
        if (value == null || !value.isTextual()) {
            throw new IllegalArgumentException(field + " 必须是文本");
        }
        String text = value.asText().trim();
        if (text.isEmpty() || text.length() > maxLength) {
            throw new IllegalArgumentException(field + " 长度不合法");
        }
        return text;
    }

    private String optionalText(JsonNode parent, String field, String defaultValue, int maxLength) {
        JsonNode value = parent.get(field);
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        if (!value.isTextual()) {
            throw new IllegalArgumentException(field + " 必须是文本");
        }
        String text = value.asText().trim();
        if (text.isEmpty() || text.length() > maxLength) {
            throw new IllegalArgumentException(field + " 长度不合法");
        }
        return text;
    }
}
