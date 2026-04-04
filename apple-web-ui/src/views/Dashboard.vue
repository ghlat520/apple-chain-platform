<template>
  <div class="dashboard">
    <van-pull-refresh v-model="refreshing" @refresh="loadDashboard">

      <!-- KPI Cards -->
      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-icon" style="background:#e8f5e9">
            <van-icon name="farm-o" color="#4caf50" size="24" />
          </div>
          <div class="kpi-content">
            <p class="kpi-value">{{ summary.totalOrchards ?? '--' }}</p>
            <p class="kpi-label">果园总数</p>
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon" style="background:#e3f2fd">
            <van-icon name="leaf" color="#2196f3" size="24" />
          </div>
          <div class="kpi-content">
            <p class="kpi-value">{{ summary.totalBatches ?? '--' }}</p>
            <p class="kpi-label">种植批次</p>
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon" style="background:#fff3e0">
            <van-icon name="orders-o" color="#ff9800" size="24" />
          </div>
          <div class="kpi-content">
            <p class="kpi-value">{{ summary.totalOrders ?? '--' }}</p>
            <p class="kpi-label">交易订单</p>
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon" style="background:#fce4ec">
            <van-icon name="search" color="#e91e63" size="24" />
          </div>
          <div class="kpi-content">
            <p class="kpi-value">{{ summary.completedOrders ?? '--' }}</p>
            <p class="kpi-label">已完成订单</p>
          </div>
        </div>
      </div>

      <!-- Agricultural Input KPI Cards -->
      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-icon" style="background:#f3e5f5">
            <van-icon name="label-o" color="#9c27b0" size="24" />
          </div>
          <div class="kpi-content">
            <p class="kpi-value">{{ summary.totalProducts ?? '--' }}</p>
            <p class="kpi-label">农资产品</p>
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon" style="background:#ffebee">
            <van-icon name="warning-o" color="#f44336" size="24" />
          </div>
          <div class="kpi-content">
            <p class="kpi-value">{{ summary.lowInventory ?? '--' }}</p>
            <p class="kpi-label">库存预警</p>
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon" style="background:#e0f2f1">
            <van-icon name="todo-list-o" color="#009688" size="24" />
          </div>
          <div class="kpi-content">
            <p class="kpi-value">{{ summary.totalUsages ?? '--' }}</p>
            <p class="kpi-label">使用记录</p>
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon" style="background:#fff8e1">
            <van-icon name="shop-o" color="#ff8f00" size="24" />
          </div>
          <div class="kpi-content">
            <p class="kpi-value">{{ summary.totalWarehouses ?? '--' }}</p>
            <p class="kpi-label">仓库总数</p>
          </div>
        </div>
      </div>

      <!-- Trend Chart -->
      <div class="chart-card">
        <h3 class="chart-title">订单与溯源趋势</h3>
        <div ref="trendChartRef" class="chart-container"></div>
      </div>

      <!-- Top Varieties Chart -->
      <div class="chart-card">
        <h3 class="chart-title">品种分布 Top 5</h3>
        <div ref="varietyChartRef" class="chart-container"></div>
      </div>

      <!-- Pending Actions -->
      <div class="section-card">
        <h3 class="section-title">待处理事项</h3>
        <van-cell-group inset>
          <van-cell
            title="待处理订单"
            :value="pending.pendingOrders ?? '--'"
            is-link
            @click="$router.push('/trade/orders?status=pending')"
          >
            <template #icon>
              <van-icon name="orders-o" color="#ff9800" style="margin-right:8px" />
            </template>
          </van-cell>
          <van-cell
            title="待处理批次"
            :value="pending.draftBatches ?? '--'"
            is-link
            @click="$router.push('/cultivation/batches')"
          >
            <template #icon>
              <van-icon name="leaf" color="#4caf50" style="margin-right:8px" />
            </template>
          </van-cell>
        </van-cell-group>
      </div>

      <!-- Inventory Warnings -->
      <div class="section-card">
        <h3 class="section-title">库存预警 <van-tag type="danger" v-if="alertList.length">{{ alertList.length }}</van-tag></h3>
        <van-cell-group inset v-if="alertList.length">
          <van-cell
            v-for="alert in alertList"
            :key="alert.id"
            :title="alert.productName"
            is-link
            @click="$router.push('/input/inventory')"
          >
            <template #label>
              库存: {{ alert.stockQuantity }}{{ alert.unit }} · 预警线: {{ alert.warningLevel }}{{ alert.unit }}
            </template>
            <template #value>
              <van-tag :type="alert.status === 'EMPTY' ? 'danger' : 'warning'">
                {{ alert.status === 'EMPTY' ? '缺货' : '偏低' }}
              </van-tag>
            </template>
          </van-cell>
        </van-cell-group>
        <van-cell-group inset v-else>
          <van-cell title="库存正常" value="无预警" />
        </van-cell-group>
      </div>

      <!-- Quick Actions -->
      <div class="section-card">
        <h3 class="section-title">快速操作</h3>
        <div class="quick-actions">
          <van-button plain type="primary" size="small" @click="$router.push('/farm/orchards')">果园管理</van-button>
          <van-button plain type="success" size="small" @click="$router.push('/cultivation/batches')">种植批次</van-button>
          <van-button plain type="warning" size="small" @click="$router.push('/trace/query')">溯源查询</van-button>
          <van-button plain type="danger" size="small" @click="$router.push('/trade/supply')">供货信息</van-button>
          <van-button plain size="small" color="#9c27b0" @click="$router.push('/input/products')">农资产品</van-button>
          <van-button plain size="small" color="#ff8f00" @click="$router.push('/warehouse/list')">仓库管理</van-button>
        </div>
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { statsApi } from '@/api/stats.js'
import { inputApi } from '@/api/input.js'
import * as echarts from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, GridComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, BarChart, TitleComponent, TooltipComponent, GridComponent, LegendComponent, CanvasRenderer])

const refreshing = ref(false)
const summary = ref({})
const pending = ref({})
const trendData = ref([])
const varietyData = ref([])
const alertList = ref([])

const trendChartRef = ref(null)
const varietyChartRef = ref(null)
let trendChart = null
let varietyChart = null

async function loadDashboard() {
  try {
    const [sum, trend, topVarieties, pend, alerts] = await Promise.allSettled([
      statsApi.getSummary(),
      statsApi.getTrend(),
      statsApi.getTopVarieties(),
      statsApi.getPending(),
      inputApi.getInventoryAlerts()
    ])
    summary.value = sum.status === 'fulfilled' ? (sum.value || {}) : {}
    trendData.value = trend.status === 'fulfilled' ? (trend.value || []) : []
    varietyData.value = topVarieties.status === 'fulfilled' ? (topVarieties.value || []) : []
    pending.value = pend.status === 'fulfilled' ? (pend.value || {}) : {}
    alertList.value = alerts.status === 'fulfilled' ? (alerts.value || []) : []

    await nextTick()
    renderTrendChart()
    renderVarietyChart()
  } catch (e) {
    // errors shown by interceptor
  } finally {
    refreshing.value = false
  }
}

function renderTrendChart() {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  const months = trendData.value.map((d) => d.month || d.ym || d.YM)
  const orders = trendData.value.map((d) => d.orderCount || d.ORDERCOUNT)
  const amounts = trendData.value.map((d) => Number(d.totalAmount || d.TOTALAMOUNT || 0).toFixed(0))
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '交易额(元)'], bottom: 0 },
    grid: { left: 40, right: 16, top: 16, bottom: 36 },
    xAxis: { type: 'category', data: months, axisLabel: { fontSize: 10 } },
    yAxis: { type: 'value', axisLabel: { fontSize: 10 } },
    series: [
      { name: '订单数', type: 'line', data: orders, smooth: true, itemStyle: { color: '#1989fa' } },
      { name: '交易额(元)', type: 'line', data: amounts, smooth: true, itemStyle: { color: '#07c160' } }
    ]
  })
}

function renderVarietyChart() {
  if (!varietyChartRef.value) return
  if (!varietyChart) {
    varietyChart = echarts.init(varietyChartRef.value)
  }
  const varieties = varietyData.value.slice(0, 5)
  varietyChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 80, right: 16, top: 16, bottom: 36 },
    xAxis: { type: 'value', axisLabel: { fontSize: 10 } },
    yAxis: {
      type: 'category',
      data: varieties.map((d) => d.variety || d.VARIETY),
      axisLabel: { fontSize: 10 }
    },
    series: [
      {
        type: 'bar',
        data: varieties.map((d) => d.orderCount || d.ORDERCOUNT),
        itemStyle: { color: '#07c160', borderRadius: [0, 4, 4, 0] }
      }
    ]
  })
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  varietyChart?.dispose()
})

function handleResize() {
  trendChart?.resize()
  varietyChart?.resize()
}
</script>

<style scoped>
.dashboard {
  padding: 12px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 12px;
}

.kpi-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.kpi-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.kpi-value {
  font-size: 22px;
  font-weight: 700;
  color: #323233;
  line-height: 1.2;
}

.kpi-label {
  font-size: 12px;
  color: #969799;
  margin-top: 2px;
}

.chart-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 12px;
}

.chart-container {
  height: 200px;
  width: 100%;
}

.section-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 0;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #323233;
  padding: 0 16px 12px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 16px;
}
</style>
