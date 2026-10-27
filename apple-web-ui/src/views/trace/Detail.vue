<template>
  <div class="trace-detail-page">
    <!-- Loading -->
    <div v-if="loading" class="page-loading">
      <van-loading size="48px" vertical>加载溯源信息...</van-loading>
    </div>

    <!-- Error -->
    <van-empty v-else-if="error" image="error" :description="error">
      <van-button type="primary" size="small" @click="loadData">重新加载</van-button>
    </van-empty>

    <!-- Content -->
    <template v-else-if="traceData">
      <!-- Top Batch Tag -->
      <div class="batch-header">
        <van-tag type="primary" size="large">{{ traceData.traceCode || batchCode }}</van-tag>
        <van-tag
          v-if="traceData.currentStatus"
          :type="statusTagType(traceData.currentStatus)"
          size="large"
          plain
          style="margin-left: var(--space-2)"
        >
          {{ statusLabel(traceData.currentStatus) }}
        </van-tag>
      </div>

      <!-- Vertical Steps Timeline -->
      <div v-if="timeline.length" class="timeline-section">
        <van-cell-group inset title="溯源时间线">
          <van-steps direction="vertical" :active="timeline.length - 1" active-color="var(--color-brand-primary)">
            <van-step v-for="(node, idx) in timeline" :key="idx">
              <h4 class="step-title">{{ nodeTypeLabel(node.nodeType) }}</h4>
              <p class="step-summary">{{ node.summary || node.description || '-' }}</p>
              <p class="step-meta">
                {{ node.operatorName || '-' }} · {{ node.location || '-' }}
              </p>
              <p class="step-time">{{ fmt(node.nodeTime) }}</p>
            </van-step>
          </van-steps>
        </van-cell-group>
      </div>
      <van-empty v-else description="暂无溯源节点" image="search" class="section-gap" />

      <!-- Detail Info -->
      <van-cell-group inset title="批次详情" class="section-gap">
        <van-cell title="溯源码" :value="traceData.traceCode || '-'" />
        <van-cell title="产品类型" :value="traceData.productType || '-'" />
        <van-cell title="批次编号" :value="traceData.batchNo || '-'" />
        <van-cell title="果园" :value="traceData.orchardName || '-'" />
        <van-cell title="果农" :value="traceData.farmerName || traceData.operatorName || '-'" />
        <van-cell title="数量" :value="traceData.quantity ? `${traceData.quantity} ${traceData.unit || ''}`.trim() : '-'" />
        <van-cell title="品种" :value="traceData.variety || '-'" />
        <van-cell title="创建时间" :value="fmt(traceData.createTime)" />
      </van-cell-group>

      <!-- Blockchain Certification -->
      <div v-if="traceData.blockchainHash" class="section-gap">
        <van-cell-group inset title="区块链存证">
          <van-cell title="链上状态">
            <template #value>
              <van-tag type="success">已上链</van-tag>
            </template>
          </van-cell>
          <van-cell title="区块哈希" :value="traceData.blockchainHash" />
          <van-cell v-if="traceData.blockTime" title="上链时间" :value="fmt(traceData.blockTime)" />
          <van-cell v-if="traceData.chainTxId" title="交易ID" :value="traceData.chainTxId" />
        </van-cell-group>
      </div>

      <!-- Agricultural Input Usage -->
      <div v-if="usageList.length" class="section-gap">
        <van-cell-group inset title="农资使用记录">
          <van-cell
            v-for="item in usageList"
            :key="item.id"
            :title="item.productName || item.name || '-'"
            :label="`用量: ${item.amount || item.quantity || '-'} ${item.unit || ''} · ${fmt(item.usageDate || item.useTime)}`"
          />
        </van-cell-group>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { traceApi } from '@/api/trace.js'
import { inputApi } from '@/api/input.js'

const route = useRoute()
const batchCode = computed(() => route.params.batchCode || '')

const loading = ref(true)
const error = ref(null)
const traceData = ref(null)
const usageList = ref([])

const timeline = computed(() => {
  const raw = traceData.value?.timeline
  if (Array.isArray(raw) && raw.length) return raw
  // Fallback: build from chain nodes if available
  const nodes = traceData.value?.nodes || traceData.value?.chainNodes
  if (Array.isArray(nodes) && nodes.length) return nodes
  return []
})

function statusTagType(status) {
  const map = { PLANTED: 'primary', HARVESTED: 'success', IN_STORAGE: 'warning', IN_TRANSIT: 'warning', SOLD: 'default' }
  return map[status] || 'default'
}

function statusLabel(status) {
  const map = { PLANTED: '种植中', HARVESTED: '已采收', IN_STORAGE: '入库中', IN_TRANSIT: '运输中', SOLD: '已售出' }
  return map[status] || status || '-'
}

function nodeTypeLabel(type) {
  const map = {
    PLANT: '果园种植', GROW: '田间管理', HARVEST: '果园采收',
    INSPECT: '质量检测', STORAGE: '仓储入库', TRANSPORT: '冷链运输',
    LOGISTICS: '物流配送', SELL: '终端销售', TRADE: '交易',
  }
  return map[type] || type || '-'
}

function fmt(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

async function loadData() {
  const code = batchCode.value
  if (!code) {
    error.value = '缺少溯源批次号'
    loading.value = false
    return
  }
  loading.value = true
  error.value = null
  try {
    traceData.value = await traceApi.getChain(code)
  } catch (e) {
    error.value = '溯源信息加载失败'
    loading.value = false
    return
  }
  // Load input usage (non-critical, silent fallback)
  try {
    const res = await inputApi.getUsageByTrace(code)
    usageList.value = res?.records || res || []
  } catch (e) {
    usageList.value = []
  }
  loading.value = false
}

onMounted(loadData)
</script>

<style scoped>
.trace-detail-page {
  padding-bottom: var(--space-6);
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: calc(var(--space-8) * 2.5) 0;
}

/* Batch Header */
.batch-header {
  display: flex;
  align-items: center;
  padding: var(--space-4) var(--space-4) var(--space-2);
  flex-wrap: wrap;
}

/* Timeline */
.timeline-section {
  margin-bottom: var(--space-4);
}

.step-title {
  font-size: var(--space-3);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 var(--space-1);
}

.step-summary {
  font-size: var(--space-3);
  color: var(--color-text-primary);
  margin: 0 0 var(--space-1);
}

.step-meta {
  font-size: var(--space-2);
  color: var(--color-text-tertiary);
  margin: 0 0 var(--space-1);
}

.step-time {
  font-size: var(--space-2);
  color: var(--color-text-disabled);
  margin: 0;
}

/* Section Gap */
.section-gap {
  margin-top: var(--space-4);
}
</style>
