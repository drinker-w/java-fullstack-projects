package com.dashboard.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("behavior_stats")
public class BehaviorStats {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDateTime statTime;

    // HOUR / DAY / WEEK
    private String statType;

    private Long pvCount;

    private Long uvCount;

    private BigDecimal conversionRate;

    private String region;

    private LocalDateTime createTime;
}
