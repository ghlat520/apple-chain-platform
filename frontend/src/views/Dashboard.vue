<template>
  <div class="page-container">
    <div class="dashboard-stats" v-if="stats">
      <van-grid :column-num="2" :border="false" :gutter="12">
        <van-grid-item>
          <div class="stat-card green">
            <div class="stat-value">{{ stats.totalOrchards }}</div>
            <div class="stat-label">注册果园</div>
          </div>
        </van-grid-item>
        <van-grid-item>
          <div class="stat-card blue">
            <div class="stat-value">{{ stats.totalFarmers }}</div>
            <div class="stat-label">注册果农</div>
          </div>
        </van-grid-item>
        <van-grid-item>
          <div class="stat-card orange">
            <div class="stat-value">{{ stats.totalTraceRecords }}</div>
            <div class="stat-label">溯源批次</div>
          </div>
        </van-grid-item>
        <van-grid-item>
          <div class="stat-card purple">
            <div class="stat-value">{{ stats.pendingTrades }}</div>
            <div class="stat-label">待处理交易</div>
          </div>
        </van-grid-item>
      </van-grid>
    </div>

    <div class="section-header" style="margin-top:16px;">
      <span class="section-title">交易量趋势（近6月）</span>
    </div>
    <div class="chart-card">
      <div ref="trendChart" style="height:220px;"></div>
    </div>

    <!-- 演示闭环入口 -->
    <div style="padding:16px 16px 0;">
      <van-button
        block
        type="primary"
        color="#07c160"
        icon="guide-o"
        @click="router.push('/orchards')"
      >
        开始全链路演示
      </van-button>
    </div>

    <div class="section-header" style="margin-top:16px;">
      <span class="section-title">待处理事项</span>
    </div>
    <van-cell-group inset>
      <van-cell
        v-for="todo in todos"
        :key="todo.id"
        :title="todo.title"
        :label="todo.createdAt"
        is-link
        @click="handleTodoClick(todo)"
      >
        <template #right-icon>
          <van-tag :type="todo.urgent ? 'danger' : 'warning'">
            {{ todo.urgent ? '紧急' : '待处理' }}
          </van-tag>
        </template>
      </van-cell>
      <van-empty v-if="todos.length === 0" description="暂无待处理事项" image-size="80" />
    </van-cell-group>

    <div class="section-header" style="margin-top:16px;">
      <span class="section-title">优质果园 TOP5</span>
    </div>
    <van-cell-group inset>
      <van-cell
        v-for="(orchard, idx) in topOrchards"
        :key="orchard.id"
        :title="orchard.name"
        :label="`总面积: ${orchard.area}亩 · ${orchard.location}`"
        is-link
        @click="router.push(`/orchards/${orchard.id}`)"
      >
        <template #icon>
          <div class="rank-badge" :class="`rank-${idx + 1}`">{{ idx + 1 }}</div>
        </template>
      </van-cell>
    </van-cell-group>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { dashboardApi } from '@/api/dashboard'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const router = useRouter()
const stats = ref(null)
const todos = ref([])
const topOrchards = ref([])
const trendChart = ref(null)
let chartInstance = null

onMounted(async () => {
  const [statsRes, trendRes, topRes, todosRes] = await Promise.all([
    dashboardApi.getStats(),
    dashboardApi.getTrend(),
    dashboardApi.getTopOrchards(),
    dashboardApi.getPendingTodos()
  ])
  stats.value = statsRes.data
  topOrchards.value = topRes.data || []
  todos.value = todosRes.data || []
  await nextTick()
  renderTrendChart(trendRes.data)
})

function renderTrendChart(data) {
  if (!trendChart.value) return
  chartInstance = echarts.init(trendChart.value)
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data?.months || [] },
    yAxis: { type: 'value', name: '交易量(吨)' },
    series: [{
      name: '交易量',
      type: 'line',
      data: data?.values || [],
      smooth: true,
      areaStyle: { color: 'rgba(7,193,96,0.1)' },
      lineStyle: { color: '#07c160' },
      itemStyle: { color: '#07c160' }
    }]
  })
}

function handleTodoClick(todo) {
  if (todo.type === 'trade') router.push('/trades')
  else if (todo.type === 'orchard') router.push('/orchards')
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
.rank-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #555;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  margin-right: 8px;
}
.rank-badge.rank-1 { background: #ffd700; color: #7b5800; }
.rank-badge.rank-2 { background: #c0c0c0; color: #444; }
.rank-badge.rank-3 { background: #cd7f32; color: #fff; }
</style>
