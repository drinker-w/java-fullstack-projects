package com.dashboard.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dashboard.backend.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

        @Select("SELECT " +
            "    DATE_FORMAT(create_time, '%Y-%m-%d %H:00:00') AS statTime, " +
            "    COUNT(*) AS pvCount, " +
            "    COUNT(DISTINCT user_id) AS uvCount " +
            "FROM user_behavior " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d %H:00:00') " +
            "ORDER BY statTime")
    List<Map<String, Object>> statsByHour(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

        @Select("SELECT " +
            "    DATE(create_time) AS statTime, " +
            "    COUNT(*) AS pvCount, " +
            "    COUNT(DISTINCT user_id) AS uvCount " +
            "FROM user_behavior " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY statTime")
    List<Map<String, Object>> statsByDay(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

        @Select("SELECT " +
            "    region, " +
            "    COUNT(*) AS pvCount, " +
            "    COUNT(DISTINCT user_id) AS uvCount " +
            "FROM user_behavior " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY region " +
            "ORDER BY pvCount DESC")
    List<Map<String, Object>> statsByRegion(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 转化漏斗统计：LOGIN -> CLICK -> PURCHASE
     * 使用子查询计算各阶段用户数
     */
    @Select("SELECT " +
            "    'LOGIN' AS stage, " +
            "    COUNT(DISTINCT user_id) AS userCount " +
            "FROM user_behavior " +
            "WHERE event_type = 'LOGIN' AND create_time BETWEEN #{startTime} AND #{endTime} " +
            "UNION ALL " +
            "SELECT " +
            "    'CLICK' AS stage, " +
            "    COUNT(DISTINCT user_id) AS userCount " +
            "FROM user_behavior " +
            "WHERE event_type = 'CLICK' AND create_time BETWEEN #{startTime} AND #{endTime} " +
            "    AND user_id IN ( " +
            "        SELECT DISTINCT user_id FROM user_behavior " +
            "        WHERE event_type = 'LOGIN' AND create_time BETWEEN #{startTime} AND #{endTime} " +
            "    ) " +
            "UNION ALL " +
            "SELECT " +
            "    'PURCHASE' AS stage, " +
            "    COUNT(DISTINCT user_id) AS userCount " +
            "FROM user_behavior " +
            "WHERE event_type = 'PURCHASE' AND create_time BETWEEN #{startTime} AND #{endTime} " +
            "    AND user_id IN ( " +
            "        SELECT DISTINCT user_id FROM user_behavior " +
            "        WHERE event_type = 'CLICK' AND create_time BETWEEN #{startTime} AND #{endTime} " +
            "            AND user_id IN ( " +
            "                SELECT DISTINCT user_id FROM user_behavior " +
            "                WHERE event_type = 'LOGIN' AND create_time BETWEEN #{startTime} AND #{endTime} " +
            "            ) " +
            "    )")
    List<Map<String, Object>> statsFunnel(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

        @Select("SELECT " +
            "    COUNT(*) AS totalPv, " +
            "    COUNT(DISTINCT user_id) AS totalUv, " +
            "    ROUND( " +
            "        COUNT(DISTINCT CASE WHEN event_type = 'PURCHASE' THEN user_id END) * 100.0 / " +
            "        NULLIF(COUNT(DISTINCT CASE WHEN event_type = 'LOGIN' THEN user_id END), 0), " +
            "        2 " +
            "    ) AS conversionRate " +
            "FROM user_behavior " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime}")
    Map<String, Object> statsOverview(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);
}
