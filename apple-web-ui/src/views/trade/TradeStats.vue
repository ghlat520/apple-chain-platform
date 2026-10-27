<template>
  <div class="trade-stats-page">
    <!-- KPI 卡片组 -->
    <section class="kpi-section">
      <div class="kpi-card">
        <span class="kpi-label">总订单数</span>
        <span class="kpi-value nums-tabular">{{ formatNum(summary.totalOrders) }}</span>
        <span class="kpi-unit">笔</span>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">总交易量</span>
        <span class="kpi-value nums-tabular">{{ formatWeight(summary.totalVolume) }}</span>
        <span class="kpi-unit">kg</span>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">总金额</span>
        <span class="kpi-value nums-tabular">{{ formatAmount(summary.totalAmount) }}</span>
        <span class="kpi-unit">元</span>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">均价</span>
        <span class="kpi-value nums-tabular">{{ formatPrice(summary.avgUnitPrice) }}</span>
        <span class="kpi-unit">元/kg</span>
      </div>
    </section>

    <!-- 订单状态分布 -->
    <section class="section-block">
      <h3 class="section-title">订单状态分布</h3>
      <van-cell-group inset>
        <van-cell
          v-for="(count, status) in summary.ordersByStatus"
          :key="status"
          :title="statusLabelMap[status] || status"
          :value="count + ' 笔'"
          :value-class="'nums-tabular'"
        />
        <van-cell v-if="!summary.ordersByStatus || Object.keys(summary.ordersByStatus).length === 0" title="暂无数据" />
      </van-cell-group>
    </section>

    <!-- 品种维度柱状图 -->
    <section class="section-block">
      <h3 class="section-title">品种交易统计</h3>
      <div ref="varietyChartRef" class="chart-container" />
      <div v-if="varietyLoading" class="chart-loading">
        <van-loading type="spinner" size="24px">加载中</van-loading>
      </div>
      <van-empty v-if="!varietyLoading && varietyData.length === 0" description="暂无品种数据" />
    </section>

    <!-- 月度趋势折线图 -->
    <section class="section-block">
      <h3 class="section-title">月度趋势</h3>
      <div class="trend-header">
        <van-button
          v-for="m in [3, 6, 12]"
          :key="m"
          size="small"
          :type="trendMonths === m ? 'primary' : 'default'"
          @click="trendMonths = m; loadTrend()"
        >
          {{ m }}个月
        </van-button>
      </div>
      <div ref="trendChartRef" class="chart-container" />
      <div v-if="trendLoading" class="chart-loading">
        <van-loading type="spinner" size="24px">加载中</van-loading>
      </div>
      <van-empty v-if="!trendLoading && trendData.length === 0" description="暂无趋势数据" />
    </section>

    <!-- 价格指数查询 -->
    <section class="section-block">
      <h3 class="section-title">品种价格走势</h3>
      <van-cell-group inset class="price-form">
        <van-field
          v-model="priceQuery.variety"
          label="品种名称"
          placeholder="如: 红富士"
          clearable
        />
        <van-field
          v-model.number="priceQuery.days"
          label="天数"
          type="number"
          placeholder="默认30天"
        />
        <van-cell>
          <van-button
            type="primary"
            size="small"
            block
            :disabled="!priceQuery.variety"
            @click="loadPriceIndex"
          >
            查询价格走势
          </van-button>
        </van-cell>
      </van-cell-group>
      <div ref="priceChartRef" class="chart-container" />
      <div v-if="priceLoading" class="chart-loading">
        <van-loading type="spinner" size="24px">加载中</van-loading>
      </div>
      <van-empty v-if="!priceLoading && priceData.length === 0 && priceQueried" description="暂无价格数据" />
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { tradeApi } from '@/api/trade'
import { tokens } from '@/design/tokens'
import * as echarts from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  BarChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  CanvasRenderer,
])

const chartSeries = tokens.color.chartSeries

// ─── Data ────────────────────────────────────────────
const summary = ref({
  totalOrders: 0,
  totalVolume: 0,
  totalAmount: 0,
  avgUnitPrice: 0,
  ordersByStatus: {},
  varietyBreakdown: [],
  monthlyTrend: [],
})

const varietyData = ref([])
const varietyLoading = ref(false)

const trendData = ref([])
const trendMonths = ref(6)
const trendLoading = ref(false)

const priceData = ref([])
const priceLoading = ref(false)
const priceQueried = ref(false)
const priceQuery = ref({
  variety: '',
  days: 30,
})

// ─── Chart refs ──────────────────────────────────────
const varietyChartRef = ref(null)
const trendChartRef = ref(null)
const priceChartRef = ref(null)

let varietyChart = null
let trendChart = null
let priceChart = null

// ─── Status label mapping ────────────────────────────
const statusLabelMap = {
  PENDING: '待确认',
  CONFIRMED: '已确认',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

// ─── Formatters ──────────────────────────────────────
function formatNum(val) {
  if (val == null) return '0'
  return Number(val).toLocaleString('zh-CN')
}

function formatWeight(val) {
  if (val == null) return '0'
  return Number(val).toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

function formatAmount(val) {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatPrice(val) {
  if (val == null) return '0.00'
  return Number(val).toFixed(2)
}

// ─── API Calls ───────────────────────────────────────
async function loadSummary() {
  try {
    const res = await tradeApi.getTradeSummary()
    if (res?.data) {
      summary.value = res.data
    }
  } catch {
    // handled silently, page shows defaults
  }
}

async function loadVariety() {
  varietyLoading.value = true
  try {
    const res = await tradeApi.getTradeVariety()
    if (res?.data) {
      varietyData.value = res.data
    }
  } catch {
    // handled silently
  } finally {
    varietyLoading.value = false
    await nextTick()
    renderVarietyChart()
  }
}

async function loadTrend() {
  trendLoading.value = true
  try {
    const res = await tradeApi.getTradeTrend(trendMonths.value)
    if (res?.data) {
      trendData.value = res.data
    }
  } catch {
    // handled silently
  } finally {
    trendLoading.value = false
    await nextTick()
    renderTrendChart()
  }
}

async function loadPriceIndex() {
  priceLoading.value = true
  priceQueried.value = true
  try {
    const res = await tradeApi.getTradePriceIndex(priceQuery.value.variety, priceQuery.value.days || 30)
    if (res?.data) {
      priceData.value = res.data
    }
  } catch {
    // handled silently
  } finally {
    priceLoading.value = false
    await nextTick()
    renderPriceChart()
  }
}

// ─── Chart Renderers ─────────────────────────────────
function baseChartOption() {
  return {
    color: chartSeries,
    tooltip: {
      trigger: 'axis',
      confine: true,
      textStyle: {
        fontFamily: 'var(--font-body)',
        fontSize: 13,
      },
    },
    legend: {
      bottom: 0,
      textStyle: { fontSize: 12 },
    },
    grid: {
      left: '12%',
      right: '8%',
      top: '14%',
      bottom: '18%',
      containLabel: true,
    },
  }
}

function renderVarietyChart() {
  if (!varietyChartRef.value) return
  if (!varietyChart) {
    varietyChart = echarts.init(varietyChartRef.value)
  }

  const names = varietyData.value.map(d => d.variety)
  const volumes = varietyData.value.map(d => d.volume)
  const amounts = varietyData.value.map(d => d.amount)

  const option = {
    ...baseChartOption(),
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: {
        rotate: names.length > 5 ? 30 : 0,
        fontSize: 11,
      },
    },
    yAxis: [
      {
        type: 'value',
        name: '交易量(kg)',
        nameTextStyle: { fontSize: 11 },
        axisLabel: { fontSize: 11 },
      },
      {
        type: 'value',
        name: '金额(元)',
        nameTextStyle: { fontSize: 11 },
        axisLabel: { fontSize: 11 },
      },
    ],
    series: [
      {
        name: '交易量',
        type: 'bar',
        data: volumes,
        yAxisIndex: 0,
        barMaxWidth: 28,
      },
      {
        name: '金额',
        type: 'bar',
        data: amounts,
        yAxisIndex: 1,
        barMaxWidth: 28,
      },
    ],
  }

  varietyChart.setOption(option, true)
}

function renderTrendChart() {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const months = trendData.value.map(d => d.month)
  const volumes = trendData.value.map(d => d.volume)
  const amounts = trendData.value.map(d => d.amount)

  const option = {
    ...baseChartOption(),
    xAxis: {
      type: 'category',
      data: months,
      axisLabel: { fontSize: 11 },
    },
    yAxis: [
      {
        type: 'value',
        name: '金额(元)',
        nameTextStyle: { fontSize: 11 },
        axisLabel: { fontSize: 11 },
      },
      {
        type: 'value',
        name: '订单数',
        nameTextStyle: { fontSize: 11 },
        axisLabel: { fontSize: 11 },
      },
    ],
    series: [
      {
        name: '交易金额',
        type: 'line',
        data: amounts,
        yAxisIndex: 0,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
      },
      {
        name: '订单数',
        type: 'line',
        data: trendData.value.map(d => d.orderCount),
        yAxisIndex: 1,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
      },
    ],
  }

  trendChart.setOption(option, true)
}

function renderPriceChart() {
  if (!priceChartRef.value) return
  if (!priceChart) {
    priceChart = echarts.init(priceChartRef.value)
  }

  const months = priceData.value.map(d => d.month)
  const amounts = priceData.value.map(d => d.amount)

  const option = {
    ...baseChartOption(),
    xAxis: {
      type: 'category',
      data: months,
      axisLabel: { fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      name: '价格指数',
      nameTextStyle: { fontSize: 11 },
      axisLabel: { fontSize: 11 },
    },
    series: [
      {
        name: '价格指数',
        type: 'line',
        data: amounts,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: chartSeries[2] + '33' },
            { offset: 1, color: chartSeries[2] + '05' },
          ]),
        },
      },
    ],
  }

  priceChart.setOption(option, true)
}

// ─── Responsive resize ───────────────────────────────
function handleResize() {
  varietyChart?.resize()
  trendChart?.resize()
  priceChart?.resize()
}

// ─── Lifecycle ───────────────────────────────────────
onMounted(async () => {
  await loadSummary()
  loadVariety()
  loadTrend()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  varietyChart?.dispose()
  trendChart?.dispose()
  priceChart?.dispose()
})
</script>

<style scoped>
.trade-stats-page {
  padding-bottom: var(--space-6);
  background-color: var(--color-surface-base);
}

/* KPI 卡片组 */
.kpi-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-3);
  padding: var(--space-4);
}

.kpi-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-4) var(--space-3);
  background-color: var(--color-surface-raised);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-1);
}

.kpi-label {
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.kpi-value {
  font-size: 24px;
  font-weight: 500;
  color: var(--color-text-primary);
  line-height: 1.3;
  margin-top: var(--space-1);
  font-family: var(--font-mono);
  font-variant-numeric: tabular-nums;
  font-feature-settings: "tnum";
}

.kpi-unit {
  font-size: 13px;
  color: var(--color-text-tertiary);
  margin-top: var(--space-1);
}

/* Section */
.section-block {
  margin-top: var(--space-5);
  padding: 0 var(--space-4);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 var(--space-3) 0;
  padding-left: var(--space-2);
  border-left: 3px solid var(--color-brand-primary);
  line-height: 1.4;
}

/* Chart container */
.chart-container {
  width: 100%;
  height: 280px;
  background-color: var(--color-surface-raised);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-1);
}

.chart-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 280px;
}

/* Trend header buttons */
.trend-header {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

/* Price form */
.price-form {
  margin-bottom: var(--space-3);
}

/* Responsive: 4-column KPI on wider screens */
@media (min-width: 576px) {
  .kpi-section {
    grid-template-columns: repeat(4, 1fr);
  }

  .chart-container {
    height: 320px;
  }
}
</style>
