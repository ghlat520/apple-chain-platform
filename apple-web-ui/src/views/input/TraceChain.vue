<template>
  <div class="trace-chain-page">
    <!-- Search / Scan Entry -->
    <van-search
      v-model="searchCode"
      placeholder="输入溯源码或批次号查询"
      @search="handleSearch"
      @clear="handleClear"
    />

    <!-- Loading -->
    <div v-if="loading" class="page-loading">
      <van-loading size="48px" vertical>加载溯源链...</van-loading>
    </div>

    <!-- Error -->
    <van-empty v-else-if="error" image="error" :description="error">
      <van-button type="primary" size="small" @click="handleSearch">重新查询</van-button>
    </van-empty>

    <!-- Content -->
    <template v-else-if="traceData">
      <!-- Top Code Tag -->
      <div class="chain-header">
        <van-tag type="primary" size="large">{{ code }}</van-tag>
        <van-tag
          v-if="traceData.currentStatus"
          :type="statusTagType(traceData.currentStatus)"
          size="medium"
          plain
          style="margin-left: var(--space-2)"
        >
          {{ statusLabel(traceData.currentStatus) }}
        </van-tag>
      </div>

      <!-- Timeline Steps -->
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

      <!-- Input Usage Details -->
      <div v-if="usageList.length" class="section-gap">
        <van-cell-group inset title="农资使用详情">
          <van-cell
            v-for="item in usageList"
            :key="item.id"
            :title="item.productName || item.name || '-'"
            is-link
            @click="showUsageDetail(item)"
          >
            <template #label>
              <span>用量: {{ item.amount || item.quantity || '-' }} {{ item.unit || '' }}</span>
              <span style="margin-left: var(--space-3)">{{ fmt(item.usageDate || item.useTime) }}</span>
            </template>
          </van-cell>
        </van-cell-group>
      </div>

      <!-- Blockchain (if available) -->
      <div v-if="traceData.blockchainHash" class="section-gap">
        <van-cell-group inset title="区块链存证">
          <van-cell title="链上状态">
            <template #value>
              <van-tag type="success">已上链</van-tag>
            </template>
          </van-cell>
          <van-cell title="区块哈希" :value="traceData.blockchainHash" />
          <van-cell v-if="traceData.blockTime" title="上链时间" :value="fmt(traceData.blockTime)" />
        </van-cell-group>
      </div>

      <!-- Detail Info -->
      <van-cell-group inset title="基本信息" class="section-gap">
        <van-cell title="溯源码" :value="traceData.traceCode || '-'" />
        <van-cell title="产品类型" :value="traceData.productType || '-'" />
        <van-cell title="批次编号" :value="traceData.batchNo || '-'" />
        <van-cell title="果园" :value="traceData.orchardName || '-'" />
        <van-cell title="品种" :value="traceData.variety || '-'" />
        <van-cell title="数量" :value="traceData.quantity ? `${traceData.quantity} ${traceData.unit || ''}`.trim() : '-'" />
      </van-cell-group>
    </template>

    <!-- Initial Empty State -->
    <van-empty v-else description="请输入溯源码查询农资溯源链" image="search" />

    <!-- Usage Detail Popup -->
    <van-popup
      v-model:show="showPopup"
      position="bottom"
      round
      :style="{ maxHeight: '70vh', overflowY: 'auto' }"
    >
      <div class="popup-header">
        <span>农资使用详情</span>
        <van-icon name="cross" @click="showPopup = false" />
      </div>
      <van-cell-group inset v-if="popupItem">
        <van-cell title="农资名称" :value="popupItem.productName || popupItem.name || '-'" />
        <van-cell title="使用量" :value="`${popupItem.amount || popupItem.quantity || '-'} ${popupItem.unit || ''}`.trim()" />
        <van-cell title="使用时间" :value="fmt(popupItem.usageDate || popupItem.useTime)" />
        <van-cell title="使用人" :value="popupItem.operatorName || popupItem.userName || '-'" />
        <van-cell title="果园" :value="popupItem.orchardName || '-'" />
        <van-cell title="备注" :value="popupItem.remark || '-'" />
      </van-cell-group>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { traceApi } from '@/api/trace.js'
import { inputApi } from '@/api/input.js'

const route = useRoute()
const router = useRouter()

const searchCode = ref('')
const code = computed(() => route.params.code || searchCode.value)
const loading = ref(false)
const error = ref(null)
const traceData = ref(null)
const usageList = ref([])
const showPopup = ref(false)
const popupItem = ref(null)

const timeline = computed(() => {
  const raw = traceData.value?.timeline
  if (Array.isArray(raw) && raw.length) return raw
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

async function loadData(queryCode) {
  if (!queryCode) return
  loading.value = true
  error.value = null
  traceData.value = null
  usageList.value = []

  try {
    traceData.value = await traceApi.getChain(queryCode)
  } catch (e) {
    error.value = '溯源信息加载失败'
    loading.value = false
    return
  }

  // Load input usage (non-critical, silent fallback)
  try {
    const res = await inputApi.getUsageByTrace(queryCode)
    usageList.value = res?.records || res || []
  } catch (e) {
    usageList.value = []
  }

  loading.value = false
}

function handleSearch() {
  if (!searchCode.value.trim()) return
  loadData(searchCode.value.trim())
}

function handleClear() {
  traceData.value = null
  error.value = null
  usageList.value = []
}

function showUsageDetail(item) {
  popupItem.value = item
  showPopup.value = true
}

onMounted(() => {
  // If route has code param, auto-load
  if (route.params.code) {
    searchCode.value = route.params.code
    loadData(route.params.code)
  }
})
</script>

<style scoped>
.trace-chain-page {
  padding-bottom: var(--space-6);
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: calc(var(--space-8) * 2.5) 0;
}

/* Chain Header */
.chain-header {
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

/* Popup */
.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-5);
  font-size: var(--space-4);
  font-weight: 600;
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border-default);
}
</style>
