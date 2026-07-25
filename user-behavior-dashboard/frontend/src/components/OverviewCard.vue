<script setup>
import { ref, onMounted } from 'vue'
import { getOverview } from '../api/stats'

const cards = ref([
  { title: '页面浏览量', subtitle: 'PAGE VIEWS', value: 0, change: 12.5, color: '#38bdf8', glow: 'rgba(56,189,248,0.12)' },
  { title: '独立访客', subtitle: 'UNIQUE VISITORS', value: 0, change: 8.3, color: '#34d399', glow: 'rgba(52,211,153,0.12)' },
  { title: '转化率', subtitle: 'CONVERSION RATE', value: 0, change: 3.2, color: '#a78bfa', glow: 'rgba(167,139,250,0.12)', unit: '%' },
  { title: '活跃用户', subtitle: 'ACTIVE USERS', value: 0, change: -2.1, color: '#fb7185', glow: 'rgba(251,113,133,0.12)' }
])

const loading = ref(false)

const formatNumber = (num) => {
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getOverview()
    if (res.data) {
      cards.value[0].value = res.data.totalPv || 0
      cards.value[1].value = res.data.totalUv || 0
      cards.value[2].value = res.data.conversionRate || 0
      cards.value[3].value = res.data.totalUv || 0
    }
  } catch (e) {
    console.error('获取概览数据失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

defineExpose({ fetchData })
</script>

<template>
  <div class="overview-container">
    <div class="overview-grid">
      <div
        v-for="(card, index) in cards"
        :key="index"
        class="overview-card"
        v-loading="loading"
        :style="{ '--accent': card.color, '--glow': card.glow }"
      >
        <div class="card-top-line"></div>
        <div class="card-inner">
          <div class="card-left">
            <span class="card-label">{{ card.title }}</span>
            <span class="card-subtitle">{{ card.subtitle }}</span>
            <span class="card-value" :class="{ 'gradient-text-blue': index === 0, 'gradient-text-green': index === 1 }">
              {{ card.unit === '%' ? card.value.toFixed(1) + '%' : formatNumber(card.value) }}
            </span>
          </div>
          <div class="card-right">
            <div class="card-icon-ring" :style="{ borderColor: card.color + '30' }">
              <div class="card-icon-fill" :style="{ background: card.color + '18' }">
                <div class="mini-chart" :style="{ background: card.color }"></div>
              </div>
            </div>
            <span class="card-change" :class="card.change >= 0 ? 'up' : 'down'">
              {{ card.change >= 0 ? '↑' : '↓' }}{{ Math.abs(card.change) }}%
            </span>
          </div>
        </div>
        <div class="card-glow-orb" :style="{ background: card.glow }"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.overview-container {
  margin-bottom: 28px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.overview-card {
  position: relative;
  background: linear-gradient(180deg, rgba(255,255,255,0.03) 0%, rgba(255,255,255,0.01) 100%);
  border: 1px solid rgba(255,255,255,0.07);
  border-radius: 18px;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.overview-card:hover {
  border-color: rgba(255,255,255,0.12);
  box-shadow: 0 12px 40px rgba(0,0,0,0.5), 0 0 60px var(--glow);
  transform: translateY(-2px);
}

.overview-card :deep(.el-loading-mask) {
  background: rgba(7,11,20,0.7);
  backdrop-filter: blur(4px);
  border-radius: 18px;
}

/* 顶部渐变线 */
.card-top-line {
  position: absolute;
  top: 0;
  left: 20px;
  right: 20px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--accent), transparent);
  opacity: 0.5;
  transition: opacity 0.3s;
}

.overview-card:hover .card-top-line {
  opacity: 1;
}

.card-inner {
  padding: 24px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}

.card-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
}

.card-subtitle {
  font-size: 9px;
  font-weight: 600;
  color: var(--text-muted);
  letter-spacing: 2px;
  margin-bottom: 4px;
}

.card-value {
  font-size: 34px;
  font-weight: 900;
  color: var(--text-primary);
  line-height: 1;
  letter-spacing: -2px;
  margin-top: 2px;
}

.card-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.card-icon-ring {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  border: 1.5px solid;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-icon-fill {
  width: 28px;
  height: 28px;
  border-radius: 9px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 4px;
}

.mini-chart {
  width: 6px;
  height: 6px;
  border-radius: 2px;
  opacity: 0.7;
}

.card-change {
  font-size: 13px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 8px;
}

.card-change.up {
  color: #34d399;
  background: rgba(52,211,153,0.1);
}

.card-change.down {
  color: #fb7185;
  background: rgba(251,113,133,0.1);
}

/* 光晕 */
.card-glow-orb {
  position: absolute;
  bottom: -30px;
  right: -30px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  filter: blur(50px);
  opacity: 0.3;
  pointer-events: none;
  transition: opacity 0.3s;
}

.overview-card:hover .card-glow-orb {
  opacity: 0.6;
}

@media (max-width: 1200px) {
  .overview-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }

  .card-value {
    font-size: 28px;
  }
}
</style>
