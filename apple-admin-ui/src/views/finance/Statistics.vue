<template>
  <div class="page-container">
    <div class="page-header">
      <h2>金融统计</h2>
    </div>

    <!-- KPI Cards -->
    <el-row :gutter="16" style="margin-bottom:24px" v-loading="summaryLoading">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">贷款笔数</div>
          <div class="stat-value nums-tabular">{{ summary.totalLoans ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">贷款总额(元)</div>
          <div class="stat-value nums-tabular">{{ summary.totalAmount != null ? summary.totalAmount.toLocaleString() : '-' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card--warning">
          <div class="stat-label">逾期率</div>
          <div class="stat-value nums-tabular">{{ summary.overdueRate != null ? (summary.overdueRate * 100).toFixed(1) + '%' : '-' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">信用分布</div>
          <div class="stat-value nums-tabular" style="font-size:16px">{{ creditSummaryText }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- Risk Distribution Pie Chart -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header>
        <span>信用等级分布</span>
      </template>
      <div ref="riskChartRef" style="height:380px" v-loading="riskLoading"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { financeStatsApi } from '@/api/finance.js'
import * as echarts from 'echarts'

const summaryLoading = ref(false)
const riskLoading = ref(false)
const summary = ref({})
const riskData = ref({})
const riskChartRef = ref()

const creditSummaryText = computed(() => {
  const levels = riskData.value?.riskByLevel || {}
  const entries = Object.entries(levels)
  if (entries.length === 0) return '-'
  return entries.map(([k, v]) => `${k}:${v}`).join(' / ')
})

async function loadSummary() {
  summaryLoading.value = true
  try {
    const res = await financeStatsApi.summary()
    summary.value = res || {}
  } catch (e) {
    console.error(e)
  } finally {
    summaryLoading.value = false
  }
}

async function loadRiskChart() {
  riskLoading.value = true
  try {
    const res = await financeStatsApi.risk()
    const data = res || {}
    riskData.value = data
    const levels = data.riskByLevel || {}
    await nextTick()
    if (!riskChartRef.value) return
    const chart = echarts.init(riskChartRef.value)
    const pieData = Object.entries(levels).map(([k, v]) => ({ value: v, name: k + '级' }))
    chart.setOption({
      color: ['#C62828', '#E53935', '#F57C00', '#FFA726', '#43A047', '#66BB6A'],
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} 笔 ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: '5%',
        top: 'center',
        data: pieData.map(d => d.name)
      },
      series: [
        {
          name: '信用等级分布',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['40%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 2,
          },
          label: {
            show: true,
            formatter: '{b}\n{c} 笔'
          },
          emphasis: {
            label: { show: true, fontSize: 14, fontWeight: 'bold' }
          },
          data: pieData
        }
      ]
    })
    window.addEventListener('resize', () => chart.resize())
  } catch (e) {
    console.error(e)
  } finally {
    riskLoading.value = false
  }
}

onMounted(async () => {
  await loadSummary()
  await loadRiskChart()
})
</script>

<style scoped>
.stat-card {
  background: #fff;
  border: 1px solid var(--el-border-color-light, #ebeef5);
  border-radius: 8px;
  padding: 20px 24px;
  text-align: center;
}

.stat-card--warning {
  border-color: #F57C00;
}

.stat-card--danger {
  border-color: #C62828;
}

.stat-label {
  font-size: 14px;
  color: var(--el-text-color-secondary, #909399);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #D32F2F;
}
</style>
