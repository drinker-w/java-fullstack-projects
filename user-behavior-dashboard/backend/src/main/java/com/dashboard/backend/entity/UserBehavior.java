package com.dashboard.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_behavior")
public class UserBehavior {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 访客标识由 SDK 生成，可能包含字母、下划线等字符，不能按数值 ID 处理。
     */
    private String userId;

    // LOGIN / CLICK / PURCHASE
    private String eventType;

    private String pageUrl;

    // PC / MOBILE / TABLET
    private String deviceType;

    private String region;

    private LocalDateTime createTime;
}
