# 用户行为分析仪表盘

前端埋点 + 数据采集 + 可视化看板的完整数据分析系统。

## 技术栈

Spring Boot + Vue 3 + ECharts + MySQL + Redis + Docker

## 功能

- 前端埋点 SDK（tracker.js），基于 sendBeacon API
- 实时 PV/UV 统计（Redis Set + String）
- 定时数据聚合（Spring Task，每小时）
- 转化漏斗、地域分布、PV/UV 趋势图（ECharts）
- 10000 条模拟数据（启动时自动生成）

## 本地运行

1. `docker-compose up -d`
2. 浏览器打开 `http://localhost:81`
3. 埋点 SDK：`<script src="http://localhost:81/sdk/tracker.js"></script>`

## 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/track | POST | 接收埋点数据 |
| /api/stats/pv-uv | GET | PV/UV 趋势 |
| /api/stats/funnel | GET | 转化漏斗 |
| /api/stats/region | GET | 地域分布 |
| /api/stats/overview | GET | 概览数据 |
| /api/stats/task-status | GET | 定时任务状态 |
| /api/realtime/overview | GET | 实时概览 |

## 在线演示

http://114.55.98.103/dashboard/
