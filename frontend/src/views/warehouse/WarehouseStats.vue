<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">仓储统计</span>
    </div>

    <!-- 概览卡片 -->
    <div class="stat-cards" v-if="summary">
      <div class="stat-card">
        <div class="stat-value">{{ summary.totalWarehouses || 0 }}</div>
        <div class="stat-label">仓库总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ summary.totalCapacity || 0 }}<span class="stat-unit">吨</span></div>
        <div class="stat-label">总容量</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ summary.usedCapacity || 0 }}<span class="stat-unit">吨</span></div>
        <div class="stat-label">已用容量</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ usagePercent }}<span class="stat-unit">%</span></div>
        <div class="stat-label">利用率</div>
      </div>
    </div>

    <van-loading v-if="chartsLoading" style="display:block;text-align:center;padding:20px;">加载中...</van-loading>

    <template v-else>
      <!-- 按仓库类型分布 - 环形图 -->
      <van-cell-group inset style="margin-bottom:12px;">
        <van-cell title="仓库类型分布" />
        <div ref="typeChartRef" style="height:280px;"></div>
      </van-cell-group>

      <!-- 按状态分布 - 柱状图 -->
      <van-cell-group inset style="margin-bottom:12px;">
        <van-cell title="仓库状态分布" />
        <div ref="statusChartRef" style="height:280px;"></div>
      </van-cell-group>

      <!-- 周转率统计 -->
      <van-cell-group inset style="margin-bottom:12px;">
        <van-cell title="周转率统计">
          <template #value>
            <div style="display:flex;gap:8px;">
              <van-dropdown-menu style="flex:1;">
                <van-dropdown-item v-model="turnoverWarehouseId" :options="warehouseFilterOptions" @change="loadTurnover" />
              </van-dropdown-menu>
            </div>
          </template>
        </van-cell>
        <div style="padding:0 16px 8px;">
          <van-button-group>
            <van-button
              v-for="d in [7, 30, 90]" :key="d"
              size="small"
              :type="turnoverDays === d ? 'primary' : 'default'"
              @click="turnoverDays = d; loadTurnover()"
            >{{ d }}天</van-button>
          </van-button-group>
        </div>
        <div ref="turnoverChartRef" style="height:250px;"></div>
      </van-cell-group>

      <!-- 损耗率统计 -->
      <van-cell-group inset style="margin-bottom:12px;">
        <van-cell title="损耗率统计">
          <template #value>
            <van-dropdown-menu style="flex:1;">
              <van-dropdown-item v-model="lossWarehouseId" :options="warehouseFilterOptions" @change="loadLoss" />
            </van-dropdown-menu>
          </template>
        </van-cell>
        <div style="padding:0 16px 8px;">
          <van-button-group>
            <van-button
              v-for="d in [7, 30, 90]" :key="d"
              size="small"
              :type="lossDays === d ? 'primary' : 'default'"
              @click="lossDays = d; loadLoss()"
            >{{ d }}天</van-button>
          </van-button-group>
        </div>
        <div ref="lossChartRef" style="height:250px;"></div>
      </van-cell-group>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { warehouseApi } from '@/api/warehouse'

const summary = ref(null)
const chartsLoading = ref(true)

// Chart DOM refs
const typeChartRef = ref(null)
const statusChartRef = ref(null)
const turnoverChartRef = ref(null)
const lossChartRef = ref(null)

// Chart instances
let typeChart = null
let statusChart = null
let turnoverChart = null
let lossChart = null

// Filter state
const turnoverWarehouseId = ref('')
const turnoverDays = ref(30)
const lossWarehouseId = ref('')
const lossDays = ref(30)
const warehouseFilterOptions = ref([{ text: '全部仓库', value: '' }])

const usagePercent = computed(() => {
  if (!summary.value || !summary.value.totalCapacity) return '0'
  return ((summary.value.usedCapacity / summary.value.totalCapacity) * 100).toFixed(1)
})

async function loadSummary() {
  try {
    const res = await warehouseApi.statisticsSummary()
    summary.value = res.data || {}
  } catch {
    summary.value = {}
  }
}

async function loadWarehouseFilters() {
  try {
    const res = await warehouseApi.list({ page: 1, size: 200 })
    const records = res.data?.records || []
    warehouseFilterOptions.value = [
      { text: '全部仓库', value: '' },
      ...records.map(w => ({ text: w.name, value: String(w.id) }))
    ]
  } catch {
    // keep default
  }
}

function initTypeChart() {
  if (!typeChartRef.value) return
  typeChart = echarts.init(typeChartRef.value)

  const typeData = summary.value?.typeDistribution || {}
  const typeNameMap = { NORMAL: '普通仓', COLD: '冷藏仓', ATMOSPHERE: '气调仓' }
  const data = Object.entries(typeData).map(([key, value]) => ({
    name: typeNameMap[key] || key,
    value
  }))

  typeChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' },
      data
    }]
  })
}

function initStatusChart() {
  if (!statusChartRef.value) return
  statusChart = echarts.init(statusChartRef.value)

  const statusData = summary.value?.statusDistribution || {}
  const statusNameMap = { ACTIVE: '运营中', MAINTENANCE: '维护中', CLOSED: '已关闭' }
  const statusColorMap = { ACTIVE: '#07c160', MAINTENANCE: '#ff976a', CLOSED: '#969799' }
  const categories = Object.keys(statusData).map(k => statusNameMap[k] || k)
  const values = Object.values(statusData)
  const colors = Object.keys(statusData).map(k => statusColorMap[k] || '#1989fa')

  statusChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: categories },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      data: values.map((v, i) => ({ value: v, itemStyle: { color: colors[i] } })),
      barWidth: '40%',
      itemStyle: { borderRadius: [4, 4, 0, 0] }
    }]
  })
}

async function loadTurnover() {
  if (!turnoverChartRef.value) return
  if (!turnoverChart) {
    turnoverChart = echarts.init(turnoverChartRef.value)
  }

  try {
    const params = { days: turnoverDays.value }
    if (turnoverWarehouseId.value) params.warehouseId = turnoverWarehouseId.value
    const res = await warehouseApi.statisticsTurnover(params)
    const data = res.data || {}

    const dates = data.dates || []
    const rates = data.rates || []

    turnoverChart.setOption({
      tooltip: { trigger: 'axis', formatter: '{b}<br/>周转率: {c}' },
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: dates, axisLabel: { rotate: 30, fontSize: 10 } },
      yAxis: { type: 'value', name: '次' },
      series: [{
        type: 'line',
        data: rates,
        smooth: true,
        areaStyle: { color: 'rgba(25,137,250,0.15)' },
        lineStyle: { color: '#1989fa' },
        itemStyle: { color: '#1989fa' }
      }]
    })
  } catch {
    turnoverChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } },
      xAxis: { show: false },
      yAxis: { show: false },
      series: []
    })
  }
}

async function loadLoss() {
  if (!lossChartRef.value) return
  if (!lossChart) {
    lossChart = echarts.init(lossChartRef.value)
  }

  try {
    const params = { days: lossDays.value }
    if (lossWarehouseId.value) params.warehouseId = lossWarehouseId.value
    const res = await warehouseApi.statisticsLoss(params)
    const data = res.data || {}

    const dates = data.dates || []
    const rates = data.rates || []

    lossChart.setOption({
      tooltip: { trigger: 'axis', formatter: '{b}<br/>损耗率: {c}%' },
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: dates, axisLabel: { rotate: 30, fontSize: 10 } },
      yAxis: { type: 'value', name: '%' },
      series: [{
        type: 'bar',
        data: rates,
        itemStyle: { color: '#ee0a24', borderRadius: [4, 4, 0, 0] },
        barWidth: '40%'
      }]
    })
  } catch {
    lossChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } },
      xAxis: { show: false },
      yAxis: { show: false },
      series: []
    })
  }
}

onMounted(async () => {
  try {
    await loadSummary()
    await loadWarehouseFilters()
    chartsLoading.value = false

    await nextTick()

    initTypeChart()
    initStatusChart()
    loadTurnover()
    loadLoss()

    window.addEventListener('resize', handleResize)
  } catch {
    chartsLoading.value = false
  }
})

function handleResize() {
  typeChart?.resize()
  statusChart?.resize()
  turnoverChart?.resize()
  lossChart?.resize()
}

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  typeChart?.dispose()
  statusChart?.dispose()
  turnoverChart?.dispose()
  lossChart?.dispose()
})
</script>

<style scoped>
.stat-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 0 16px;
  margin-bottom: 16px;
}
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #323233;
}
.stat-unit {
  font-size: 12px;
  font-weight: 400;
  color: #969799;
  margin-left: 2px;
}
.stat-label {
  font-size: 12px;
  color: #969799;
  margin-top: 4px;
}
</style>
