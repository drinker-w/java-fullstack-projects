<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getRegion } from '../api/stats'

const chartRef = ref(null)
const loading = ref(false)
let chart = null

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRegion()
    if (res.data) {
      updateChart(res.data)
    }
  } catch (e) {
    console.error('获取地域数据失败:', e)
  } finally {
    loading.value = false
  }
}

const updateChart = (data) => {
  if (!chart) return

  const sortedData = [...data].sort((a, b) => b.pvCount - a.pvCount)
  const regions = sortedData.map(item => item.region)
  const values = sortedData.map(item => item.pvCount)

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(17,24,39,0.95)',
      borderColor: 'rgba(255,255,255,0.1)',
      borderWidth: 1,
      textStyle: { color: '#e2e8f0', fontSize: 13 },
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: regions,
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      axisTick: { show: false },
      axisLabel: {
        color: '#94a3b8',
        fontSize: 12,
        fontWeight: 500
      }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
      axisLabel: { color: '#64748b', fontSize: 11 }
    },
    series: [
      {
        name: 'PV',
        type: 'bar',
        barWidth: '55%',
        data: values,
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#38bdf8' },
            { offset: 1, color: '#0ea5e9' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#a78bfa' },
              { offset: 1, color: '#8b5cf6' }
            ])
          }
        },
        label: {
          show: true,
          position: 'top',
          color: '#94a3b8',
          fontSize: 11,
          fontWeight: 500,
          formatter: (params) => params.value.toLocaleString()
        }
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
      <span class="chart-title">地域分布</span>
      <span class="chart-desc">最近 7 天</span>
    </div>
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<style scoped>
.chart-wrapper {
  padding: 24px;
  border-radius: 16px;
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
}

.chart-container {
  width: 100%;
  height: 320px;
}
</style>
