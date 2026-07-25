<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTaskStatus } from '../api/stats'

const router = useRouter()
const loading = ref(false)
const taskInfo = ref({
  lastRunTime: '--',
  processedCount: 0,
  status: 'normal',
  statusText: '正常',
  errorMessage: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTaskStatus()
    if (res.data) {
      taskInfo.value = {
        lastRunTime: res.data.lastRunTime || '--',
        processedCount: res.data.processedCount || 0,
        status: res.data.status || 'normal',
        statusText: res.data.statusText || '正常',
        errorMessage: res.data.errorMessage || ''
      }
    }
  } catch (e) {
    console.error('获取任务状态失败:', e)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/dashboard')
}

onMounted(fetchData)
</script>

<template>
  <div class="task-page">
    <!-- 背景 -->
    <div class="bg-layer">
      <div class="bg-grid"></div>
      <div class="bg-orb bg-orb-1"></div>
    </div>

    <header class="page-header">
      <div class="header-left">
        <button class="btn-glass" @click="goBack">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="15 18 9 12 15 6"/></svg>
          返回看板
        </button>
        <div class="header-title-group">
          <h1 class="page-title">定时任务状态</h1>
          <span class="page-subtitle">SCHEDULED TASK MONITOR</span>
        </div>
      </div>
      <button class="btn-glass" @click="fetchData">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
        刷新
      </button>
    </header>

    <main class="page-content" v-loading="loading">
      <div class="status-grid">
        <!-- 执行时间 -->
        <div class="status-card" style="--accent: #38bdf8; --glow: rgba(56,189,248,0.12)">
          <div class="card-top-line"></div>
          <div class="card-icon-box" style="background:rgba(56,189,248,0.08);border-color:rgba(56,189,248,0.15)">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#38bdf8" stroke-width="1.5" stroke-linecap="round">
              <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
            </svg>
          </div>
          <div class="card-label">最近执行时间</div>
          <div class="card-subtitle">LAST EXECUTION</div>
          <div class="card-value-text">{{ taskInfo.lastRunTime }}</div>
        </div>

        <!-- 处理量 -->
        <div class="status-card" style="--accent: #34d399; --glow: rgba(52,211,153,0.12)">
          <div class="card-top-line"></div>
          <div class="card-icon-box" style="background:rgba(52,211,153,0.08);border-color:rgba(52,211,153,0.15)">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#34d399" stroke-width="1.5" stroke-linecap="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
            </svg>
          </div>
          <div class="card-label">处理数据量</div>
          <div class="card-subtitle">PROCESSED RECORDS</div>
          <div class="card-value-big gradient-text-green">{{ taskInfo.processedCount.toLocaleString() }}</div>
        </div>

        <!-- 状态 -->
        <div class="status-card" :style="{
          '--accent': taskInfo.status === 'normal' ? '#34d399' : '#fb7185',
          '--glow': taskInfo.status === 'normal' ? 'rgba(52,211,153,0.12)' : 'rgba(251,113,133,0.12)'
        }">
          <div class="card-top-line"></div>
          <div class="card-icon-box" :style="{
            background: taskInfo.status === 'normal' ? 'rgba(52,211,153,0.08)' : 'rgba(251,113,133,0.08)',
            borderColor: taskInfo.status === 'normal' ? 'rgba(52,211,153,0.15)' : 'rgba(251,113,133,0.15)'
          }">
            <svg v-if="taskInfo.status === 'normal'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#34d399" stroke-width="1.5" stroke-linecap="round">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
            <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#fb7185" stroke-width="1.5" stroke-linecap="round">
              <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
          </div>
          <div class="card-label">任务状态</div>
          <div class="card-subtitle">STATUS</div>
          <div class="card-value-text">
            <span class="status-badge" :class="taskInfo.status === 'normal' ? 'success' : 'error'">
              <span class="badge-dot" :class="taskInfo.status === 'normal' ? 'success' : 'error'"></span>
              {{ taskInfo.statusText }}
            </span>
          </div>
        </div>
      </div>

      <!-- 错误信息 -->
      <div v-if="taskInfo.errorMessage" class="error-card" style="--accent: #fb7185">
        <div class="card-top-line"></div>
        <div class="error-header">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fb7185" stroke-width="2" stroke-linecap="round">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
          <span>错误详情</span>
        </div>
        <div class="error-body">{{ taskInfo.errorMessage }}</div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.task-page {
  min-height: 100vh;
  background: var(--bg-deep);
  padding: 28px 36px 48px;
  position: relative;
  overflow: hidden;
}

/* ===== 背景 ===== */
.bg-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, rgba(255,255,255,0.025) 1px, transparent 1px);
  background-size: 32px 32px;
  mask-image: radial-gradient(ellipse 60% 50% at 50% 30%, black 30%, transparent 70%);
}

.bg-orb-1 {
  position: absolute;
  width: 400px;
  height: 400px;
  background: rgba(167,139,250,0.04);
  border-radius: 50%;
  filter: blur(120px);
  top: -100px;
  right: -80px;
  animation: orbFloat 20s ease-in-out infinite;
}

@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20px, -15px) scale(1.05); }
}

/* ===== 顶栏 ===== */
.page-header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
  padding: 18px 28px;
  background: linear-gradient(135deg, rgba(255,255,255,0.025), rgba(255,255,255,0.01));
  border: 1px solid rgba(255,255,255,0.07);
  border-radius: 18px;
  backdrop-filter: blur(20px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.3px;
  line-height: 1.2;
}

.page-subtitle {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-muted);
  letter-spacing: 2.5px;
  line-height: 1;
}

/* ===== 主内容 ===== */
.page-content {
  position: relative;
  z-index: 1;
  max-width: 1000px;
  margin: 0 auto;
}

.page-content :deep(.el-loading-mask) {
  background: rgba(7,11,20,0.7);
  backdrop-filter: blur(4px);
  border-radius: 16px;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

/* ===== 状态卡片 ===== */
.status-card {
  position: relative;
  background: linear-gradient(180deg, rgba(255,255,255,0.03) 0%, rgba(255,255,255,0.01) 100%);
  border: 1px solid rgba(255,255,255,0.07);
  border-radius: 18px;
  overflow: hidden;
  padding: 32px 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.status-card:hover {
  border-color: rgba(255,255,255,0.12);
  box-shadow: 0 12px 40px rgba(0,0,0,0.5), 0 0 60px var(--glow);
  transform: translateY(-2px);
}

.card-top-line {
  position: absolute;
  top: 0;
  left: 24px;
  right: 24px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--accent), transparent);
  opacity: 0.5;
  transition: opacity 0.3s;
}

.status-card:hover .card-top-line {
  opacity: 1;
}

.card-icon-box {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  border: 1px solid;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.card-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  position: relative;
  z-index: 1;
  line-height: 1;
}

.card-subtitle {
  font-size: 9px;
  font-weight: 600;
  color: var(--text-muted);
  letter-spacing: 2px;
  position: relative;
  z-index: 1;
  line-height: 1;
}

.card-value-text {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  position: relative;
  z-index: 1;
}

.card-value-big {
  font-size: 36px;
  font-weight: 900;
  letter-spacing: -2px;
  position: relative;
  z-index: 1;
}

/* ===== 状态徽章 ===== */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 18px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}

.status-badge.success {
  background: rgba(52,211,153,0.1);
  color: #34d399;
  border: 1px solid rgba(52,211,153,0.18);
}

.status-badge.error {
  background: rgba(251,113,133,0.1);
  color: #fb7185;
  border: 1px solid rgba(251,113,133,0.18);
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.badge-dot.success {
  background: #34d399;
  box-shadow: 0 0 6px rgba(52,211,153,0.5);
}

.badge-dot.error {
  background: #fb7185;
  box-shadow: 0 0 6px rgba(251,113,133,0.5);
}

/* ===== 错误卡片 ===== */
.error-card {
  position: relative;
  background: linear-gradient(180deg, rgba(255,255,255,0.03), rgba(255,255,255,0.01));
  border: 1px solid rgba(255,255,255,0.07);
  border-radius: 18px;
  overflow: hidden;
  padding: 24px;
}

.error-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #fb7185;
  margin-bottom: 14px;
}

.error-body {
  font-size: 14px;
  color: #e2e8f0;
  line-height: 1.7;
  padding: 14px 18px;
  background: rgba(251,113,133,0.05);
  border: 1px solid rgba(251,113,133,0.1);
  border-radius: 10px;
}

@media (max-width: 768px) {
  .task-page {
    padding: 20px 16px 40px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
    padding: 16px 20px;
  }

  .header-left {
    flex-wrap: wrap;
    gap: 10px;
  }

  .page-subtitle {
    display: none;
  }

  .status-grid {
    grid-template-columns: 1fr;
  }
}
</style>
