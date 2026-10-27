<template>
  <div class="anomaly-page">
    <!-- Filter Bar: Status Tabs + Severity Dropdown -->
    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="待处理" name="OPEN" />
        <van-tab title="调查中" name="INVESTIGATING" />
        <van-tab title="已解决" name="RESOLVED" />
      </van-tabs>
      <div class="severity-filter">
        <van-dropdown-menu>
          <van-dropdown-item v-model="query.severity" :options="severityOptions" @change="handleSearch" />
        </van-dropdown-menu>
      </div>
    </div>

    <!-- Action Bar -->
    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openReportDrawer">上报异常</van-button>
    </div>

    <!-- List -->
    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadMore"
      >
        <van-cell-group inset style="margin-bottom: var(--space-2)" v-for="item in list" :key="item.id">
          <van-cell is-link @click="openDetailDrawer(item)">
            <template #title>
              <span>{{ anomalyTypeLabel(item.anomalyType) }}</span>
            </template>
            <template #label>
              <div class="card-meta">
                <span>溯源码: {{ item.traceCode || '-' }}</span>
                <span class="card-time">{{ item.createTime || '-' }}</span>
              </div>
            </template>
            <template #value>
              <div class="card-tags">
                <van-tag :type="severityTagType(item.severity)" size="medium">
                  {{ severityLabel(item.severity) }}
                </van-tag>
                <van-tag :type="statusTagType(item.status)" size="medium" style="margin-left: 4px">
                  {{ statusLabel(item.status) }}
                </van-tag>
              </div>
            </template>
          </van-cell>
          <van-cell style="padding-top: 0">
            <template #title>
              <span class="card-desc">{{ item.description || '-' }}</span>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无异常记录" />
      </van-list>
    </van-pull-refresh>

    <!-- Report Anomaly Drawer -->
    <van-popup
      v-model:show="showReportDrawer"
      position="bottom"
      round
      :style="{ maxHeight: '90vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>上报异常</span>
        <van-icon name="cross" @click="showReportDrawer = false" />
      </div>
      <van-form @submit="handleReport" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.traceCode"
            label="溯源码"
            placeholder="请输入关联溯源码"
            :rules="[{ required: true, message: '请填写溯源码' }]"
          />
          <van-field
            v-model="form.batchId"
            label="批次ID"
            placeholder="请输入关联批次ID"
          />
          <van-field
            v-model="form.anomalyType"
            is-link
            readonly
            label="异常类型"
            placeholder="请选择异常类型"
            :rules="[{ required: true, message: '请选择异常类型' }]"
            @click="showTypePicker = true"
          />
          <van-field
            v-model="form.severity"
            is-link
            readonly
            label="严重程度"
            placeholder="请选择严重程度"
            :rules="[{ required: true, message: '请选择严重程度' }]"
            @click="showSeverityPicker = true"
          />
          <van-field
            v-model="form.description"
            label="异常描述"
            type="textarea"
            rows="3"
            autosize
            placeholder="请描述异常情况"
            :rules="[{ required: true, message: '请填写异常描述' }]"
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
          <van-button block type="primary" native-type="submit" :loading="submitting">提交上报</van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- Type Picker -->
    <van-popup v-model:show="showTypePicker" position="bottom" round>
      <van-picker
        :columns="anomalyTypeColumns"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>

    <!-- Severity Picker -->
    <van-popup v-model:show="showSeverityPicker" position="bottom" round>
      <van-picker
        :columns="severityColumns"
        @confirm="onSeverityConfirm"
        @cancel="showSeverityPicker = false"
      />
    </van-popup>

    <!-- Detail Drawer -->
    <van-popup
      v-model:show="showDetail"
      position="bottom"
      round
      :style="{ maxHeight: '90vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>异常详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="异常编号" :value="currentItem.traceCode || `#${currentItem.id}`" />
        <van-cell title="批次ID" :value="currentItem.batchId || '-'" />
        <van-cell title="异常类型" :value="anomalyTypeLabel(currentItem.anomalyType)" />
        <van-cell title="异常描述" :value="currentItem.description || '-'" />
        <van-cell title="严重程度">
          <template #value>
            <van-tag :type="severityTagType(currentItem.severity)">
              {{ severityLabel(currentItem.severity) }}
            </van-tag>
          </template>
        </van-cell>
        <van-cell title="当前状态">
          <template #value>
            <van-tag :type="statusTagType(currentItem.status)">
              {{ statusLabel(currentItem.status) }}
            </van-tag>
          </template>
        </van-cell>
        <van-cell title="影响批次" :value="String(currentItem.affectedBatchCount ?? '-')" />
        <van-cell title="影响果品数" :value="String(currentItem.affectedFruitCount ?? '-')" />
        <van-cell title="根因分析" :value="currentItem.rootCauseAnalysis || '-'" />
        <van-cell title="解决人" :value="currentItem.resolvedBy || '-'" />
        <van-cell title="解决时间" :value="currentItem.resolvedTime || '-'" />
        <van-cell title="备注" :value="currentItem.remark || '-'" />
        <van-cell title="上报时间" :value="currentItem.createTime || '-'" />
      </van-cell-group>

      <!-- Impact Analysis -->
      <div class="impact-section" v-if="currentItem">
        <div class="section-title">影响范围分析</div>
        <div class="impact-stats" v-if="impactData">
          <div class="impact-item">
            <span class="impact-num nums-tabular">{{ impactData.affectedBatchCount ?? '-' }}</span>
            <span class="impact-label">受影响批次</span>
          </div>
          <div class="impact-item">
            <span class="impact-num nums-tabular">{{ impactData.affectedFruitCount ?? '-' }}</span>
            <span class="impact-label">受影响果品</span>
          </div>
        </div>
        <van-loading v-else-if="impactLoading" size="24px" vertical style="padding: var(--space-4)">加载中...</van-loading>
        <van-button
          block
          type="default"
          size="small"
          plain
          :loading="impactLoading"
          @click="loadImpact"
          style="margin-top: var(--space-2)"
        >
          查询影响范围
        </van-button>
      </div>

      <!-- Status Actions -->
      <div class="status-actions" v-if="currentItem">
        <p class="status-title">状态操作</p>
        <div class="status-btn-group">
          <van-button
            v-if="currentItem.status === 'OPEN'"
            type="primary"
            size="small"
            plain
            :loading="transitioning"
            @click="handleInvestigate"
          >
            开始调查
          </van-button>
          <van-button
            v-if="currentItem.status === 'INVESTIGATING'"
            type="success"
            size="small"
            plain
            :loading="transitioning"
            @click="showResolveDialog = true"
          >
            解决异常
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- Resolve Dialog -->
    <van-dialog
      v-model:show="showResolveDialog"
      title="解决异常"
      show-cancel-button
      :before-close="handleResolve"
    >
      <div class="resolve-form">
        <van-field
          v-model="resolveForm.rootCause"
          label="根因分析"
          type="textarea"
          rows="3"
          autosize
          placeholder="请输入根因分析"
        />
        <van-field
          v-model="resolveForm.resolvedBy"
          label="解决人"
          placeholder="请输入解决人姓名"
        />
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { traceApi } from '@/api/trace.js'
import { showToast } from 'vant'

// ----- List State -----
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const query = reactive({ status: '', severity: '', page: 1, size: 10 })

// ----- Drawer State -----
const showReportDrawer = ref(false)
const showDetail = ref(false)
const submitting = ref(false)
const transitioning = ref(false)
const currentItem = ref(null)

// ----- Picker State -----
const showTypePicker = ref(false)
const showSeverityPicker = ref(false)

// ----- Impact State -----
const impactData = ref(null)
const impactLoading = ref(false)

// ----- Resolve Dialog -----
const showResolveDialog = ref(false)
const resolveForm = reactive({ rootCause: '', resolvedBy: '' })

// ----- Form -----
const form = reactive({
  traceCode: '',
  batchId: '',
  anomalyType: '',
  severity: '',
  description: '',
  remark: ''
})

// ----- Options -----
const anomalyTypeColumns = [
  { text: '农药残留超标', value: 'PESTICIDE_EXCESS' },
  { text: '温度违规', value: 'TEMP_VIOLATION' },
  { text: '质量不合格', value: 'QUALITY_FAIL' },
  { text: '其他', value: 'OTHER' }
]

const severityOptions = [
  { text: '全部严重程度', value: '' },
  { text: '高', value: 'HIGH' },
  { text: '中', value: 'MEDIUM' },
  { text: '低', value: 'LOW' }
]

const severityColumns = [
  { text: '高', value: 'HIGH' },
  { text: '中', value: 'MEDIUM' },
  { text: '低', value: 'LOW' }
]

// ----- Label Mappers -----
function anomalyTypeLabel(type) {
  const map = {
    PESTICIDE_EXCESS: '农药残留超标',
    TEMP_VIOLATION: '温度违规',
    QUALITY_FAIL: '质量不合格',
    OTHER: '其他'
  }
  return map[type] || type || '-'
}

function severityLabel(severity) {
  const map = { HIGH: '高', MEDIUM: '中', LOW: '低' }
  return map[severity] || severity || '-'
}

function statusLabel(status) {
  const map = { OPEN: '待处理', INVESTIGATING: '调查中', RESOLVED: '已解决' }
  return map[status] || status || '-'
}

// ----- Tag Type Mappers -----
function severityTagType(severity) {
  const map = { HIGH: 'danger', MEDIUM: 'warning', LOW: 'primary' }
  return map[severity] || 'default'
}

function statusTagType(status) {
  const map = { OPEN: 'warning', INVESTIGATING: 'primary', RESOLVED: 'success' }
  return map[status] || 'default'
}

// ----- Data Loading -----
async function loadList() {
  loading.value = true
  try {
    const params = { page: 1, size: query.size }
    if (query.status) params.status = query.status
    if (query.severity) params.severity = query.severity
    const res = await traceApi.getAnomalies(params)
    const records = res?.records || res || []
    list.value = records
    finished.value = records.length >= (res?.total || records.length)
    query.page = 1
  } catch (e) {
    showToast({ type: 'fail', message: '加载失败' })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function loadMore() {
  if (finished.value) return
  query.page += 1
  try {
    const params = { ...query, page: query.page, size: query.size }
    const res = await traceApi.getAnomalies(params)
    const records = res?.records || []
    list.value = [...list.value, ...records]
    if (list.value.length >= (res?.total || 0) || records.length < query.size) {
      finished.value = true
    }
  } catch (e) {
    showToast({ type: 'fail', message: '加载失败' })
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

// ----- Report Form -----
function resetForm() {
  Object.assign(form, {
    traceCode: '', batchId: '', anomalyType: '', severity: '',
    description: '', remark: ''
  })
}

function openReportDrawer() {
  resetForm()
  showReportDrawer.value = true
}

function onTypeConfirm({ selectedOptions }) {
  const selected = selectedOptions[0]
  form.anomalyType = selected.text
  form._anomalyTypeValue = selected.value
  showTypePicker.value = false
}

function onSeverityConfirm({ selectedOptions }) {
  const selected = selectedOptions[0]
  form.severity = selected.text
  form._severityValue = selected.value
  showSeverityPicker.value = false
}

async function handleReport() {
  submitting.value = true
  try {
    await traceApi.reportAnomaly({
      traceCode: form.traceCode,
      batchId: form.batchId || undefined,
      anomalyType: form._anomalyTypeValue || form.anomalyType,
      severity: form._severityValue || form.severity,
      description: form.description,
      remark: form.remark || undefined
    })
    showToast({ type: 'success', message: '异常已上报' })
    showReportDrawer.value = false
    loadList()
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '上报失败' })
  } finally {
    submitting.value = false
  }
}

// ----- Detail Drawer -----
function openDetailDrawer(item) {
  currentItem.value = item
  impactData.value = null
  impactLoading.value = false
  showDetail.value = true
}

// ----- Impact Analysis -----
async function loadImpact() {
  if (!currentItem.value?.traceCode) {
    showToast({ type: 'fail', message: '缺少溯源码' })
    return
  }
  impactLoading.value = true
  try {
    const res = await traceApi.getAnomalyImpact(currentItem.value.traceCode)
    impactData.value = res || null
  } catch (e) {
    showToast({ type: 'fail', message: '查询失败' })
  } finally {
    impactLoading.value = false
  }
}

// ----- Status Transitions -----
async function handleInvestigate() {
  if (!currentItem.value) return
  transitioning.value = true
  try {
    await traceApi.investigateAnomaly(currentItem.value.id)
    showToast({ type: 'success', message: '已开始调查' })
    showDetail.value = false
    loadList()
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '操作失败' })
  } finally {
    transitioning.value = false
  }
}

async function handleResolve(action, done) {
  if (action === 'confirm') {
    if (!resolveForm.rootCause) {
      showToast({ type: 'fail', message: '请填写根因分析' })
      done(false)
      return
    }
    if (!resolveForm.resolvedBy) {
      showToast({ type: 'fail', message: '请填写解决人' })
      done(false)
      return
    }
    transitioning.value = true
    try {
      await traceApi.resolveAnomaly(currentItem.value.id, {
        rootCause: resolveForm.rootCause,
        resolvedBy: resolveForm.resolvedBy
      })
      showToast({ type: 'success', message: '异常已解决' })
      showDetail.value = false
      resolveForm.rootCause = ''
      resolveForm.resolvedBy = ''
      loadList()
      done()
    } catch (e) {
      showToast({ type: 'fail', message: e?.message || '操作失败' })
      done(false)
    } finally {
      transitioning.value = false
    }
  } else {
    done()
  }
}

onMounted(loadList)
</script>

<style scoped>
.anomaly-page {
  padding-bottom: var(--space-6);
}

.filter-bar {
  background: var(--color-surface-raised);
  margin-bottom: var(--space-1);
}

.severity-filter {
  padding: 0 var(--space-4);
  border-top: 1px solid var(--color-divider);
}

.action-bar {
  display: flex;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
}

.card-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.card-time {
  font-size: var(--font-size-body-s, 12px);
  color: var(--color-text-tertiary);
}

.card-tags {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.card-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Drawer */
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid var(--color-border-default);
}

.drawer-form {
  padding-bottom: var(--space-5);
}

.drawer-actions {
  padding: var(--space-4);
}

/* Impact Section */
.impact-section {
  padding: var(--space-4);
  border-top: 1px solid var(--color-border-default);
  margin-top: var(--space-2);
}

.section-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-bottom: var(--space-2);
}

.impact-stats {
  display: flex;
  gap: var(--space-4);
}

.impact-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  background: var(--color-surface-sunken);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
}

.impact-num {
  font-size: 24px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.impact-label {
  font-size: 13px;
  color: var(--color-text-tertiary);
  margin-top: var(--space-1);
}

/* Status Actions */
.status-actions {
  padding: var(--space-4);
  border-top: 1px solid var(--color-border-default);
  margin-top: var(--space-2);
}

.status-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-bottom: var(--space-3);
}

.status-btn-group {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

/* Resolve Dialog */
.resolve-form {
  padding: var(--space-3) var(--space-4);
}
</style>
