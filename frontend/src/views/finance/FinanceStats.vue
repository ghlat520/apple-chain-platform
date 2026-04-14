<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">金融统计</span>
    </div>

    <!-- Summary cards -->
    <van-grid :column-num="2" :border="false" :gutter="12" v-if="summary">
      <van-grid-item>
        <div class="stat-card green">
          <div class="stat-value">{{ summary.totalLoans }}</div>
          <div class="stat-label">贷款总数</div>
        </div>
      </van-grid-item>
      <van-grid-item>
        <div class="stat-card blue">
          <div class="stat-value">{{ formatMoney(summary.totalAmount) }}</div>
          <div class="stat-label">贷款总额</div>
        </div>
      </van-grid-item>
      <van-grid-item>
        <div class="stat-card orange">
          <div class="stat-value">{{ summary.avgRate }}%</div>
          <div class="stat-label">平均利率</div>
        </div>
      </van-grid-item>
      <van-grid-item>
        <div class="stat-card red">
          <div class="stat-value">{{ summary.overdueCount }}</div>
          <div class="stat-label">逾期笔数</div>
        </div>
      </van-grid-item>
      <van-grid-item>
        <div class="stat-card purple">
          <div class="stat-value">{{ summary.overdueRate }}%</div>
          <div class="stat-label">逾期率</div>
        </div>
      </van-grid-item>
    </van-grid>

    <!-- Status distribution pie -->
    <div class="section-header" style="margin-top:16px;">
      <span class="section-title">贷款状态分布</span>
    </div>
    <div class="chart-card">
      <div ref="statusChartRef" style="height:260px;"></div>
    </div>

    <!-- Type distribution bar -->
    <div class="section-header" style="margin-top:16px;">
      <span class="section-title">贷款类型分布</span>
    </div>
    <div class="chart-card">
      <div ref="typeChartRef" style="height:260px;"></div>
    </div>

    <!-- Monthly trend line -->
    <div class="section-header" style="margin-top:16px;">
      <span class="section-title">月度趋势</span>
    </div>
    <div class="chart-card">
      <div ref="trendChartRef" style="height:260px;"></div>
    </div>

    <van-loading v-if="pageLoading" style="display:block;margin:40px auto;" size="24px">加载中...</van-loading>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { financeApi } from '@/api/finance'
import * as echarts from 'echarts/core'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import {
  GridComponent, TooltipComponent, LegendComponent,
  TitleComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  PieChart, BarChart, LineChart,
  GridComponent, TooltipComponent, LegendComponent,
  TitleComponent, CanvasRenderer
])

const LOAN_TYPE_MAP = {
  PLEDGE: '仓单质押', RECEIVABLE: '应收账款', CREDIT: '信用贷款',
  PLANT: '种植贷', WAREHOUSE: '仓储贷', TRADE: '贸易融资', EXPORT: '出口融资'
}
const STATUS_MAP = {
  PENDING: '待审批', APPROVED: '已审批', REJECTED: '已拒绝',
  DISBURSED: '已放款', REPAID: '已还款', OVERDUE: '逾期', SETTLED: '已结清'
}
const STATUS_COLORS = {
  PENDING: '#ff976a', APPROVED: '#1989fa', REJECTED: '#ee0a24',
  DISBURSED: '#07c160', REPAID: '#13c2c2', OVERDUE: '#f5222d', SETTLED: '#8c8c8c'
}

const summary = ref(null)
const pageLoading = ref(true)
const statusChartRef = ref(null)
const typeChartRef = ref(null)
const trendChartRef = ref(null)
const charts = []

function formatMoney(v) {
  if (v == null) return '-'
  if (v >= 10000) return `${(v / 10000).toFixed(1)}万`
  return `¥${Number(v).toLocaleString()}`
}

function initChart(el) {
  const chart = echarts.init(el)
  charts.push(chart)
  return chart
}

function renderStatusChart(data) {
  if (!statusChartRef.value || !data) return
  const chart = initChart(statusChartRef.value)
  const items = Object.entries(data).map(([key, value]) => ({
    name: STATUS_MAP[key] || key,
    value,
    itemStyle: { color: STATUS_COLORS[key] || '#999' }
  }))
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [{
      type: 'pie',
      radius: ['35%', '60%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6 },
      label: { show: false },
      emphasis: { label: { show: true, fontWeight: 'bold' } },
      data: items
    }]
  })
}

function renderTypeChart(data) {
  if (!typeChartRef.value || !data) return
  const chart = initChart(typeChartRef.value)
  const entries = Object.entries(data)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 12, right: 12, bottom: 30, top: 16, containLabel: true },
    xAxis: {
      type: 'category',
      data: entries.map(([k]) => LOAN_TYPE_MAP[k] || k),
      axisLabel: { fontSize: 10, interval: 0, rotate: 20 }
    },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: entries.map(([, v]) => v),
      barWidth: '40%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#07c160' },
          { offset: 1, color: '#95de64' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }]
  })
}

function renderTrendChart(data) {
  if (!trendChartRef.value || !data) return
  const chart = initChart(trendChartRef.value)
  const months = data.map(d => d.month || d.date)
  const amounts = data.map(d => d.amount || 0)
  const counts = data.map(d => d.count || 0)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['贷款金额', '贷款笔数'], bottom: 0 },
    grid: { left: 12, right: 12, bottom: 36, top: 16, containLabel: true },
    xAxis: { type: 'category', data: months },
    yAxis: [
      { type: 'value', name: '金额' },
      { type: 'value', name: '笔数' }
    ],
    series: [
      {
        name: '贷款金额',
        type: 'line',
        smooth: true,
        data: amounts,
        lineStyle: { color: '#07c160' },
        itemStyle: { color: '#07c160' },
        areaStyle: { color: 'rgba(7,193,96,0.08)' }
      },
      {
        name: '贷款笔数',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: counts,
        lineStyle: { color: '#1989fa' },
        itemStyle: { color: '#1989fa' }
      }
    ]
  })
}

onMounted(async () => {
  try {
    const [summaryRes, trendRes] = await Promise.all([
      financeApi.statisticsSummary(),
      financeApi.statisticsTrend({ months: 6 })
    ])
    summary.value = summaryRes.data
    await nextTick()
    renderStatusChart(summaryRes.data?.loansByStatus)
    renderTypeChart(summaryRes.data?.loansByType)
    renderTrendChart(trendRes.data)
  } finally {
    pageLoading.value = false
  }
})

onBeforeUnmount(() => {
  charts.forEach(c => c.dispose())
})

// Handle window resize
if (typeof window !== 'undefined') {
  const onResize = () => charts.forEach(c => c.resize())
  window.addEventListener('resize', onResize)
  onBeforeUnmount(() => window.removeEventListener('resize', onResize))
}
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
.stat-card.red { background: #fce4ec; }
.stat-card.purple { background: #f3e5f5; }
.stat-value {
  font-size: 22px;
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
