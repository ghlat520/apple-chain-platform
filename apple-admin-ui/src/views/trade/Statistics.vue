<template>
  <div class="page-container">
    <div class="page-header">
      <h2>交易统计</h2>
    </div>

    <!-- Filter -->
    <div class="filter-bar" style="margin-bottom:20px">
      <el-select v-model="filterVariety" placeholder="品种（价格指数用）" clearable style="width:180px">
        <el-option label="红富士" value="红富士" />
        <el-option label="嘎啦" value="嘎啦" />
        <el-option label="黄金维纳斯" value="黄金维纳斯" />
        <el-option label="秦冠" value="秦冠" />
      </el-select>
      <el-button type="primary" @click="loadAll" icon="Search">查询</el-button>
    </div>

    <!-- KPI Cards -->
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-label">总订单数</div>
          <div class="kpi-value nums-tabular">{{ summary.totalOrders ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-label">总交易额(元)</div>
          <div class="kpi-value nums-tabular">{{ formatAmount(summary.totalAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-label">总成交量(kg)</div>
          <div class="kpi-value nums-tabular">{{ summary.totalVolume ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-label">平均单价(元/kg)</div>
          <div class="kpi-value nums-tabular">{{ summary.avgUnitPrice ?? '-' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts 2x2 Grid -->
    <el-row :gutter="16">
      <el-col :span="12" style="margin-bottom:16px">
        <el-card shadow="never">
          <template #header><span>交易趋势</span></template>
          <div ref="trendChartRef" style="height:300px" v-loading="chartLoading.trend"></div>
        </el-card>
      </el-col>
      <el-col :span="12" style="margin-bottom:16px">
        <el-card shadow="never">
          <template #header><span>品种分布</span></template>
          <div ref="pieChartRef" style="height:300px" v-loading="chartLoading.pie"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>价格指数</span></template>
          <div ref="priceChartRef" style="height:300px" v-loading="chartLoading.price"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>月度成交量</span></template>
          <div ref="volumeChartRef" style="height:300px" v-loading="chartLoading.volume"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { tradeStatsApi } from '@/api/trade.js'
import * as echarts from 'echarts'

const BRAND_COLOR = '#D32F2F'

const filterVariety = ref('')
const summary = ref({})

const trendChartRef = ref()
const pieChartRef = ref()
const priceChartRef = ref()
const volumeChartRef = ref()

const chartLoading = reactive({ trend: false, pie: false, price: false, volume: false })

function formatAmount(val) {
  if (val == null) return '-'
  if (val >= 10000) return (val / 10000).toFixed(2) + ' 万'
  return val
}

async function loadSummary() {
  try {
    const res = await tradeStatsApi.summary()
    summary.value = res || {}
  } catch (e) {
    summary.value = {}
  }
}

async function loadTrendChart() {
  chartLoading.trend = true
  try {
    const res = await tradeStatsApi.trend({ months: 6 })
    const data = Array.isArray(res) ? res : (res?.records || res?.list || [])
    await nextTick()
    if (!trendChartRef.value) return
    const chart = echarts.init(trendChartRef.value)
    const months = data.map(d => d.month)
    const amounts = data.map(d => d.amount || 0)
    chart.setOption({
      color: [BRAND_COLOR],
      tooltip: { trigger: 'axis' },
      grid: { left: 56, right: 20, top: 24, bottom: 30 },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value', name: '元' },
      series: [{ name: '交易额', type: 'line', data: amounts, smooth: true, areaStyle: { opacity: 0.12 } }],
    })
  } catch (e) {
    console.error(e)
  } finally {
    chartLoading.trend = false
  }
}

async function loadPieChart() {
  chartLoading.pie = true
  try {
    const res = await tradeStatsApi.variety()
    const data = Array.isArray(res) ? res : (res?.records || res?.list || [])
    await nextTick()
    if (!pieChartRef.value) return
    const chart = echarts.init(pieChartRef.value)
    const pieData = data.map(d => ({ name: d.variety, value: d.volume }))
    chart.setOption({
      color: [BRAND_COLOR, '#EF5350', '#FF7043', '#FFAB40', '#8D6E63'],
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, type: 'scroll' },
      series: [{
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        data: pieData,
        label: { formatter: '{b}\n{d}%' },
      }],
    })
  } catch (e) {
    console.error(e)
  } finally {
    chartLoading.pie = false
  }
}

async function loadPriceChart() {
  if (!filterVariety.value) {
    if (priceChartRef.value) {
      const chart = echarts.getInstanceByDom(priceChartRef.value)
      if (chart) chart.clear()
    }
    return
  }
  chartLoading.price = true
  try {
    const res = await tradeStatsApi.priceIndex({ variety: filterVariety.value, days: 30 })
    const data = Array.isArray(res) ? res : (res?.records || res?.list || [])
    await nextTick()
    if (!priceChartRef.value) return
    const chart = echarts.init(priceChartRef.value)
    const months = data.map(d => d.month)
    const prices = data.map(d => d.amount || 0)
    chart.setOption({
      color: [BRAND_COLOR],
      tooltip: { trigger: 'axis' },
      grid: { left: 56, right: 20, top: 24, bottom: 30 },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value', name: '元/kg' },
      series: [{ name: '价格指数', type: 'line', data: prices, smooth: true, symbol: 'circle', symbolSize: 6 }],
    })
  } catch (e) {
    console.error(e)
  } finally {
    chartLoading.price = false
  }
}

async function loadVolumeChart() {
  chartLoading.volume = true
  try {
    const res = await tradeStatsApi.trend({ months: 6 })
    const data = Array.isArray(res) ? res : (res?.records || res?.list || [])
    await nextTick()
    if (!volumeChartRef.value) return
    const chart = echarts.init(volumeChartRef.value)
    const months = data.map(d => d.month)
    const volumes = data.map(d => d.volume || 0)
    chart.setOption({
      color: [BRAND_COLOR],
      tooltip: { trigger: 'axis' },
      grid: { left: 56, right: 20, top: 24, bottom: 30 },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value', name: 'kg' },
      series: [{ name: '成交量', type: 'bar', data: volumes, barMaxWidth: 40 }],
    })
  } catch (e) {
    console.error(e)
  } finally {
    chartLoading.volume = false
  }
}

async function loadAll() {
  await loadSummary()
  await Promise.all([loadTrendChart(), loadPieChart(), loadPriceChart(), loadVolumeChart()])
}

onMounted(loadAll)
</script>

<style scoped>
.kpi-card {
  text-align: center;
  border-top: 3px solid #D32F2F;
}
.kpi-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}
.kpi-value {
  font-size: 28px;
  font-weight: 700;
  color: #D32F2F;
}
</style>
