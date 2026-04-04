<template>
  <div class="query-page">
    <div class="search-section">
      <van-search
        v-model="traceCode"
        placeholder="请输入溯源码查询"
        show-action
        @search="handleQuery"
      >
        <template #action>
          <van-button type="primary" size="small" :loading="loading" @click="handleQuery">查询</van-button>
        </template>
      </van-search>
    </div>

    <div v-if="!hasQueried" class="guide">
      <van-empty image="search" description="输入溯源码，查看完整产业链信息" />
      <p class="guide-tip">溯源码格式示例：AC-2024-001</p>
    </div>

    <div v-else-if="loading" class="loading-wrap">
      <van-loading type="spinner" color="#1989fa">查询中...</van-loading>
    </div>

    <div v-else-if="!traceResult" class="not-found">
      <van-empty image="error" description="未找到该溯源码对应的产品信息" />
      <van-button plain type="primary" size="small" @click="reset">重新查询</van-button>
    </div>

    <div v-else class="result-section">
      <!-- Product Card -->
      <div class="result-card">
        <div class="result-card-header">
          <van-icon name="success" color="#07c160" size="18" />
          <span>溯源验证通过</span>
          <van-tag type="success" size="medium">{{ traceCode }}</van-tag>
        </div>
        <van-cell-group inset>
          <van-cell title="产品名称" :value="traceResult.productName || '-'" />
          <van-cell title="苹果品种" :value="traceResult.variety || '-'" />
          <van-cell title="来源果园" :value="traceResult.orchardName || '-'" />
          <van-cell title="种植批次" :value="traceResult.batchCode || '-'" />
          <van-cell title="包装日期" :value="traceResult.packDate || '-'" />
          <van-cell title="重量(kg)" :value="traceResult.weight ?? '-'" />
          <van-cell title="质检编号" :value="traceResult.certificationNo || '-'" />
        </van-cell-group>
      </div>

      <!-- Timeline -->
      <div class="timeline-card" v-if="traceResult.chain && traceResult.chain.length > 0">
        <h3 class="section-title">产业链完整轨迹</h3>
        <van-steps direction="vertical" :active="traceResult.chain.length - 1">
          <van-step v-for="(step, index) in traceResult.chain" :key="index">
            <div class="step-content">
              <p class="step-title">{{ step.stage || `阶段${index + 1}` }}</p>
              <p class="step-desc">{{ step.description || step.detail || '-' }}</p>
              <p class="step-time">{{ step.time || step.date || '-' }}</p>
              <p class="step-operator" v-if="step.operator">操作人：{{ step.operator }}</p>
              <p class="step-location" v-if="step.location">地点：{{ step.location }}</p>
            </div>
          </van-step>
        </van-steps>
      </div>

      <!-- Orchard Info -->
      <div class="result-card" v-if="traceResult.orchard">
        <h3 class="section-title">果园信息</h3>
        <van-cell-group inset>
          <van-cell title="果园名称" :value="traceResult.orchard.name || '-'" />
          <van-cell title="地理位置" :value="traceResult.orchard.location || '-'" />
          <van-cell title="面积（亩）" :value="traceResult.orchard.area ?? '-'" />
          <van-cell title="负责人" :value="traceResult.orchard.owner || '-'" />
        </van-cell-group>
      </div>

      <!-- Batch Info -->
      <div class="result-card" v-if="traceResult.batch">
        <h3 class="section-title">种植批次</h3>
        <van-cell-group inset>
          <van-cell title="批次编号" :value="traceResult.batch.batchCode || '-'" />
          <van-cell title="种植年份" :value="traceResult.batch.plantYear ?? '-'" />
          <van-cell title="预计产量" :value="traceResult.batch.expectedYield ? `${traceResult.batch.expectedYield}kg` : '-'" />
          <van-cell title="实际产量" :value="traceResult.batch.actualYield ? `${traceResult.batch.actualYield}kg` : '-'" />
          <van-cell title="采收日期" :value="traceResult.batch.harvestDate || '-'" />
        </van-cell-group>
      </div>

      <!-- Agricultural Input Usage -->
      <div class="result-card">
        <h3 class="section-title">农资使用记录</h3>
        <van-cell-group v-if="usageRecords.length > 0" inset>
          <van-cell
            v-for="(record, index) in usageRecords"
            :key="index"
            :title="record.productName || '-'"
            :label="`${record.usageDate || '-'} | 操作人：${record.operator || '-'}`"
          >
            <template #value>
              <div class="usage-value">
                <van-tag :type="methodTagType(record.method)" size="medium">{{ record.method || '-' }}</van-tag>
                <span class="usage-quantity">{{ record.quantity ?? '-' }}{{ record.unit || '' }}</span>
              </div>
            </template>
          </van-cell>
        </van-cell-group>
        <div v-else class="usage-empty">
          <span>暂无农资使用记录</span>
        </div>
      </div>

      <div class="re-query">
        <van-button block plain type="default" @click="reset">重新查询</van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { traceApi } from '@/api/trace.js'
import { inputApi } from '@/api/input.js'
import { showToast } from 'vant'

const route = useRoute()
const traceCode = ref('')
const loading = ref(false)
const hasQueried = ref(false)
const traceResult = ref(null)
const usageRecords = ref([])

async function handleQuery() {
  if (!traceCode.value.trim()) {
    showToast('请输入溯源码')
    return
  }
  loading.value = true
  hasQueried.value = true
  traceResult.value = null
  usageRecords.value = []
  try {
    const code = traceCode.value.trim()
    const [res, usageRes] = await Promise.all([
      traceApi.getChain(code),
      inputApi.getUsageByTrace(code).catch(() => [])
    ])
    // Map API response { chain, nodes } to frontend display format
    const c = res?.chain || res || {}
    const nodes = res?.nodes || []
    traceResult.value = {
      productName: c.productType || '-',
      variety: c.productType || '-',
      orchardName: c.remark || '-',
      batchCode: c.batchNo || '-',
      packDate: '-',
      weight: '-',
      certificationNo: c.dataHash || '-',
      chain: nodes.map(n => ({
        stage: n.summary || n.nodeType || '-',
        description: n.detail ? (typeof n.detail === 'string' ? (() => { try { const d = JSON.parse(n.detail); return Object.entries(d).map(([k,v]) => `${k}: ${v}`).join(', ') } catch { return n.detail } })() : n.detail) : '-',
        time: n.nodeTime || '-',
        operator: n.operatorName || '-',
        location: n.location || '-'
      }))
    }
    usageRecords.value = Array.isArray(usageRes) ? usageRes : (usageRes?.data || usageRes?.records || [])
  } catch (e) {
    traceResult.value = null
  } finally {
    loading.value = false
  }
}

const methodTagType = (method) => {
  const map = { '撒施': 'primary', '喷洒': 'success', '滴灌': 'warning', '穴施': 'danger' }
  return map[method] || 'default'
}

function reset() {
  traceCode.value = ''
  hasQueried.value = false
  traceResult.value = null
  usageRecords.value = []
}

onMounted(() => {
  const code = route.query.code
  if (code) {
    traceCode.value = String(code)
    handleQuery()
  }
})
</script>

<style scoped>
.query-page {
  padding-bottom: 24px;
}

.search-section {
  background: #fff;
  padding-bottom: 8px;
  margin-bottom: 12px;
}

.guide {
  padding: 40px 16px;
  text-align: center;
}

.guide-tip {
  font-size: 12px;
  color: #c8c9cc;
  margin-top: 8px;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 16px;
}

.result-section {
  padding: 0 12px;
}

.result-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 0;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.result-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px 12px;
  font-size: 14px;
  font-weight: 600;
}

.timeline-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #323233;
  padding: 0 16px 12px;
}

.step-content {
  padding-left: 8px;
}

.step-title {
  font-size: 14px;
  font-weight: 500;
  color: #323233;
}

.step-desc {
  font-size: 12px;
  color: #646566;
  margin-top: 4px;
}

.step-time {
  font-size: 12px;
  color: #969799;
  margin-top: 2px;
}

.step-operator,
.step-location {
  font-size: 12px;
  color: #969799;
  margin-top: 2px;
}

.usage-value {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.usage-quantity {
  font-size: 13px;
  color: #323233;
  font-weight: 500;
}

.usage-empty {
  padding: 24px 16px;
  text-align: center;
  font-size: 13px;
  color: #c8c9cc;
}

.re-query {
  padding: 8px 0 16px;
}
</style>
