<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getPvUv } from '../api/stats'

const chartRef = ref(null)
const loading = ref(false)
const statType = ref('hour')
const currentDate = ref(new Date().toISOString().split('T')[0])
let chart = null

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPvUv({
      statType: statType.value,
      date: currentDate.value
    })
    if (res.data) {
      updateChart(res.data)
    }
  } catch (e) {
    console.error('获取PV/UV数据失败:', e)
  } finally {
    loading.value = false
  }
}

const updateChart = (data) => {
  if (!chart) return

  const xData = data.map(item => item.statTime)
  const pvData = data.map(item => item.pvCount)
  const uvData = data.map(item => item.uvCount)

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(17,24,39,0.95)',
      borderColor: 'rgba(255,255,255,0.1)',
      borderWidth: 1,
      textStyle: { color: '#e2e8f0', fontSize: 13 },
      axisPointer: {
        type: 'cross',
        crossStyle: { color: 'rgba(255,255,255,0.3)' }
      }
    },
    legend: {
      data: ['PV', 'UV'],
      bottom: 0,
      textStyle: { color: '#94a3b8', fontSize: 12 },
      itemGap: 24
    },
    grid: {
      left: '3%',
      right: '5%',
      bottom: '12%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xData,
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      axisTick: { show: false },
      axisLabel: {
        color: '#64748b',
        fontSize: 11,
        formatter: (v) => v.split(' ')[1] || v
      },
      splitLine: { show: false }
    },
    yAxis: [
      {
        type: 'value',
        name: 'PV',
        nameTextStyle: { color: '#64748b', fontSize: 11 },
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
        axisLabel: { color: '#64748b', fontSize: 11 }
      },
      {
        type: 'value',
        name: 'UV',
        nameTextStyle: { color: '#64748b', fontSize: 11 },
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { color: '#64748b', fontSize: 11 }
      }
    ],
    series: [
      {
        name: 'PV',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 3, color: '#38bdf8', shadowBlur: 10, shadowColor: 'rgba(56,189,248,0.3)' },
        itemStyle: { color: '#38bdf8', borderColor: '#0b1120', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(56, 189, 248, 0.2)' },
            { offset: 1, color: 'rgba(56, 189, 248, 0.01)' }
          ])
        },
        data: pvData,
        animationDuration: 1000,
        animationEasing: 'cubicInOut'
      },
      {
        name: 'UV',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        yAxisIndex: 1,
        lineStyle: { width: 3, color: '#34d399', shadowBlur: 10, shadowColor: 'rgba(52,211,153,0.3)' },
        itemStyle: { color: '#34d399', borderColor: '#0b1120', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(52, 211, 153, 0.15)' },
            { offset: 1, color: 'rgba(52, 211, 153, 0.01)' }
          ])
        },
        data: uvData,
        animationDuration: 1000,
        animationEasing: 'cubicInOut'
      }
    ],
    animationDuration: 1000,
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

watch(statType, () => { fetchData() })

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
      <span class="chart-title">PV/UV 趋势</span>
      <div class="chart-tabs">
        <button :class="{ active: statType === 'hour' }" @click="statType = 'hour'">按小时</button>
        <button :class="{ active: statType === 'day' }" @click="statType = 'day'">按天</button>
      </div>
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
  margin-bottom: 16px;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #e2e8f0;
}

.chart-tabs {
  display: flex;
  background: rgba(255,255,255,0.04);
  border-radius: 8px;
  padding: 3px;
  gap: 2px;
}

.chart-tabs button {
  padding: 5px 14px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #64748b;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.chart-tabs button.active {
  background: rgba(56,189,248,0.15);
  color: #38bdf8;
}

.chart-container {
  width: 100%;
  height: 340px;
}
</style>
