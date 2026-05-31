package com.dashboard.backend.scheduler;

import com.dashboard.backend.entity.UserBehavior;
import com.dashboard.backend.mapper.UserBehaviorMapper;
import com.dashboard.backend.service.StatsCacheService;
import com.dashboard.backend.service.StatsService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Component
public class StatsScheduler {

    @Autowired
    private StatsService statsService;

    @Autowired(required = false)
    private StatsCacheService statsCacheService;

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    private static final String[] REGIONS = {"华东","华南","华北","华中","西南","西北","东北"};
    private static final String[] EVENT_TYPES = {"LOGIN","CLICK","PURCHASE"};
    private static final String[] DEVICES = {"PC","MOBILE","TABLET"};
    private static final String[] PAGES = {"/home","/product/list","/product/detail","/cart","/checkout","/order/success"};
    private final Random random = new Random();

    @Data
    public static class TaskStatus {
        private LocalDateTime lastExecuteTime;
        private Long lastExecuteDuration;
        private Integer lastProcessedCount;
        private String lastExecuteStatus;
        private Integer totalExecuteCount = 0;
    }

    private final TaskStatus taskStatus = new TaskStatus();

    @Scheduled(cron = "0 0 * * * *")
    public void executeHourlyStats() {
        log.info("========== 定时任务开始执行 ==========");
        long startTime = System.currentTimeMillis();

        try {
            // 每小时自动补充模拟数据，保证图表始终有内容
            generateHourlyMockData();

            int processedCount = statsService.aggregateHourlyStats();

            if (statsCacheService != null) {
                statsCacheService.clearAll();
            }

            long duration = System.currentTimeMillis() - startTime;
            taskStatus.setLastExecuteTime(LocalDateTime.now());
            taskStatus.setLastExecuteDuration(duration);
            taskStatus.setLastProcessedCount(processedCount);
            taskStatus.setLastExecuteStatus("SUCCESS");
            taskStatus.setTotalExecuteCount(taskStatus.getTotalExecuteCount() + 1);

            log.info("定时任务执行成功，处理{}条数据，耗时{}ms", processedCount, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            taskStatus.setLastExecuteTime(LocalDateTime.now());
            taskStatus.setLastExecuteDuration(duration);
            taskStatus.setLastProcessedCount(0);
            taskStatus.setLastExecuteStatus("FAILED: " + e.getMessage());
            taskStatus.setTotalExecuteCount(taskStatus.getTotalExecuteCount() + 1);

            log.error("定时任务执行失败: {}", e.getMessage(), e);
        }

        log.info("========== 定时任务执行结束 ==========");
    }

    private void generateHourlyMockData() {
        int count = 30 + random.nextInt(50); // 每小时 30-80 条
        LocalDateTime now = LocalDateTime.now().withMinute(random.nextInt(60)).withSecond(random.nextInt(60));

        for (int i = 0; i < count; i++) {
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId((long) (random.nextInt(500) + 1));
            behavior.setEventType(EVENT_TYPES[random.nextInt(EVENT_TYPES.length)]);
            behavior.setPageUrl(PAGES[random.nextInt(PAGES.length)]);
            behavior.setDeviceType(DEVICES[random.nextInt(DEVICES.length)]);
            behavior.setRegion(REGIONS[random.nextInt(REGIONS.length)]);
            behavior.setCreateTime(now.minusMinutes(random.nextInt(60)));
            userBehaviorMapper.insert(behavior);
        }
        log.info("自动生成{}条模拟数据", count);
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }
}
