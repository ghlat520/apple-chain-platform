<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">冷链统计</span>
    </div>

    <!-- 概览卡片 -->
    <div v-if="summary">
      <van-grid :column-num="2" :border="false" :gutter="12">
        <van-grid-item>
          <div class="stat-card green">
            <div class="stat-value">{{ summary.vehicleCount ?? '-' }}</div>
            <div class="stat-label">车辆总数</div>
          </div>
        </van-grid-item>
        <van-grid-item>
          <div class="stat-card blue">
            <div class="stat-value">{{ summary.transportCount ?? '-' }}</div>
            <div class="stat-label">运输总次数</div>
          </div>
        </van-grid-item>
        <van-grid-item>
          <div class="stat-card orange">
            <div class="stat-value">{{ formatRate(summary.completionRate) }}</div>
            <div class="stat-label">完成率</div>
          </div>
        </van-grid-item>
        <van-grid-item>
          <div class="stat-card purple">
            <div class="stat-value">{{ summary.avgTemp != null ? `${summary.avgTemp}°C` : '-' }}</div>
            <div class="stat-label">平均温度</div>
          </div>
        </van-grid-item>
      </van-grid>
    </div>

    <!-- 车辆类型分布 环形图 -->
    <div class="section-header" style="margin-top:16px;">
      <span class="section-title">车辆类型分布</span>
    </div>
    <div class="chart-card">
      <div ref="typeChartRef" style="height:260px;"></div>
    </div>

    <!-- 车辆状态分布 环形图 -->
    <div class="section-header" style="margin-top:16px;">
      <span class="section-title">车辆状态分布</span>
    </div>
    <div class="chart-card">
      <div ref="statusChartRef" style="height:260px;"></div>
    </div>

    <!-- 运输趋势 折线图 -->
    <div class="section-header" style="margin-top:16px;">
      <span class="section-title">运输趋势（近30天）</span>
    </div>
    <div class="chart-card">
      <div ref="trendChartRef" style="height:260px;"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { coldchainApi } from '@/api/coldchain'
import * as echarts from 'echarts/core'
import { PieChart, LineChart } from 'echarts/charts'
import {
  GridComponent, TooltipComponent, LegendComponent, TitleComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  PieChart, LineChart, GridComponent, TooltipComponent,
  LegendComponent, TitleComponent, CanvasRenderer
])

const summary = ref(null)
const typeChartRef = ref(null)
const statusChartRef = ref(null)
const trendChartRef = ref(null)

let typeChart = null
let statusChart = null
let trendChart = null

const vehicleTypeLabels = {
  REFRIGERATED: '冷藏车',
  INSULATED: '保温车',
  NORMAL: '普通车'
}
const vehicleStatusLabels = {
  IDLE: '空闲',
  IN_TRANSIT: '运输中',
  MAINTENANCE: '维修中',
  RETIRED: '已退役'
}

function formatRate(val) {
  if (val == null) return '-'
  return typeof val === 'number' ? `${(val * 100).toFixed(1)}%` : val
}

function buildPieData(map, labels) {
  if (!map) return []
  return Object.entries(map).map(([key, value]) => ({
    name: labels[key] || key,
    value
  }))
}

function renderTypeChart(data) {
  if (!typeChartRef.value) return
  typeChart = echarts.init(typeChartRef.value)
  const chartData = buildPieData(data, vehicleTypeLabels)
  typeChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, left: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{c}辆' },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
      data: chartData
    }],
    color: ['#1989fa', '#07c160', '#c8c9cc']
  })
}

function renderStatusChart(data) {
  if (!statusChartRef.value) return
  statusChart = echarts.init(statusChartRef.value)
  const chartData = buildPieData(data, vehicleStatusLabels)
  statusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, left: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{c}辆' },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
      data: chartData
    }],
    color: ['#07c160', '#1989fa', '#ff976a', '#c8c9cc']
  })
}

function renderTrendChart(data) {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const dates = data?.map(d => d.date) || []
  const counts = data?.map(d => d.count) || []
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { rotate: 45, fontSize: 10 }
    },
    yAxis: { type: 'value', name: '运输次数' },
    series: [{
      name: '运输次数',
      type: 'line',
      data: counts,
      smooth: true,
      areaStyle: { color: 'rgba(25,137,250,0.1)' },
      lineStyle: { color: '#1989fa' },
      itemStyle: { color: '#1989fa' }
    }]
  })
}

onMounted(async () => {
  const [summaryRes, trendRes] = await Promise.all([
    coldchainApi.statisticsSummary(),
    coldchainApi.statisticsTransportTrend({ days: 30 })
  ])

  summary.value = summaryRes.data
  await nextTick()

  renderTypeChart(summaryRes.data?.vehicleByType)
  renderStatusChart(summaryRes.data?.vehicleByStatus)
  renderTrendChart(trendRes.data)
})

onBeforeUnmount(() => {
  typeChart?.dispose()
  statusChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.stat-card {
  width: 100%;
  padding: 16px;
  border-radius: 12px;
  text-align: center;
}
.stat-card.green { background: #e8f5e9; }
.stat-card.blue { background: #e3f2fd; }
.stat-card.orange { background: #fff8e1; }
.stat-card.purple { background: #f3e5f5; }
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 4px;
}
.stat-label { font-size: 12px; color: #666; }
.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
</style>
