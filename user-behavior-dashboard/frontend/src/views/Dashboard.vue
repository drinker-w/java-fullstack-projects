<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import OverviewCard from '../components/OverviewCard.vue'
import PvUvChart from '../components/PvUvChart.vue'
import FunnelChart from '../components/FunnelChart.vue'
import RegionMap from '../components/RegionMap.vue'

const router = useRouter()
const overviewRef = ref(null)
const pvUvRef = ref(null)
const funnelRef = ref(null)
const regionRef = ref(null)

const refreshAll = () => {
  overviewRef.value?.fetchData()
  pvUvRef.value?.fetchData()
  funnelRef.value?.fetchData()
  regionRef.value?.fetchData()
}

const goToTaskStatus = () => {
  router.push('/task-status')
}
</script>

<template>
  <div class="dashboard">
    <!-- 背景装饰层 -->
    <div class="bg-layer">
      <div class="bg-grid"></div>
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-orb bg-orb-3"></div>
    </div>

    <header class="dashboard-header">
      <div class="header-left">
        <div class="logo-mark">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
            <path d="M3 3h7v7H3V3z" fill="rgba(56,189,248,0.5)"/>
            <path d="M14 3h7v7h-7V3z" fill="rgba(167,139,250,0.35)"/>
            <path d="M3 14h7v7H3v-7z" fill="rgba(52,211,153,0.3)"/>
            <path d="M14 14h7v7h-7v-7z" fill="rgba(56,189,248,0.2)"/>
          </svg>
        </div>
        <div class="header-text">
          <h1 class="header-title">用户行为分析</h1>
          <span class="header-subtitle">REAL-TIME ANALYTICS DASHBOARD</span>
        </div>
      </div>
      <div class="header-right">
        <div class="status-dot"></div>
        <span class="status-text">系统运行中</span>
        <span class="header-divider"></span>
        <button class="btn-glass" @click="refreshAll">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
          刷新
        </button>
        <button class="btn-glass" @click="goToTaskStatus">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          任务
        </button>
      </div>
    </header>

    <main class="dashboard-content">
      <OverviewCard ref="overviewRef" />

      <div class="charts-grid">
        <div class="chart-cell">
          <PvUvChart ref="pvUvRef" />
        </div>
        <div class="chart-cell">
          <FunnelChart ref="funnelRef" />
        </div>
      </div>

      <div class="chart-row-full">
        <RegionMap ref="regionRef" />
      </div>
    </main>
  </div>
</template>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: var(--bg-deep);
  padding: 28px 36px 48px;
  position: relative;
  overflow: hidden;
}

/* ===== 背景装饰层 ===== */
.bg-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(circle, rgba(255,255,255,0.03) 1px, transparent 1px);
  background-size: 32px 32px;
  mask-image: radial-gradient(ellipse 80% 60% at 50% 0%, black 30%, transparent 70%);
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.4;
}

.bg-orb-1 {
  width: 500px;
  height: 500px;
  background: rgba(56,189,248,0.06);
  top: -150px;
  right: -100px;
  animation: orbFloat 20s ease-in-out infinite;
}

.bg-orb-2 {
  width: 400px;
  height: 400px;
  background: rgba(167,139,250,0.05);
  bottom: -100px;
  left: -80px;
  animation: orbFloat 25s ease-in-out infinite reverse;
}

.bg-orb-3 {
  width: 350px;
  height: 350px;
  background: rgba(52,211,153,0.04);
  top: 40%;
  left: 50%;
  animation: orbFloat 18s ease-in-out infinite 5s;
}

@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -20px) scale(1.08); }
  66% { transform: translate(-20px, 15px) scale(0.95); }
}

/* ===== 顶栏 ===== */
.dashboard-header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
  padding: 20px 32px;
  background: linear-gradient(135deg, rgba(255,255,255,0.025), rgba(255,255,255,0.01));
  border: 1px solid rgba(255,255,255,0.07);
  border-radius: 18px;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-mark {
  width: 44px;
  height: 44px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.header-title {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #e2e8f0 0%, #f1f5f9 50%, #94a3b8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.3px;
}

.header-subtitle {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-muted);
  letter-spacing: 2.5px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.status-dot {
  width: 7px;
  height: 7px;
  background: #34d399;
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(52,211,153,0.5);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.status-text {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
}

.header-divider {
  width: 1px;
  height: 20px;
  background: rgba(255,255,255,0.08);
}

.dashboard-content {
  position: relative;
  z-index: 1;
  max-width: 1440px;
  margin: 0 auto;
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-row-full {
  margin-bottom: 0;
}

@media (max-width: 1024px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }

  .dashboard {
    padding: 20px 16px 40px;
  }
}

@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
    padding: 18px 20px;
  }

  .header-title {
    font-size: 17px;
  }

  .header-right {
    width: 100%;
    flex-wrap: wrap;
  }

  .header-divider {
    display: none;
  }
}
</style>
