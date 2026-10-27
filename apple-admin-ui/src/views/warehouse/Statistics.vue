<template>
  <div class="page-container">
    <div class="page-header">
      <h2>仓储统计</h2>
    </div>

    <!-- KPI Cards -->
    <el-row :gutter="16" style="margin-bottom:24px" v-loading="summaryLoading">
      <el-col :span="6">
        <div class="kpi-card">
          <div class="kpi-label">仓库总数</div>
          <div class="kpi-value nums-tabular">{{ summary.totalWarehouses ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="kpi-card">
          <div class="kpi-label">总容量(吨)</div>
          <div class="kpi-value nums-tabular">{{ summary.totalCapacity ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="kpi-card">
          <div class="kpi-label">已用(吨)</div>
          <div class="kpi-value nums-tabular">{{ summary.totalUsed ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="kpi-card">
          <div class="kpi-label">利用率</div>
          <div class="kpi-value nums-tabular">
            {{ summary.utilizationRate != null ? summary.utilizationRate.toFixed(1) + '%' : '-' }}
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Charts Row -->
    <el-row :gutter="16">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">周转率概况</div>
          <div ref="turnoverChartRef" style="height:300px"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">损耗率概况</div>
          <div ref="lossChartRef" style="height:300px"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { warehouseStatsApi } from '@/api/warehouse.js'
import * as echarts from 'echarts'

const summaryLoading = ref(false)
const summary = ref({})

const turnoverChartRef = ref()
const lossChartRef = ref()

const BRAND_COLOR = '#D32F2F'

async function loadSummary() {
  summaryLoading.value = true
  try {
    const res = await warehouseStatsApi.summary()
    summary.value = res || {}
  } catch (e) {
    console.error(e)
  } finally {
    summaryLoading.value = false
  }
}

async function loadTurnoverChart() {
  try {
    const res = await warehouseStatsApi.turnover({ days: 7 })
    const data = res || {}
    await nextTick()
    if (!turnoverChartRef.value) return
    const chart = echarts.getInstanceByDom(turnoverChartRef.value) || echarts.init(turnoverChartRef.value)
    chart.setOption({
      color: [BRAND_COLOR],
      tooltip: { trigger: 'axis' },
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: ['入库总量', '出库总量', '记录数'] },
      yAxis: { type: 'value', name: '数量' },
      series: [{
        type: 'bar',
        data: [data.totalInbound ?? 0, data.totalOutbound ?? 0, data.recordCount ?? 0],
        itemStyle: { color: BRAND_COLOR },
        barMaxWidth: 40
      }]
    })
  } catch (e) {
    console.error(e)
  }
}

async function loadLossChart() {
  try {
    const res = await warehouseStatsApi.loss({ days: 7 })
    const data = res || {}
    await nextTick()
    if (!lossChartRef.value) return
    const chart = echarts.getInstanceByDom(lossChartRef.value) || echarts.init(lossChartRef.value)
    chart.setOption({
      color: [BRAND_COLOR],
      tooltip: { trigger: 'axis' },
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: ['入库总量', '出库总量', '差异', '损耗率'] },
      yAxis: { type: 'value', name: '数量/比率' },
      series: [{
        type: 'bar',
        data: [
          { value: data.totalInbound ?? 0, name: '入库总量' },
          { value: data.totalOutbound ?? 0, name: '出库总量' },
          { value: data.difference ?? 0, name: '差异' },
          { value: data.lossRate ?? 0, name: '损耗率' },
        ],
        itemStyle: { color: BRAND_COLOR },
        barMaxWidth: 40
      }]
    })
  } catch (e) {
    console.error(e)
  }
}

onMounted(async () => {
  await loadSummary()
  await nextTick()
  loadTurnoverChart()
  loadLossChart()
})
</script>

<style scoped>
.kpi-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 20px 24px;
  text-align: center;
}
.kpi-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.kpi-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}
.chart-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px 20px;
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}
</style>
