<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getFunnel } from '../api/stats'

const chartRef = ref(null)
const loading = ref(false)
let chart = null

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getFunnel()
    if (res.data) {
      updateChart(res.data)
    }
  } catch (e) {
    console.error('获取漏斗数据失败:', e)
  } finally {
    loading.value = false
  }
}

const updateChart = (data) => {
  if (!chart) return

  const maxCount = data.length > 0 ? data[0].userCount : 1
  const colors = ['#38bdf8', '#a78bfa', '#34d399']
  const funnelData = data.map((item, index) => ({
    name: item.stage,
    value: item.userCount,
    rate: index === 0 ? 100 : ((item.userCount / maxCount) * 100).toFixed(1)
  }))

  chart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(17,24,39,0.95)',
      borderColor: 'rgba(255,255,255,0.1)',
      borderWidth: 1,
      textStyle: { color: '#e2e8f0', fontSize: 13 },
      formatter: (params) => {
        const item = funnelData[params.dataIndex]
        return `<div style="font-weight:600;margin-bottom:6px;color:#e2e8f0">${params.name}</div>
                <div style="color:#94a3b8">数量: <span style="color:#f1f5f9">${item.value.toLocaleString()}</span></div>
                <div style="color:#94a3b8">转化率: <span style="color:#38bdf8">${item.rate}%</span></div>`
      }
    },
    series: [
      {
        name: '转化漏斗',
        type: 'funnel',
        left: '10%',
        top: '5%',
        bottom: '5%',
        width: '80%',
        min: 0,
        max: 100,
        minSize: '20%',
        maxSize: '100%',
        sort: 'descending',
        gap: 6,
        label: {
          show: true,
          position: 'inside',
          formatter: (params) => {
            const item = funnelData[params.dataIndex]
            return `${params.name}\n${item.value.toLocaleString()} (${item.rate}%)`
          },
          fontSize: 13,
          color: '#fff',
          fontWeight: 600
        },
        labelLine: { show: false },
        itemStyle: {
          borderColor: '#0b1120',
          borderWidth: 3,
          borderRadius: 4
        },
        emphasis: {
          label: { fontSize: 15 }
        },
        data: funnelData.map((item, index) => ({
          name: item.name,
          value: item.value,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: colors[index] },
              { offset: 1, color: colors[index] + '99' }
            ])
          }
        }))
      }
    ],
    animationDuration: 1200,
    animationEasing: 'cubicInOut'
  })
}

const initChart = () => {
  if (chartRef.value) {
    chart = echarts.init(chartRef.value, null, { devicePixelRatio: 2 })
    fetchData()
  }
}

const handleResize = () => { chart?.resize() }

onMounted(() => {
  nextTick(initChart)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})

defineExpose({ fetchData })
</script>

<template>
  <div class="chart-wrapper glass-card" v-loading="loading">
    <div class="chart-header">
      <span class="chart-title">转化漏斗</span>
      <span class="chart-desc">LOGIN → CLICK → PURCHASE</span>
    </div>
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<style scoped>
.chart-wrapper {
  padding: 24px;
  border-radius: 16px;
  height: 100%;
}

.chart-wrapper :deep(.el-loading-mask) {
  background: rgba(11,17,32,0.6);
  backdrop-filter: blur(4px);
  border-radius: 16px;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #e2e8f0;
}

.chart-desc {
  font-size: 11px;
  color: #64748b;
  letter-spacing: 0.5px;
}

.chart-container {
  width: 100%;
  height: 340px;
}
</style>
