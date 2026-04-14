<template>
  <div class="harvest-batch-page" data-density="comfortable">
    <!-- Search bar -->
    <van-search
      v-model="query.keyword"
      placeholder="搜索品种 / 果园名称"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <!-- Status filter tabs -->
    <div class="filter-bar">
      <van-tabs v-model:active="activeStatus" @change="onStatusChange" scrollable>
        <van-tab title="全部" :name="''" />
        <van-tab title="草稿" :name="1" />
        <van-tab title="已确认" :name="2" />
      </van-tabs>
    </div>

    <!-- Action bar -->
    <div class="action-bar">
      <van-button
        type="primary"
        size="small"
        icon="plus"
        @click="openCreateDrawer"
      >
        新增批次
      </van-button>
      <van-button
        type="default"
        size="small"
        icon="down"
        @click="handleExport"
      >
        导出
      </van-button>
    </div>

    <!-- Error state -->
    <div v-if="errorMsg" class="error-banner" role="alert">
      <span>{{ errorMsg }}</span>
      <van-button type="default" size="mini" @click="retryLoad">重试</van-button>
    </div>

    <!-- List with pull-refresh & infinite scroll -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadMore"
      >
        <!-- Loading skeleton -->
        <template v-if="loading && list.length === 0">
          <van-cell-group inset style="margin-bottom: var(--space-2)" v-for="n in 3" :key="`sk-${n}`">
            <van-skeleton :row="3" />
          </van-cell-group>
        </template>

        <!-- Batch cards -->
        <van-cell-group
          v-for="item in list"
          :key="item.id"
          inset
          class="batch-card"
        >
          <van-cell is-link @click="openDetailDrawer(item)">
            <template #title>
              <div class="batch-title-row">
                <span class="batch-id nums-tabular">批次 #{{ item.id }}</span>
                <van-tag
                  :color="statusColor(item.status)"
                  :text-color="statusTextColor(item.status)"
                  size="small"
                  class="status-tag"
                >
                  {{ statusDesc(item.status) }}
                </van-tag>
              </div>
            </template>
            <template #label>
              <div class="batch-meta">
                <span class="meta-row">
                  <van-icon name="shop-o" size="14" />
                  {{ item.orchardName || '-' }}
                </span>
                <span class="meta-row">
                  <van-icon name="flower-o" size="14" />
                  品种：{{ item.variety || '-' }}
                </span>
                <div class="meta-numbers">
                  <span class="meta-row">
                    <van-icon name="balance-o" size="14" />
                    <span class="nums-tabular">{{ formatQuantity(item.quantity) }}</span>
                  </span>
                  <span class="meta-row grade-badge" :class="`grade-${(item.grade || '').toLowerCase()}`">
                    {{ item.grade ? `${item.grade} 级` : '未评级' }}
                  </span>
                </div>
                <span class="meta-row date-row">
                  <van-icon name="calendar-o" size="14" />
                  采收日期：{{ item.harvestDate || '-' }}
                </span>
              </div>
              <!-- Trace code row -->
              <div v-if="item.traceCode" class="trace-code-row">
                <van-icon name="qr" size="14" />
                <span class="trace-code nums-tabular">{{ item.traceCode }}</span>
              </div>
            </template>
          </van-cell>
        </van-cell-group>

        <!-- Empty state -->
        <div v-if="!loading && !errorMsg && list.length === 0" class="empty-wrap">
          <van-empty
            image="search"
            :description="query.keyword || activeStatus !== '' ? '未找到匹配的采收批次' : '暂无采收批次。点击右上方「新增批次」开始。'"
          />
        </div>
      </van-list>
    </van-pull-refresh>

    <!-- FAB: 新增 -->
    <van-button
      class="fab-btn"
      round
      type="primary"
      icon="plus"
      aria-label="新增采收批次"
      @click="openCreateDrawer"
    />

    <!-- Create / Edit Popup -->
    <van-popup
      v-model:show="showFormDrawer"
      position="bottom"
      round
      :style="{ maxHeight: '92vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span class="drawer-title">{{ isEditing ? '编辑批次' : '新增批次' }}</span>
        <van-icon
          name="cross"
          size="20"
          class="drawer-close"
          role="button"
          aria-label="关闭"
          tabindex="0"
          @click="showFormDrawer = false"
          @keydown.enter="showFormDrawer = false"
        />
      </div>

      <van-form @submit="handleFormSubmit" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model.number="form.orchardId"
            label="果园ID"
            type="digit"
            placeholder="请输入果园ID"
            :rules="[{ required: true, message: '请填写果园ID' }]"
          />
          <van-field
            v-model="form.variety"
            label="品种"
            placeholder="请输入苹果品种，如：红富士"
            :rules="[{ required: true, message: '请填写品种' }]"
          />
          <van-field
            v-model.number="form.quantity"
            label="采收数量 (kg)"
            type="number"
            placeholder="请输入采收重量"
            :rules="[{ required: true, message: '请填写采收数量' }, { validator: v => v > 0, message: '数量必须大于 0' }]"
          />
          <van-field label="质量等级">
            <template #input>
              <van-radio-group v-model="form.grade" direction="horizontal" class="grade-radio-group">
                <van-radio name="">未评级</van-radio>
                <van-radio name="A">A 级</van-radio>
                <van-radio name="B">B 级</van-radio>
                <van-radio name="C">C 级</van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field
            v-model="form.harvestDate"
            label="采收日期"
            is-link
            readonly
            placeholder="请选择采收日期"
            :rules="[{ required: true, message: '请选择采收日期' }]"
            @click="showDatePicker = true"
          />
          <van-popup v-model:show="showDatePicker" position="bottom" round>
            <van-date-picker
              v-model="datePickerValue"
              title="选择采收日期"
              @confirm="onPickDate"
              @cancel="showDatePicker = false"
            />
          </van-popup>
          <van-field
            v-model="form.remark"
            label="备注"
            type="textarea"
            rows="2"
            autosize
            placeholder="选填，最多 500 字"
            :maxlength="500"
          />
        </van-cell-group>

        <div class="drawer-actions">
          <van-button
            block
            type="primary"
            native-type="submit"
            :loading="submitting"
            :disabled="submitting"
          >
            {{ isEditing ? '保存修改' : '确认创建' }}
          </van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- Detail Popup -->
    <van-popup
      v-model:show="showDetail"
      position="bottom"
      round
      :style="{ maxHeight: '92vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span class="drawer-title">批次详情</span>
        <van-icon
          name="cross"
          size="20"
          class="drawer-close"
          role="button"
          aria-label="关闭"
          tabindex="0"
          @click="showDetail = false"
          @keydown.enter="showDetail = false"
        />
      </div>

      <template v-if="currentItem">
        <van-cell-group inset>
          <van-cell title="批次ID">
            <template #value>
              <span class="nums-tabular">#{{ currentItem.id }}</span>
            </template>
          </van-cell>
          <van-cell title="状态">
            <template #value>
              <van-tag
                :color="statusColor(currentItem.status)"
                :text-color="statusTextColor(currentItem.status)"
              >
                {{ statusDesc(currentItem.status) }}
              </van-tag>
            </template>
          </van-cell>
          <van-cell title="果园" :value="currentItem.orchardName || '-'" />
          <van-cell title="品种" :value="currentItem.variety || '-'" />
          <van-cell title="质量等级" :value="currentItem.grade ? `${currentItem.grade} 级` : '未评级'" />
          <van-cell title="采收数量 (kg)">
            <template #value>
              <span class="nums-tabular">{{ formatQuantity(currentItem.quantity) }}</span>
            </template>
          </van-cell>
          <van-cell title="采收日期" :value="currentItem.harvestDate || '-'" />
          <van-cell title="溯源码">
            <template #value>
              <span v-if="currentItem.traceCode" class="nums-tabular trace-code">
                {{ currentItem.traceCode }}
              </span>
              <span v-else class="text-tertiary">未生成</span>
            </template>
          </van-cell>
          <van-cell title="备注" :value="currentItem.remark || '-'" />
          <van-cell title="创建时间" :value="currentItem.createdAt || '-'" />
          <van-cell title="更新时间" :value="currentItem.updatedAt || '-'" />
        </van-cell-group>

        <!-- Action buttons -->
        <div class="detail-actions">
          <p class="action-label">操作</p>
          <div class="action-btn-group">
            <!-- Only DRAFT(code=1) batches can be edited/confirmed/deleted -->
            <template v-if="statusCode(currentItem.status) === 1">
              <van-button
                type="primary"
                size="small"
                plain
                :loading="confirming"
                :disabled="confirming"
                @click="handleConfirm(currentItem)"
              >
                确认采收
              </van-button>
              <van-button
                type="default"
                size="small"
                plain
                @click="handleEdit(currentItem)"
              >
                编辑
              </van-button>
              <van-button
                type="danger"
                size="small"
                plain
                @click="handleDelete(currentItem)"
              >
                删除此批次
              </van-button>
            </template>

            <!-- Trace code copy (available once confirmed) -->
            <van-button
              v-if="currentItem.traceCode"
              type="default"
              size="small"
              plain
              @click="copyTraceCode(currentItem.traceCode)"
            >
              复制溯源码
            </van-button>
          </div>
        </div>
      </template>

      <!-- Detail loading skeleton -->
      <template v-else>
        <van-cell-group inset>
          <van-skeleton :row="8" />
        </van-cell-group>
      </template>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { plantingApi } from '@/api/planting.js'
import { showToast, showConfirmDialog } from 'vant'

// ============ List state ============
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const errorMsg = ref('')

// ============ Query / filter ============
// status is sent as number per contract (1=DRAFT, 2=CONFIRMED); '' means all
const activeStatus = ref('')
const query = reactive({ keyword: '', status: undefined, page: 1, size: 10 })

// ============ Form drawer state ============
const showFormDrawer = ref(false)
const showDatePicker = ref(false)
const submitting = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const datePickerValue = ref([])

const emptyForm = () => ({
  orchardId: '',
  variety: '',
  quantity: '',
  grade: '',
  harvestDate: todayStr(),
  remark: '',
})
const form = reactive(emptyForm())

// ============ Detail drawer state ============
const showDetail = ref(false)
const currentItem = ref(null)
const confirming = ref(false)

// ============ Helpers ============

function todayStr() {
  return new Date().toISOString().slice(0, 10)
}

function formatQuantity(qty) {
  if (qty == null) return '-'
  return Number(qty).toLocaleString('zh-CN') + ' kg'
}

/**
 * status can be:
 *   - EnumValue<number>: { code: 1, desc: "草稿" }
 *   - raw number: 1 or 2
 *   - null / undefined
 */
function statusCode(status) {
  if (status == null) return null
  if (typeof status === 'object') return status.code
  return status
}

function statusDesc(status) {
  if (status == null) return '-'
  if (typeof status === 'object') return status.desc || '-'
  return status === 1 ? '草稿' : status === 2 ? '已确认' : String(status)
}

// Tag colors per DESIGN.md: no hardcoded hex — use CSS vars through inline style
// DRAFT → info (蓝), CONFIRMED → success (绿)
function statusColor(status) {
  const code = statusCode(status)
  if (code === 1) return 'var(--color-info)'       // 草稿 → 信息蓝
  if (code === 2) return 'var(--color-success)'    // 已确认 → 成功绿
  return 'var(--color-text-tertiary)'
}

function statusTextColor(status) {
  const code = statusCode(status)
  if (code === 1 || code === 2) return 'var(--color-text-inverse)'
  return 'var(--color-text-secondary)'
}

// ============ Load / pagination ============

function buildParams(page) {
  const params = { page, size: query.size }
  if (query.keyword && query.keyword.trim()) params.keyword = query.keyword.trim()
  if (activeStatus.value !== '') params.status = activeStatus.value
  return params
}

async function loadList() {
  errorMsg.value = ''
  loading.value = true
  try {
    const res = await plantingApi.getHarvestBatches(buildParams(1))
    const records = res?.records || []
    list.value = records
    query.page = 1
    finished.value = records.length >= (res?.total ?? records.length)
  } catch (e) {
    errorMsg.value = '加载失败，请检查网络后重试'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function loadMore() {
  if (finished.value) return
  const nextPage = query.page + 1
  try {
    const res = await plantingApi.getHarvestBatches(buildParams(nextPage))
    const records = res?.records || []
    list.value = [...list.value, ...records]
    query.page = nextPage
    if (list.value.length >= (res?.total ?? 0) || records.length < query.size) {
      finished.value = true
    }
  } catch {
    // silent fail on pagination — user can pull-refresh
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  finished.value = false
  list.value = []
  loadList()
}

function onStatusChange(val) {
  activeStatus.value = val
  handleSearch()
}

function onRefresh() {
  finished.value = false
  list.value = []
  loadList()
}

function retryLoad() {
  handleSearch()
}

// ============ Create / Edit ============

function resetForm() {
  isEditing.value = false
  editingId.value = null
  Object.assign(form, emptyForm())
  initDatePicker(form.harvestDate)
}

function openCreateDrawer() {
  resetForm()
  showFormDrawer.value = true
}

function handleEdit(item) {
  showDetail.value = false
  isEditing.value = true
  editingId.value = item.id
  Object.assign(form, {
    orchardId: item.orchardId ?? '',
    variety: item.variety ?? '',
    quantity: item.quantity ?? '',
    grade: item.grade ?? '',
    harvestDate: item.harvestDate ?? todayStr(),
    remark: item.remark ?? '',
  })
  initDatePicker(form.harvestDate)
  showFormDrawer.value = true
}

function initDatePicker(dateStr) {
  if (dateStr) {
    datePickerValue.value = dateStr.split('-')
  } else {
    datePickerValue.value = todayStr().split('-')
  }
}

function onPickDate({ selectedValues }) {
  form.harvestDate = selectedValues.join('-')
  showDatePicker.value = false
}

async function handleFormSubmit() {
  submitting.value = true
  try {
    const payload = {
      orchardId: Number(form.orchardId),
      variety: form.variety,
      quantity: Number(form.quantity),
      harvestDate: form.harvestDate,
    }
    if (form.grade) payload.grade = form.grade
    if (form.remark) payload.remark = form.remark

    if (isEditing.value) {
      // orchardId is not in HarvestBatchUpdateRequest — omit it
      const { orchardId: _, ...updatePayload } = payload
      await plantingApi.updateHarvestBatch(editingId.value, updatePayload)
      showToast({ type: 'success', message: '批次已更新' })
    } else {
      await plantingApi.createHarvestBatch(payload)
      showToast({ type: 'success', message: '批次已创建' })
    }
    showFormDrawer.value = false
    loadList()
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '操作失败，请重试' })
  } finally {
    submitting.value = false
  }
}

// ============ Detail ============

async function openDetailDrawer(item) {
  currentItem.value = null
  showDetail.value = true
  try {
    const detail = await plantingApi.getHarvestBatch(item.id)
    currentItem.value = detail && typeof detail === 'object' ? detail : item
  } catch {
    currentItem.value = item
  }
}

// ============ Confirm ============

async function handleConfirm(item) {
  try {
    await showConfirmDialog({
      title: '确认采收',
      message: '确认后将生成溯源码，状态不可撤回。是否继续？',
      confirmButtonText: '确认采收',
      cancelButtonText: '取消',
    })
    confirming.value = true
    const result = await plantingApi.confirmHarvestBatch(item.id)
    showToast({ type: 'success', message: '采收已确认，溯源码已生成' })
    // Refresh detail with returned data or re-fetch
    if (result && typeof result === 'object') {
      currentItem.value = { ...currentItem.value, ...result }
    } else {
      try {
        const refreshed = await plantingApi.getHarvestBatch(item.id)
        currentItem.value = { ...currentItem.value, ...(refreshed || {}) }
      } catch { /* keep stale detail */ }
    }
    loadList()
  } catch (e) {
    if (e !== 'cancel') {
      showToast({ type: 'fail', message: e?.message || '确认失败，请重试' })
    }
  } finally {
    confirming.value = false
  }
}

// ============ Delete ============

async function handleDelete(item) {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定要删除此采收批次（#${item.id}）吗？删除后不可恢复。`,
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await plantingApi.deleteHarvestBatch(item.id)
    showToast({ type: 'success', message: '批次已删除' })
    showDetail.value = false
    loadList()
  } catch {
    // user cancelled or error — error toast handled by catch block above if needed
  }
}

// ============ Copy trace code ============

function copyTraceCode(code) {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(code).then(() => {
      showToast({ type: 'success', message: '溯源码已复制' })
    }).catch(() => fallbackCopy(code))
  } else {
    fallbackCopy(code)
  }
}

function fallbackCopy(text) {
  const el = document.createElement('textarea')
  el.value = text
  el.setAttribute('readonly', '')
  el.style.position = 'absolute'
  el.style.left = '-100vmin'
  document.body.appendChild(el)
  el.select()
  document.execCommand('copy')
  document.body.removeChild(el)
  showToast({ type: 'success', message: '溯源码已复制' })
}

// ============ Export ============

async function handleExport() {
  showToast('正在准备导出...')
  try {
    await plantingApi.exportHarvestBatches()
  } catch {
    showToast({ type: 'fail', message: '导出失败，请重试' })
  }
}

// ============ Init ============
onMounted(loadList)
</script>

<style scoped>
/* ======================================
   Page Shell
====================================== */
.harvest-batch-page {
  padding-bottom: 80px; /* space for FAB */
  background: var(--color-surface-base);
  min-height: 100vh;
}

/* ======================================
   Search & Filter
====================================== */
.filter-bar {
  background: var(--color-surface-raised);
  border-bottom: 1px solid var(--color-divider);
  margin-bottom: var(--space-1);
}

/* ======================================
   Action bar
====================================== */
.action-bar {
  display: flex;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
}

/* ======================================
   Error banner
====================================== */
.error-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: var(--space-2) var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--color-warning-bg);
  border-left: var(--border-width-strong) solid var(--color-warning);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-body-s);
  color: var(--color-text-secondary);
}

/* ======================================
   Batch card
====================================== */
.batch-card {
  margin-bottom: var(--space-2);
}

.batch-title-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.batch-id {
  font-weight: 500;
  font-size: var(--font-size-body);
  color: var(--color-text-primary);
}

.status-tag {
  flex-shrink: 0;
}

/* Batch metadata */
.batch-meta {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  margin-top: var(--space-1);
}

.meta-row {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-size-body-s);
  color: var(--color-text-secondary);
}

.meta-numbers {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.date-row {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-caption);
}

/* Grade badge */
.grade-badge {
  font-size: var(--font-size-overline);
  font-weight: 500;
  padding: 1px var(--space-2);
  border-radius: var(--radius-sm);
  background: var(--color-surface-sunken);
  color: var(--color-text-tertiary);
}

.grade-a {
  background: var(--color-leaf-green-light);
  color: var(--color-leaf-green);
}

.grade-b {
  background: var(--color-warning-bg);
  color: var(--color-warning);
}

.grade-c {
  background: var(--color-info-bg);
  color: var(--color-info);
}

/* Trace code row */
.trace-code-row {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-top: var(--space-1);
  padding-top: var(--space-1);
  border-top: 1px solid var(--color-divider);
}

.trace-code {
  font-family: var(--font-mono);
  font-size: var(--font-size-caption);
  color: var(--color-text-secondary);
}

/* Tabular numbers */
.nums-tabular {
  font-variant-numeric: tabular-nums;
  font-family: var(--font-mono);
}

/* ======================================
   Empty state
====================================== */
.empty-wrap {
  padding: var(--space-8) var(--space-4);
}

/* ======================================
   FAB button
====================================== */
.fab-btn {
  position: fixed;
  bottom: calc(env(safe-area-inset-bottom, 0px) + var(--fab-bottom-offset));
  right: var(--space-4);
  width: var(--fab-size);
  height: var(--fab-size);
  box-shadow: var(--shadow-4);
  z-index: var(--z-sticky);
  transition: transform var(--motion-fast) var(--ease-out),
              background-color var(--motion-fast) var(--ease-out);
}

.fab-btn:hover {
  background-color: var(--color-brand-hover);
  transform: scale(1.04);
}

.fab-btn:active {
  background-color: var(--color-brand-active);
  transform: scale(0.97);
}

.fab-btn:focus-visible {
  outline: var(--outline-width) solid var(--color-brand-primary);
  outline-offset: 3px;
  box-shadow: var(--shadow-focus);
}

/* ======================================
   Drawer shared
====================================== */
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  border-bottom: 1px solid var(--color-border-default);
}

.drawer-title {
  font-size: var(--font-size-body);
  font-weight: 600;
  color: var(--color-text-primary);
}

.drawer-close {
  color: var(--color-text-tertiary);
  cursor: pointer;
  padding: var(--space-1);
  border-radius: var(--radius-sm);
  transition: color var(--motion-fast) var(--ease-out);
}

.drawer-close:hover {
  color: var(--color-text-primary);
}

.drawer-close:focus-visible {
  outline: var(--outline-width) solid var(--color-brand-primary);
  outline-offset: 2px;
}

.drawer-form {
  padding-bottom: var(--space-5);
}

.grade-radio-group {
  padding: var(--space-1) 0;
}

.drawer-actions {
  padding: var(--space-4);
}

/* ======================================
   Detail actions
====================================== */
.detail-actions {
  padding: var(--space-4);
  border-top: 1px solid var(--color-border-default);
  margin-top: var(--space-2);
}

.action-label {
  font-size: var(--font-size-caption);
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-3);
  margin-top: 0;
}

.action-btn-group {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

/* ======================================
   Text utilities
====================================== */
.text-tertiary {
  color: var(--color-text-tertiary);
}

/* ======================================
   Accessibility: reduced-motion
====================================== */
@media (prefers-reduced-motion: reduce) {
  .fab-btn,
  .drawer-close {
    transition: none;
  }
}
</style>
