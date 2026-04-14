<template>
  <div class="precooling-page">
    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="待处理" name="PENDING" />
        <van-tab title="冷却中" name="COOLING" />
        <van-tab title="已完成" name="COMPLETED" />
        <van-tab title="失败" name="FAILED" />
      </van-tabs>
    </div>

    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openAddDrawer">
        新增预冷
      </van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadMore"
      >
        <van-cell-group
          v-for="item in list"
          :key="item.id"
          inset
          class="list-item"
        >
          <van-cell
            is-link
            @click="openDetailDrawer(item)"
          >
            <template #title>
              <span>{{ item.taskNo || `任务 #${item.id}` }}</span>
            </template>
            <template #label>
              <span class="cell-label-text">
                批次: {{ item.batchCode || '-' }}
                &middot; 初始温度: <span class="nums-tabular">{{ formatTemp(item.startTemp) }}</span>
                &rarr; 目标: <span class="nums-tabular">{{ formatTemp(item.targetTemp) }}</span>
              </span>
            </template>
            <template #value>
              <van-tag :type="statusTagType(item.status)">
                {{ statusLabel(item.status) }}
              </van-tag>
            </template>
          </van-cell>
          <van-cell label="" style="padding-top:0">
            <template #title>
              <span class="cell-meta-text">
                操作员: {{ item.operator || '-' }}
                &middot; 创建: {{ formatTime(item.createTime) }}
              </span>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty
          v-if="!loading && list.length === 0"
          description="暂无预冷记录"
        />
      </van-list>
    </van-pull-refresh>

    <!-- Add Precool Task Drawer -->
    <van-popup
      v-model:show="showAddDrawer"
      position="bottom"
      round
      :style="{ maxHeight: '90vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>新增预冷任务</span>
        <van-icon name="cross" @click="showAddDrawer = false" />
      </div>
      <van-form @submit="handleCreate" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.vehicleId"
            label="车辆ID"
            placeholder="请输入车辆ID"
            :rules="[{ required: true, message: '请填写车辆ID' }]"
          />
          <van-field
            v-model="form.batchCode"
            label="批次编码"
            placeholder="请输入批次编码"
            :rules="[{ required: true, message: '请填写批次编码' }]"
          />
          <van-field
            v-model="form.startTemp"
            label="初始温度(℃)"
            type="number"
            placeholder="当前货物温度"
            :rules="[{ required: true, message: '请填写初始温度' }]"
          />
          <van-field
            v-model="form.targetTemp"
            label="目标温度(℃)"
            type="number"
            placeholder="默认 2.0"
          />
          <van-field
            v-model="form.operator"
            label="操作员"
            placeholder="请输入操作员姓名"
          />
          <van-field
            v-model="form.remark"
            label="备注"
            type="textarea"
            rows="2"
            autosize
            placeholder="请输入备注"
          />
        </van-cell-group>
        <div class="drawer-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">
            确认创建
          </van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- Detail Drawer -->
    <van-popup
      v-model:show="showDetail"
      position="bottom"
      round
      :style="{ maxHeight: '90vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>预冷任务详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>

      <div v-if="detailLoading" class="drawer-loading">
        <van-loading type="spinner" />
      </div>

      <template v-else-if="currentItem">
        <van-cell-group inset>
          <van-cell title="任务编号" :value="currentItem.taskNo || `#${currentItem.id}`" />
          <van-cell title="状态">
            <template #value>
              <van-tag :type="statusTagType(currentItem.status)">
                {{ statusLabel(currentItem.status) }}
              </van-tag>
            </template>
          </van-cell>
          <van-cell title="车辆ID" :value="currentItem.vehicleId || '-'" />
          <van-cell title="批次编码" :value="currentItem.batchCode || '-'" />
          <van-cell title="初始温度">
            <template #value>
              <span
                class="nums-tabular"
                :style="{ color: tempColor(currentItem.startTemp) }"
              >
                {{ formatTemp(currentItem.startTemp) }}
              </span>
            </template>
          </van-cell>
          <van-cell title="目标温度">
            <template #value>
              <span
                class="nums-tabular"
                :style="{ color: 'var(--color-temp-normal)' }"
              >
                {{ formatTemp(currentItem.targetTemp) }}
              </span>
            </template>
          </van-cell>
          <van-cell title="开始时间" :value="formatTime(currentItem.startTime)" />
          <van-cell title="结束时间" :value="formatTime(currentItem.endTime)" />
          <van-cell title="耗时">
            <template #value>
              <span class="nums-tabular">
                {{ currentItem.duration != null ? `${currentItem.duration} 分钟` : '-' }}
              </span>
            </template>
          </van-cell>
          <van-cell title="操作员" :value="currentItem.operator || '-'" />
          <van-cell title="备注" :value="currentItem.remark || '-'" />
          <van-cell title="创建时间" :value="formatTime(currentItem.createTime)" />
        </van-cell-group>

        <!-- State Transition Actions -->
        <div class="status-actions">
          <p class="status-title">状态操作</p>
          <van-button
            v-if="currentItem.status === 'PENDING'"
            block
            type="primary"
            plain
            :loading="transitioning"
            @click="handleStartCooling"
          >
            开始冷却
          </van-button>
          <van-button
            v-if="currentItem.status === 'COOLING'"
            block
            type="success"
            plain
            :loading="transitioning"
            @click="handleCompleteCooling"
          >
            完成冷却
          </van-button>
          <van-cell
            v-if="currentItem.status !== 'PENDING' && currentItem.status !== 'COOLING'"
            title=""
            value="无可执行操作"
            class="no-action-cell"
          />
        </div>
      </template>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { coldchainApi } from '@/api/coldchain.js'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAddDrawer = ref(false)
const showDetail = ref(false)
const submitting = ref(false)
const transitioning = ref(false)
const detailLoading = ref(false)
const currentItem = ref(null)

const query = reactive({ status: '', vehicleId: '', page: 1, size: 10 })
const form = reactive({
  vehicleId: '',
  batchCode: '',
  startTemp: '',
  targetTemp: '',
  operator: '',
  remark: '',
})

// --- Status maps ---

function statusTagType(status) {
  const map = {
    PENDING: 'warning',
    COOLING: 'primary',
    COMPLETED: 'success',
    FAILED: 'danger',
  }
  return map[status] || 'default'
}

function statusLabel(status) {
  const map = {
    PENDING: '待处理',
    COOLING: '冷却中',
    COMPLETED: '已完成',
    FAILED: '失败',
  }
  return map[status] || status || '-'
}

// --- Formatting ---

function formatTemp(temp) {
  return temp != null && temp !== '' ? `${temp}℃` : '-'
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

/**
 * Temperature color based on coldchain IoT palette.
 * Normal <= 8, Warning 8~15, Critical > 15.
 */
function tempColor(temp) {
  if (temp == null || temp === '') return ''
  const t = Number(temp)
  if (t <= 8) return 'var(--color-temp-normal)'
  if (t <= 15) return 'var(--color-temp-warning)'
  return 'var(--color-temp-critical)'
}

// --- List operations ---

async function loadList() {
  loading.value = true
  try {
    const params = { ...query, page: 1 }
    // Remove empty strings so backend doesn't filter on them
    Object.keys(params).forEach(k => {
      if (params[k] === '' || params[k] == null) delete params[k]
    })
    const res = await coldchainApi.getPrecooling(params)
    const records = res?.records || res || []
    list.value = records
    finished.value = records.length >= (res?.total || records.length)
    query.page = 1
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function loadMore() {
  if (finished.value) return
  query.page += 1
  try {
    const params = { ...query }
    Object.keys(params).forEach(k => {
      if (params[k] === '' || params[k] == null) delete params[k]
    })
    const res = await coldchainApi.getPrecooling(params)
    const records = res?.records || []
    list.value = [...list.value, ...records]
    if (list.value.length >= (res?.total || 0) || records.length < query.size) {
      finished.value = true
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  finished.value = false
  list.value = []
  loadList()
}

// --- Form operations ---

function resetForm() {
  Object.assign(form, {
    vehicleId: '',
    batchCode: '',
    startTemp: '',
    targetTemp: '',
    operator: '',
    remark: '',
  })
}

function openAddDrawer() {
  resetForm()
  showAddDrawer.value = true
}

async function handleCreate() {
  submitting.value = true
  try {
    const payload = { ...form }
    // Parse numeric fields
    if (payload.startTemp !== '') payload.startTemp = Number(payload.startTemp)
    if (payload.targetTemp !== '') payload.targetTemp = Number(payload.targetTemp)
    else payload.targetTemp = 2.0

    await coldchainApi.createPrecooling(payload)
    showToast({ type: 'success', message: '预冷任务已创建' })
    showAddDrawer.value = false
    loadList()
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '创建失败' })
  } finally {
    submitting.value = false
  }
}

// --- Detail operations ---

async function openDetailDrawer(item) {
  currentItem.value = item
  showDetail.value = true
  // Fetch full detail from backend
  detailLoading.value = true
  try {
    const res = await coldchainApi.getPrecoolTask(item.id)
    currentItem.value = res?.data || res || item
  } catch {
    // Fallback to list item data
  } finally {
    detailLoading.value = false
  }
}

async function handleStartCooling() {
  if (!currentItem.value) return
  try {
    await showConfirmDialog({
      title: '确认操作',
      message: `确定开始冷却任务「${currentItem.value.taskNo || currentItem.value.id}」？`,
    })
    transitioning.value = true
    await coldchainApi.startPrecooling(currentItem.value.id)
    showToast({ type: 'success', message: '已开始冷却' })
    showDetail.value = false
    loadList()
  } catch (e) {
    if (e !== 'cancel') {
      showToast({ type: 'fail', message: e?.message || '操作失败' })
    }
  } finally {
    transitioning.value = false
  }
}

async function handleCompleteCooling() {
  if (!currentItem.value) return
  try {
    await showConfirmDialog({
      title: '确认操作',
      message: `确定完成任务「${currentItem.value.taskNo || currentItem.value.id}」？`,
    })
    transitioning.value = true
    await coldchainApi.completePrecooling(currentItem.value.id)
    showToast({ type: 'success', message: '冷却已完成' })
    showDetail.value = false
    loadList()
  } catch (e) {
    if (e !== 'cancel') {
      showToast({ type: 'fail', message: e?.message || '操作失败' })
    }
  } finally {
    transitioning.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.precooling-page {
  padding-bottom: var(--space-5);
}

.filter-bar {
  background: var(--color-surface-raised);
  margin-bottom: var(--space-2);
}

.action-bar {
  display: flex;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
}

.list-item {
  margin-bottom: var(--space-2);
}

.cell-label-text {
  font-size: var(--font-size-body-s);
  color: var(--color-text-secondary);
}

.cell-meta-text {
  font-size: var(--font-size-caption);
  color: var(--color-text-tertiary);
}

/* --- Drawer --- */

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  font-size: var(--font-size-body);
  font-weight: 600;
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border-default);
}

.drawer-form {
  padding-bottom: var(--space-5);
}

.drawer-actions {
  padding: var(--space-4);
}

.drawer-loading {
  display: flex;
  justify-content: center;
  padding: var(--space-8) 0;
}

/* --- Status actions --- */

.status-actions {
  padding: var(--space-4);
  border-top: 1px solid var(--color-border-default);
  margin-top: var(--space-2);
}

.status-title {
  font-size: var(--font-size-caption);
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-3);
}

.no-action-cell {
  color: var(--color-text-tertiary);
}
</style>
