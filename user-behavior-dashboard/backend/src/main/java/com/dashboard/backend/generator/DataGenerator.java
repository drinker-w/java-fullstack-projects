package com.dashboard.backend.generator;

import com.dashboard.backend.entity.UserBehavior;
import com.dashboard.backend.mapper.UserBehaviorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 模拟数据生成器
 * 启动时自动插入10000条测试数据
 */
@Slf4j
@Component
public class DataGenerator implements CommandLineRunner {

    private static final int DATA_COUNT = 10000;
    private static final int MAX_USER_ID = 500;
    private static final int DAYS_RANGE = 7;

    private static final String[] EVENT_TYPES = {"LOGIN", "CLICK", "PURCHASE"};
    private static final String[] PAGE_URLS = {
            "/home", "/product/list", "/product/detail",
            "/cart", "/checkout", "/order/success",
            "/user/profile", "/user/orders", "/search"
    };
    private static final String[] DEVICE_TYPES = {"PC", "MOBILE", "TABLET"};
    private static final String[] REGIONS = {"华北", "华东", "华南", "华中", "西南", "西北", "东北"};

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Override
    public void run(String... args) throws Exception {
        long count = userBehaviorMapper.selectCount(null);
        if (count > 0) {
            log.info("数据库已有{}条数据，跳过生成", count);
            return;
        }

        log.info("开始生成{}条模拟数据...", DATA_COUNT);
        long startTime = System.currentTimeMillis();

        List<UserBehavior> behaviors = new ArrayList<>();
        Random random = new Random();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(DAYS_RANGE);

        for (int i = 0; i < DATA_COUNT; i++) {
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId((long) (random.nextInt(MAX_USER_ID) + 1));
            behavior.setEventType(EVENT_TYPES[random.nextInt(EVENT_TYPES.length)]);
            behavior.setPageUrl(PAGE_URLS[random.nextInt(PAGE_URLS.length)]);
            behavior.setDeviceType(DEVICE_TYPES[random.nextInt(DEVICE_TYPES.length)]);
            behavior.setRegion(REGIONS[random.nextInt(REGIONS.length)]);

            long minutesBetween = ChronoUnit.MINUTES.between(startDate, now);
            long randomMinutes = (long) (random.nextDouble() * minutesBetween);
            behavior.setCreateTime(startDate.plusMinutes(randomMinutes));

            behaviors.add(behavior);

            if (behaviors.size() == 1000) {
                batchInsert(behaviors);
                behaviors.clear();
            }
        }

        if (!behaviors.isEmpty()) {
            batchInsert(behaviors);
        }

        long costTime = System.currentTimeMillis() - startTime;
        log.info("模拟数据生成完成，共{}条，耗时{}ms", DATA_COUNT, costTime);
    }

        private void batchInsert(List<UserBehavior> behaviors) {
        for (UserBehavior behavior : behaviors) {
            userBehaviorMapper.insert(behavior);
        }
        log.debug("批量插入{}条数据", behaviors.size());
    }
}
