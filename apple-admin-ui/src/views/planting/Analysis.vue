<template>
  <div class="page-container">
    <div class="page-header">
      <h2>种植分析</h2>
    </div>
    <div class="filter-bar">
      <el-select v-model="filter.year" placeholder="年份" clearable style="width:120px" @change="loadAll">
        <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
      </el-select>
      <el-select v-model="filter.variety" placeholder="品种" clearable style="width:140px" @change="loadAll">
        <el-option label="红富士" value="红富士" />
        <el-option label="嘎拉" value="嘎拉" />
        <el-option label="黄元帅" value="黄元帅" />
      </el-select>
      <el-button type="primary" @click="loadAll" icon="Search" :loading="loading">查询</el-button>
    </div>

    <div class="chart-grid">
      <!-- 亩产排名 -->
      <div class="chart-card">
        <div class="chart-card-title">亩产排名</div>
        <div ref="yieldChartRef" class="chart-body"></div>
      </div>

      <!-- 优果率趋势 -->
      <div class="chart-card">
        <div class="chart-card-title">优果率趋势</div>
        <div ref="premiumChartRef" class="chart-body"></div>
      </div>

      <!-- 病虫害发生率 -->
      <div class="chart-card">
        <div class="chart-card-title">病虫害发生率</div>
        <div ref="pestChartRef" class="chart-body"></div>
      </div>

      <!-- 地块对比 -->
      <div class="chart-card">
        <div class="chart-card-title">地块对比（需选择果园）</div>
        <div style="margin-bottom:8px">
          <el-select v-model="selectedOrchardIds" multiple placeholder="选择果园进行对比" style="width:100%" @change="loadCompare">
            <el-option v-for="o in orchardList" :key="o.id" :label="o.orchardName" :value="o.id" />
          </el-select>
        </div>
        <div ref="compareChartRef" class="chart-body"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { plantingAnalysisApi, orchardApi } from '@/api/planting.js'

const BRAND = '#D32F2F'
const loading = ref(false)
const currentYear = new Date().getFullYear()
const years = [currentYear, currentYear - 1, currentYear - 2, currentYear - 3]
const filter = ref({ year: currentYear, variety: '' })

const yieldChartRef = ref()
const premiumChartRef = ref()
const pestChartRef = ref()
const compareChartRef = ref()

let yieldChart = null
let premiumChart = null
let pestChart = null
let compareChart = null

const orchardList = ref([])
const selectedOrchardIds = ref([])

function getParams() {
  const p = {}
  if (filter.value.year) p.year = filter.value.year
  if (filter.value.variety) p.variety = filter.value.variety
  return p
}

async function loadOrchards() {
  try {
    const res = await orchardApi.list({ page: 1, size: 200 })
    orchardList.value = res?.records || res?.list || []
  } catch (e) { console.error(e) }
}

async function loadAll() {
  loading.value = true
  const params = getParams()
  try {
    const [yieldData, premiumData, pestData] = await Promise.allSettled([
      plantingAnalysisApi.yield(params),
      plantingAnalysisApi.premium(params),
      plantingAnalysisApi.pest({ year: params.year })
    ])
    await nextTick()
    renderYield(yieldData.status === 'fulfilled' ? yieldData.value : null)
    renderPremium(premiumData.status === 'fulfilled' ? premiumData.value : null)
    renderPest(pestData.status === 'fulfilled' ? pestData.value : null)
  } finally {
    loading.value = false
  }
}

async function loadCompare() {
  if (selectedOrchardIds.value.length < 2) {
    if (compareChart) compareChart.clear()
    return
  }
  try {
    const params = { orchardIds: selectedOrchardIds.value.join(',') }
    const res = await plantingAnalysisApi.compare(params)
    const data = Array.isArray(res) ? res : []
    await nextTick()
    renderCompare(data)
  } catch (e) {
    console.error(e)
  }
}

function renderYield(data) {
  if (!yieldChartRef.value) return
  if (!yieldChart) yieldChart = echarts.init(yieldChartRef.value)
  const list = Array.isArray(data) ? data : []
  yieldChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 100, right: 30, top: 20, bottom: 30 },
    xAxis: { type: 'value', name: 'kg/亩' },
    yAxis: { type: 'category', data: list.map(d => d.orchardName || '未知').reverse(), axisLabel: { fontSize: 12 } },
    series: [{
      type: 'bar',
      data: list.map(d => d.yieldPerMu || 0).reverse(),
      itemStyle: { color: BRAND, borderRadius: [0, 4, 4, 0] },
      label: { show: true, position: 'right', formatter: '{c} kg' }
    }]
  }, true)
}

function renderPremium(data) {
  if (!premiumChartRef.value) return
  if (!premiumChart) premiumChart = echarts.init(premiumChartRef.value)
  const list = Array.isArray(data) ? data : []
  const orchards = list.map(d => d.orchardName || '未知')
  const rates = list.map(d => d.premiumRate != null ? (d.premiumRate * 100).toFixed(1) : 0)
  premiumChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: orchards, axisLabel: { rotate: orchards.length > 5 ? 15 : 0 } },
    yAxis: { type: 'value', name: '%', min: 0, max: 100 },
    series: [{
      type: 'bar',
      data: rates,
      itemStyle: { color: '#43A047', borderRadius: [4, 4, 0, 0] },
      label: { show: true, position: 'top', formatter: '{c}%' }
    }]
  }, true)
}

function renderPest(data) {
  if (!pestChartRef.value) return
  if (!pestChart) pestChart = echarts.init(pestChartRef.value)
  const list = Array.isArray(data) ? data : []
  const orchards = list.map(d => d.orchardName || '未知')
  const rates = list.map(d => d.incidenceRate != null ? (d.incidenceRate * 100).toFixed(1) : 0)
  pestChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 60, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: orchards, axisLabel: { rotate: orchards.length > 5 ? 15 : 0 } },
    yAxis: { type: 'value', name: '%' },
    series: [{
      type: 'bar',
      data: rates,
      itemStyle: { color: '#F57C00', borderRadius: [4, 4, 0, 0] },
      label: { show: true, position: 'top', formatter: '{c}%' }
    }]
  }, true)
}

function renderCompare(list) {
  if (!compareChartRef.value) return
  if (!compareChart) compareChart = echarts.init(compareChartRef.value)
  if (!list.length) { compareChart.clear(); return }

  // Build radar from actual data
  const indicators = [
    { name: '亩产(kg/亩)', max: 500 },
    { name: '优果率(%)', max: 100 },
    { name: '病虫害控制(%)', max: 100 }
  ]

  const seriesData = list.map((item, i) => {
    const y = item.yieldMetrics || {}
    const p = item.premiumMetrics || {}
    const pest = item.pestMetrics || {}
    return {
      name: item.orchardName || `果园${i + 1}`,
      value: [
        y.yieldPerMu || 0,
        p.premiumRate != null ? (p.premiumRate * 100).toFixed(1) : 0,
        pest.incidenceRate != null ? (100 - pest.incidenceRate * 100).toFixed(1) : 0
      ]
    }
  })

  compareChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, data: seriesData.map(s => s.name) },
    radar: { indicator: indicators, radius: '60%' },
    series: [{
      type: 'radar',
      data: seriesData.map((s, i) => ({
        name: s.name,
        value: s.value,
        itemStyle: { color: i === 0 ? BRAND : '#1565C0' },
        lineStyle: { color: i === 0 ? BRAND : '#1565C0' },
        areaStyle: { color: i === 0 ? 'rgba(211,47,47,0.1)' : 'rgba(21,101,192,0.1)' }
      }))
    }]
  }, true)
}

function handleResize() {
  yieldChart?.resize()
  premiumChart?.resize()
  pestChart?.resize()
  compareChart?.resize()
}

onMounted(() => {
  loadOrchards()
  loadAll()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  yieldChart?.dispose()
  premiumChart?.dispose()
  pestChart?.dispose()
  compareChart?.dispose()
})
</script>

<style scoped>
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 16px;
}
.chart-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 16px;
}
.chart-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}
.chart-body {
  height: 280px;
  width: 100%;
}
</style>
