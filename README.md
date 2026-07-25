# 用户行为分析仪表盘

基于 Spring Boot + Vue 3 + ECharts 的全栈数据分析系统，Docker 一键部署。

## 技术栈

**后端：** Spring Boot 2.7 + MyBatis-Plus + MySQL + Redis + Spring Task
**前端：** Vue 3 + Vite + Element Plus + ECharts
**部署：** Docker Compose

## 功能

- 前端埋点 SDK（tracker.js），sendBeacon API 上报
- PV/UV 实时统计（Redis）
- 定时数据聚合（Spring Task，每小时执行）
- 转化漏斗、地域分布、PV/UV 趋势图
- ECharts 暗色主题，玻璃态卡片设计

项目不提供在线演示地址或测试账号，请按子项目 README 使用本地 Docker 环境启动。

`.env` 中分别设置 `DB_ROOT_PASSWORD`（MySQL 初始化用）和 `DB_PASSWORD`（应用账号用），应用默认使用非 root 的 `dashboard` 账号。

## 快速启动

```bash
cd user-behavior-dashboard
cp .env.example .env  # 修改为本地强密码
docker compose up -d
```

PowerShell 可执行：

```powershell
Set-Location user-behavior-dashboard
Copy-Item .env.example .env
notepad .env
docker compose up -d
```

- 前端：http://localhost:81
- 后端：http://localhost:8081
- 埋点 SDK：http://localhost:81/sdk/tracker.js

**环境：** Docker、Docker Compose、Java 17、Node 20

## API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/track | POST | 接收埋点数据 |
| /api/stats/pv-uv | GET | PV/UV 趋势 |
| /api/stats/funnel | GET | 转化漏斗 |
| /api/stats/region | GET | 地域分布 |
| /api/stats/overview | GET | 概览数据 |
| /api/stats/task-status | GET | 任务状态 |
| /api/realtime/overview | GET | 实时概览 |
| /api/realtime/pv | GET | 实时 PV |
| /api/realtime/uv | GET | 实时 UV |
