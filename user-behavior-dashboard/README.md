# 用户行为分析仪表盘

前端埋点 + 数据采集 + 可视化看板的完整数据分析系统。

## 技术栈

Spring Boot + Vue 3 + ECharts + MySQL + Redis + Docker

## 功能

- 前端埋点 SDK（tracker.js），基于 sendBeacon API
- 实时 PV/UV 统计（Redis Set + String）
- 定时数据聚合（Spring Task，每小时）
- 转化漏斗、地域分布、PV/UV 趋势图（ECharts）
- 可选演示数据生成器（设置 `DEMO_DATA_ENABLED=true` 后启用）

## 本地运行

1. 复制 `.env.example` 为 `.env` 并设置强密码
2. `docker-compose up -d`
3. 浏览器打开 `http://localhost:81`
4. 埋点 SDK：`<script src="http://localhost:81/sdk/tracker.js"></script>`

数据库和 Redis 密码必须通过环境变量 `DB_ROOT_PASSWORD`、`DB_PASSWORD`、`REDIS_PASSWORD` 提供，示例见 `.env.example`。应用使用非 root 的 `dashboard` 数据库账号。

### PowerShell

```powershell
Copy-Item .env.example .env
notepad .env
docker compose up -d
```

宿主机端口：前端 `81`、后端 `8081`、MySQL `3307`、Redis `6380`；MySQL 和 Redis 端口仅绑定本机。

### 不启动 Redis

在宿主机直接运行后端时，可设置 `REDIS_ENABLED=false`。埋点明细仍会写入 MySQL，实时 PV/UV 接口会返回 `0` 或全零小时数据。

```powershell
$env:DB_PASSWORD = "your_db_password"
$env:REDIS_ENABLED = "false"
mvn spring-boot:run
```

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
