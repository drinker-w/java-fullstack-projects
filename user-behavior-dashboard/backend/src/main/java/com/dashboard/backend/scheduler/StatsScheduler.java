package com.dashboard.backend.scheduler;

import com.dashboard.backend.service.StatsCacheService;
import com.dashboard.backend.service.StatsService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class StatsScheduler {

    @Autowired
    private StatsService statsService;

    @Autowired(required = false)
    private StatsCacheService statsCacheService;

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

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }
}
