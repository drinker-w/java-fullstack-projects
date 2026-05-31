package com.dashboard.backend.controller;

import com.dashboard.backend.entity.UserBehavior;
import com.dashboard.backend.mapper.UserBehaviorMapper;
import com.dashboard.backend.service.RealtimeStatsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

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

        @PostMapping
    public void track(@RequestBody String body,
                      HttpServletRequest request,
                      HttpServletResponse response) {
        try {
            JsonNode json = objectMapper.readTree(body);

            String eventType = json.path("data").path("eventType").asText("UNKNOWN");
            String userId = json.path("userId").asText();
            String url = json.path("url").asText();
            String deviceType = json.path("deviceType").asText("PC");
            String region = json.path("region").asText("未知");

            String mappedEventType = mapEventType(eventType);

            
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(Long.parseLong(userId.replaceAll("[^0-9]", "0")));
            behavior.setEventType(mappedEventType);
            behavior.setPageUrl(url);
            behavior.setDeviceType(deviceType);
            behavior.setRegion(region);
            behavior.setCreateTime(LocalDateTime.now());

            userBehaviorMapper.insert(behavior);

            realtimeStatsService.recordPv(url);
            realtimeStatsService.recordUv(userId, url);

            log.debug("埋点数据已记录: {} - {} - {}", userId, eventType, url);

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            log.error("埋点数据处理失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

        private String mapEventType(String eventType) {
        switch (eventType) {
            case "PAGE_VIEW":
            case "PAGE_STAY":
                return "LOGIN";  // 页面访问算作登录行为
            case "CLICK":
            case "SEARCH":
                return "CLICK";
            case "PURCHASE":
                return "PURCHASE";
            default:
                return "CLICK";
        }
    }
}
