-- 用户行为数据分析平台数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS user_behavior DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE user_behavior;

-- 用户行为记录表
CREATE TABLE IF NOT EXISTS user_behavior (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    event_type VARCHAR(20) NOT NULL COMMENT '事件类型：LOGIN-登录，CLICK-点击，PURCHASE-购买',
    page_url VARCHAR(200) COMMENT '页面URL',
    device_type VARCHAR(20) COMMENT '设备类型：PC-电脑，MOBILE-手机，TABLET-平板',
    region VARCHAR(20) COMMENT '地域：华北/华东/华南/华中/西南/西北/东北',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_event_type (event_type),
    INDEX idx_create_time (create_time),
    INDEX idx_region (region)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为记录表';

-- 行为统计聚合表
CREATE TABLE IF NOT EXISTS behavior_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    stat_time DATETIME NOT NULL COMMENT '统计时间',
    stat_type VARCHAR(10) NOT NULL COMMENT '统计类型：HOUR-按小时，DAY-按天，WEEK-按周',
    pv_count BIGINT DEFAULT 0 COMMENT 'PV数量（页面浏览量）',
    uv_count BIGINT DEFAULT 0 COMMENT 'UV数量（独立访客数）',
    conversion_rate DECIMAL(5,2) DEFAULT 0 COMMENT '转化率',
    region VARCHAR(20) COMMENT '地域',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_stat_time (stat_time),
    INDEX idx_stat_type (stat_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行为统计聚合表';
