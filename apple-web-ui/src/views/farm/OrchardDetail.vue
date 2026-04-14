<template>
  <div class="orchard-detail-page">
    <!-- Loading -->
    <div v-if="loading" class="page-loading">
      <van-loading size="48px" vertical>加载果园信息...</van-loading>
    </div>

    <!-- Error -->
    <van-empty v-else-if="error" image="error" :description="error">
      <van-button type="primary" size="small" @click="loadOrchard">重新加载</van-button>
    </van-empty>

    <!-- Content -->
    <template v-else-if="orchard">
      <!-- Top Info Card -->
      <div class="info-card">
        <div class="info-card-header">
          <h2 class="info-card-title">{{ orchard.orchardName || orchard.name || '-' }}</h2>
          <van-tag :type="statusTagType(orchard.status)" size="medium">
            {{ statusLabel(orchard.status) }}
          </van-tag>
        </div>
        <div class="info-card-meta">
          <span class="meta-item">
            <van-icon name="gem-o" color="var(--color-leaf-green)" size="16" />
            {{ orchard.variety || '未设置品种' }}
          </span>
          <span class="meta-item">
            <van-icon name="map-marked" color="var(--color-info)" size="16" />
            {{ orchard.area || 0 }} 亩
          </span>
        </div>
      </div>

      <!-- Basic Info -->
      <van-cell-group inset title="基本信息" class="section-group">
        <van-cell title="地理位置" :value="orchard.location || '-'" />
        <van-cell title="种植日期" :value="fmt(orchard.plantDate)" />
        <van-cell title="负责人" :value="orchard.owner || '-'" />
        <van-cell title="联系电话" :value="orchard.phone || '-'" />
        <van-cell title="备注" :value="orchard.remark || '-'" />
        <van-cell title="创建时间" :value="fmt(orchard.createTime)" />
      </van-cell-group>

      <!-- Trace Batches -->
      <div class="section-group">
        <van-cell-group inset :title="`溯源批次 (${batches.length})`">
          <template v-if="batches.length">
            <van-cell
              v-for="batch in batches"
              :key="batch.id || batch.traceCode"
              :title="batch.traceCode || batch.batchNo || '无溯源码'"
              :label="`产品: ${batch.productType || '-'} · 状态: ${statusLabel(batch.currentStatus)}`"
              is-link
              @click="goToTraceDetail(batch)"
            >
              <template #value>
                <van-tag :type="statusTagType(batch.currentStatus)" plain>
                  {{ statusLabel(batch.currentStatus) }}
                </van-tag>
              </template>
            </van-cell>
          </template>
          <van-empty v-else description="暂无溯源批次" image="search" />
        </van-cell-group>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { farmApi } from '@/api/farm.js'
import { traceApi } from '@/api/trace.js'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref(null)
const orchard = ref(null)
const batches = ref([])

function statusTagType(status) {
  const s = (status || '').toUpperCase()
  const map = { ACTIVE: 'success', INACTIVE: 'default', PLANTED: 'primary', HARVESTED: 'success', IN_STORAGE: 'warning', IN_TRANSIT: 'warning', SOLD: 'default' }
  return map[s] || 'default'
}

function statusLabel(status) {
  const s = (status || '').toUpperCase()
  const map = { ACTIVE: '正常', INACTIVE: '休耕', PLANTED: '种植中', HARVESTED: '已采收', IN_STORAGE: '入库中', IN_TRANSIT: '运输中', SOLD: '已售出' }
  return map[s] || status || '-'
}

function fmt(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

async function loadOrchard() {
  const id = route.params.id
  if (!id) {
    error.value = '缺少果园 ID'
    loading.value = false
    return
  }
  loading.value = true
  error.value = null
  try {
    orchard.value = await farmApi.getOrchard(id)
  } catch (e) {
    error.value = '果园信息加载失败'
  } finally {
    loading.value = false
  }
}

async function loadBatches() {
  const id = route.params.id
  if (!id) return
  try {
    const res = await traceApi.getBatches({ orchardId: id })
    batches.value = res?.records || res || []
  } catch (e) {
    batches.value = []
  }
}

function goToTraceDetail(batch) {
  const code = batch.traceCode || batch.batchNo
  if (code) {
    router.push(`/trace/detail/${code}`)
  }
}

onMounted(() => {
  loadOrchard()
  loadBatches()
})
</script>

<style scoped>
.orchard-detail-page {
  padding-bottom: var(--space-6);
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: calc(var(--space-8) * 2.5) 0;
}

/* Info Card */
.info-card {
  margin: 0 var(--space-4) var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--color-surface-raised);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-2);
}

.info-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
}

.info-card-title {
  font-size: var(--space-5);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
  line-height: 1.35;
}

.info-card-meta {
  display: flex;
  gap: var(--space-5);
  margin-top: var(--space-3);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--space-3);
  color: var(--color-text-secondary);
}

/* Sections */
.section-group {
  margin-bottom: var(--space-4);
}
</style>
