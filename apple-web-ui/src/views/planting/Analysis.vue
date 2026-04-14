<template>
  <div class="analysis-page">
    <van-nav-bar title="种植分析" left-arrow @click-left="$router.back()" />

    <van-tabs v-model:active="activeTab" sticky animated swipeable>
      <!-- Tab 1: Yield Ranking -->
      <van-tab title="亩产排名">
        <div class="tab-content">
          <van-cell-group inset title="筛选条件" style="margin-top: var(--space-3)">
            <van-field
              v-model="yieldFilter.variety"
              label="品种"
              placeholder="如: 红富士 (可选)"
              clearable
            />
            <van-field
              v-model.number="yieldFilter.year"
              label="年份"
              type="number"
              placeholder="如: 2026"
              clearable
            >
              <template #button>
                <van-button size="small" type="primary" :loading="yieldLoading" @click="loadYieldData">
                  查询
                </van-button>
              </template>
            </van-field>
          </van-cell-group>

          <van-empty v-if="yieldEmpty && !yieldLoading" description="暂无亩产数据" image="search" />

          <div v-if="!yieldEmpty" class="chart-section">
            <div class="section-title">亩产排名 (kg/亩)</div>
            <div ref="yieldChartRef" class="chart-container"></div>
          </div>
        </div>
      </van-tab>

      <!-- Tab 2: Premium Rate -->
      <van-tab title="优果率">
        <div class="tab-content">
          <van-cell-group inset title="筛选条件" style="margin-top: var(--space-3)">
            <van-field
              v-model="premiumFilter.variety"
              label="品种"
              placeholder="如: 红富士 (可选)"
              clearable
            />
            <van-field
              v-model.number="premiumFilter.year"
              label="年份"
              type="number"
              placeholder="如: 2026"
              clearable
            >
              <template #button>
                <van-button size="small" type="primary" :loading="premiumLoading" @click="loadPremiumData">
                  查询
                </van-button>
              </template>
            </van-field>
          </van-cell-group>

          <van-empty v-if="premiumEmpty && !premiumLoading" description="暂无优果率数据" image="search" />

          <div v-if="!premiumEmpty" class="chart-section">
            <div class="section-title">优果率 (%)</div>
            <div ref="premiumChartRef" class="chart-container"></div>
          </div>
        </div>
      </van-tab>

      <!-- Tab 3: Pest Incidence -->
      <van-tab title="病虫害">
        <div class="tab-content">
          <van-cell-group inset title="筛选条件" style="margin-top: var(--space-3)">
            <van-field
              v-model.number="pestFilter.year"
              label="年份"
              type="number"
              placeholder="如: 2026"
              clearable
            >
              <template #button>
                <van-button size="small" type="primary" :loading="pestLoading" @click="loadPestData">
                  查询
                </van-button>
              </template>
            </van-field>
          </van-cell-group>

          <van-empty v-if="pestEmpty && !pestLoading" description="暂无病虫害数据" image="search" />

          <div v-if="!pestEmpty" class="chart-section">
            <div class="section-title">病虫害发生率 (%)</div>
            <div ref="pestChartRef" class="chart-container"></div>
          </div>
        </div>
      </van-tab>

      <!-- Tab 4: Plot Comparison -->
      <van-tab title="地块对比">
        <div class="tab-content">
          <van-cell-group inset title="对比条件" style="margin-top: var(--space-3)">
            <van-field
              v-model="compareIds"
              label="果园ID"
              placeholder="多个ID用逗号分隔，如: 1,2,3"
              type="textarea"
              rows="2"
              autosize
              clearable
            >
              <template #button>
                <van-button size="small" type="primary" :loading="compareLoading" @click="loadCompareData">
                  查询
                </van-button>
              </template>
            </van-field>
          </van-cell-group>

          <van-empty v-if="compareEmpty && !compareLoading" description="请输入果园ID进行对比" image="search" />

          <!-- Comparison Cards -->
          <div v-if="!compareEmpty" class="compare-list">
            <div v-for="item in compareList" :key="item.orchardId" class="compare-card">
              <div class="compare-header">
                <span class="compare-name">{{ item.orchardName || `果园 #${item.orchardId}` }}</span>
              </div>
              <div class="compare-metrics">
                <div class="metric-item">
                  <span class="metric-label">亩产</span>
                  <span class="metric-value nums-tabular">
                    {{ item.yieldMetrics?.yieldPerMu != null ? `${formatNum(item.yieldMetrics.yieldPerMu)} kg` : '-' }}
                  </span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">优果率</span>
                  <span class="metric-value nums-tabular">
                    <van-tag
                      v-if="item.premiumMetrics?.premiumRate != null"
                      :type="premiumRateTagType(item.premiumMetrics.premiumRate)"
                      size="large"
                    >
                      {{ formatPercent(item.premiumMetrics.premiumRate) }}
                    </van-tag>
                    <span v-else>-</span>
                  </span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">病虫害率</span>
                  <span class="metric-value nums-tabular">
                    <van-tag
                      v-if="item.pestMetrics?.incidenceRate != null"
                      :type="pestRateTagType(item.pestMetrics.incidenceRate)"
                      size="large"
                    >
                      {{ formatPercent(item.pestMetrics.incidenceRate) }}
                    </van-tag>
                    <span v-else>-</span>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { showFailToast } from 'vant'
import { plantingApi } from '@/api/planting.js'
import { useChart } from '@/design/echarts-theme'
import { color } from '@/design/tokens'

// ---------- Shared state ----------
const activeTab = ref(0)

// ---------- Tab 1: Yield Ranking ----------
const yieldChartRef = ref(null)
const yieldChart = useChart(yieldChartRef)
const yieldLoading = ref(false)
const yieldEmpty = ref(true)

const yieldFilter = reactive({
  variety: '',
  year: new Date().getFullYear(),
})

async function loadYieldData() {
  yieldLoading.value = true
  try {
    const params = {}
    if (yieldFilter.variety) params.variety = yieldFilter.variety
    if (yieldFilter.year) params.year = yieldFilter.year
    const res = await plantingApi.getYieldRanking(params)
    const data = res || []
    yieldEmpty.value = !data.length
    buildYieldChart(data)
  } catch (e) {
    yieldEmpty.value = true
    showFailToast(e?.message || '查询失败')
  } finally {
    yieldLoading.value = false
  }
}

function buildYieldChart(data) {
  // Sort by yield ascending for horizontal bar (bottom=lowest, top=highest)
  const sorted = [...data].sort((a, b) => a.yieldPerMu - b.yieldPerMu)
  const names = sorted.map(d => d.orchardName || `#${d.orchardId}`)
  const values = sorted.map(d => d.yieldPerMu)

  yieldChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}<br/>亩产: <b>{c}</b> kg/亩',
    },
    grid: { top: 8, bottom: 8, left: 8, right: 48, containLabel: true },
    xAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}',
        fontFamily: '"JetBrains Mono", "SF Mono", Consolas, monospace',
      },
    },
    yAxis: {
      type: 'category',
      data: names,
      inverse: false,
    },
    series: [{
      type: 'bar',
      data: values,
      label: {
        show: true,
        position: 'right',
        formatter: '{c} kg',
        fontFamily: '"JetBrains Mono", "SF Mono", Consolas, monospace',
        fontSize: 12,
      },
    }],
  })
}

// ---------- Tab 2: Premium Rate ----------
const premiumChartRef = ref(null)
const premiumChart = useChart(premiumChartRef)
const premiumLoading = ref(false)
const premiumEmpty = ref(true)

const premiumFilter = reactive({
  variety: '',
  year: new Date().getFullYear(),
})

async function loadPremiumData() {
  premiumLoading.value = true
  try {
    const params = {}
    if (premiumFilter.variety) params.variety = premiumFilter.variety
    if (premiumFilter.year) params.year = premiumFilter.year
    const res = await plantingApi.getPremiumRates(params)
    const data = res || []
    premiumEmpty.value = !data.length
    buildPremiumChart(data)
  } catch (e) {
    premiumEmpty.value = true
    showFailToast(e?.message || '查询失败')
  } finally {
    premiumLoading.value = false
  }
}

function buildPremiumChart(data) {
  const names = data.map(d => d.orchardName || `#${d.orchardId}`)
  const values = data.map(d => d.premiumRate)

  premiumChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}<br/>优果率: <b>{c}%</b>',
    },
    grid: { top: 16, bottom: 8, left: 8, right: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: {
        rotate: names.length > 5 ? 30 : 0,
      },
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}%',
        fontFamily: '"JetBrains Mono", "SF Mono", Consolas, monospace',
      },
    },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: {
        color: color.accent.leafGreen,
        borderRadius: [4, 4, 0, 0],
      },
      label: {
        show: true,
        position: 'top',
        formatter: '{c}%',
        fontFamily: '"JetBrains Mono", "SF Mono", Consolas, monospace',
        fontSize: 12,
      },
    }],
  })
}

// ---------- Tab 3: Pest Incidence ----------
const pestChartRef = ref(null)
const pestChart = useChart(pestChartRef)
const pestLoading = ref(false)
const pestEmpty = ref(true)

const pestFilter = reactive({
  year: new Date().getFullYear(),
})

async function loadPestData() {
  pestLoading.value = true
  try {
    const params = {}
    if (pestFilter.year) params.year = pestFilter.year
    const res = await plantingApi.getPestIncidences(params)
    const data = res || []
    pestEmpty.value = !data.length
    buildPestChart(data)
  } catch (e) {
    pestEmpty.value = true
    showFailToast(e?.message || '查询失败')
  } finally {
    pestLoading.value = false
  }
}

function buildPestChart(data) {
  const names = data.map(d => d.orchardName || `#${d.orchardId}`)
  const values = data.map(d => d.incidenceRate)

  pestChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}<br/>发生率: <b>{c}%</b>',
    },
    grid: { top: 16, bottom: 8, left: 8, right: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: {
        rotate: names.length > 5 ? 30 : 0,
      },
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}%',
        fontFamily: '"JetBrains Mono", "SF Mono", Consolas, monospace',
      },
    },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: {
        color: color.semantic.warning,
        borderRadius: [4, 4, 0, 0],
      },
      label: {
        show: true,
        position: 'top',
        formatter: '{c}%',
        fontFamily: '"JetBrains Mono", "SF Mono", Consolas, monospace',
        fontSize: 12,
      },
    }],
  })
}

// ---------- Tab 4: Plot Comparison ----------
const compareIds = ref('')
const compareLoading = ref(false)
const compareEmpty = ref(true)
const compareList = ref([])

async function loadCompareData() {
  const ids = parseOrchardIds(compareIds.value)
  if (!ids.length) {
    showFailToast('请输入至少一个果园ID')
    return
  }
  compareLoading.value = true
  try {
    const res = await plantingApi.comparePlots(ids.join(','))
    const data = res || []
    compareList.value = data
    compareEmpty.value = !data.length
  } catch (e) {
    compareList.value = []
    compareEmpty.value = true
    showFailToast(e?.message || '查询失败')
  } finally {
    compareLoading.value = false
  }
}

// ---------- Helpers ----------
function parseOrchardIds(raw) {
  if (!raw) return []
  return String(raw)
    .split(/[,，\s]+/)
    .map(s => s.trim())
    .filter(Boolean)
    .map(Number)
    .filter(n => !isNaN(n) && n > 0)
}

function formatNum(v) {
  return Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 1 })
}

function formatPercent(v) {
  return `${Number(v).toFixed(1)}%`
}

function premiumRateTagType(rate) {
  if (rate >= 80) return 'success'
  if (rate >= 60) return 'warning'
  return 'danger'
}

function pestRateTagType(rate) {
  if (rate <= 10) return 'success'
  if (rate <= 30) return 'warning'
  return 'danger'
}

// ---------- Resize handling ----------
function handleResize() {
  yieldChart.resize()
  premiumChart.resize()
  pestChart.resize()
}

// ---------- Lifecycle ----------
onMounted(async () => {
  await nextTick()
  // Load default year data for first tab
  loadYieldData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  yieldChart.dispose()
  premiumChart.dispose()
  pestChart.dispose()
})
</script>

<style scoped>
.analysis-page {
  padding-bottom: var(--space-8);
  background: var(--color-surface-base);
  min-height: 100vh;
}

.tab-content {
  padding: 0 var(--space-4);
}

.chart-section {
  background: var(--color-surface-raised);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  box-shadow: var(--shadow-1);
  margin-top: var(--space-4);
}

.section-title {
  font-size: var(--font-size-body);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--space-3);
}

.chart-container {
  width: 100%;
  height: 280px;
}

/* Comparison cards */
.compare-list {
  margin-top: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.compare-card {
  background: var(--color-surface-raised);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  box-shadow: var(--shadow-1);
}

.compare-header {
  margin-bottom: var(--space-3);
  padding-bottom: var(--space-2);
  border-bottom: 1px solid var(--color-border-divider);
}

.compare-name {
  font-size: var(--font-size-body);
  font-weight: 600;
  color: var(--color-text-primary);
}

.compare-metrics {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
}

.metric-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
}

.metric-label {
  font-size: var(--font-size-caption);
  color: var(--color-text-tertiary);
}

.metric-value {
  font-size: var(--font-size-body);
  font-weight: 500;
  color: var(--color-text-primary);
}
</style>
