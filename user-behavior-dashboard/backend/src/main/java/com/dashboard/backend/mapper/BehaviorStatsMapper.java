package com.dashboard.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dashboard.backend.entity.BehaviorStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;

@Mapper
public interface BehaviorStatsMapper extends BaseMapper<BehaviorStats> {

    @Insert("INSERT INTO behavior_stats " +
            "(stat_time, stat_type, pv_count, uv_count, conversion_rate, region, create_time) " +
            "VALUES (#{statTime}, #{statType}, #{pvCount}, #{uvCount}, #{conversionRate}, #{region}, #{createTime}) " +
            "ON DUPLICATE KEY UPDATE " +
            "pv_count = VALUES(pv_count), " +
            "uv_count = VALUES(uv_count), " +
            "conversion_rate = VALUES(conversion_rate), " +
            "create_time = VALUES(create_time)")
    int upsert(BehaviorStats stats);
}
