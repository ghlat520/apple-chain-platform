<template>
  <div class="harvest-batch-page">
    <!-- Filter: status tabs -->
    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="草稿" name="DRAFT" />
        <van-tab title="已确认" name="CONFIRMED" />
        <van-tab title="已入库" name="IN_STORAGE" />
      </van-tabs>
    </div>

    <!-- Action bar -->
    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openCreateDrawer">
        新增批次
      </van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">
        导出
      </van-button>
    </div>

    <!-- List with pull-refresh & infinite scroll -->
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
          style="margin-bottom: 8px"
        >
          <van-cell is-link @click="openDetailDrawer(item)">
            <template #title>
              <span class="batch-no">{{ item.batchNo || `批次 #${item.id}` }}</span>
            </template>
            <template #label>
              <div class="cell-label">
                <span>{{ item.harvestDate || '-' }}</span>
                <span class="nums-tabular">{{ item.totalWeight ?? '-' }} kg</span>
              </div>
              <div class="grade-row">
                <span class="grade-item grade-a">甲 {{ item.gradeA ?? 0 }}</span>
                <span class="grade-item grade-b">乙 {{ item.gradeB ?? 0 }}</span>
                <span class="grade-item grade-c">丙 {{ item.gradeC ?? 0 }}</span>
              </div>
            </template>
            <template #value>
              <van-tag :type="statusTagType(item.status)">
                {{ statusLabel(item.status) }}
              </van-tag>
            </template>
          </van-cell>
          <van-cell v-if="item.traceCode" style="padding-top: 0">
            <template #title>
              <span class="trace-code">
                <van-icon name="qr" size="14" />
                溯源码: {{ item.traceCode }}
              </span>
            </template>
          </van-cell>
        </van-cell-group>

        <van-empty v-if="!loading && list.length === 0" description="暂无采收批次" />
      </van-list>
    </van-pull-refresh>

    <!-- Create / Edit Drawer -->
    <van-popup
      v-model:show="showFormDrawer"
      position="bottom"
      round
      :style="{ maxHeight: '90vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>{{ isEditing ? '编辑批次' : '新增批次' }}</span>
        <van-icon name="cross" @click="showFormDrawer = false" />
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
            v-model.number="form.totalWeight"
            label="总重量(kg)"
            type="number"
            placeholder="请输入总重量"
            :rules="[{ required: true, message: '请填写总重量' }]"
          />
          <van-field
            v-model.number="form.gradeA"
            label="甲级(kg)"
            type="number"
            placeholder="甲级苹果重量"
          />
          <van-field
            v-model.number="form.gradeB"
            label="乙级(kg)"
            type="number"
            placeholder="乙级苹果重量"
          />
          <van-field
            v-model.number="form.gradeC"
            label="丙级(kg)"
            type="number"
            placeholder="丙级苹果重量"
          />
          <van-field
            v-model="form.remark"
            label="备注"
            type="textarea"
            rows="2"
            autosize
            placeholder="选填"
          />
        </van-cell-group>
        <div class="drawer-actions">
          <van-button
            block
            type="primary"
            native-type="submit"
            :loading="submitting"
          >
            {{ isEditing ? '保存修改' : '确认创建' }}
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
        <span>批次详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>

      <template v-if="currentItem">
        <van-cell-group inset>
          <van-cell title="批次号" :value="currentItem.batchNo || `#${currentItem.id}`" />
          <van-cell title="状态">
            <template #value>
              <van-tag :type="statusTagType(currentItem.status)">
                {{ statusLabel(currentItem.status) }}
              </van-tag>
            </template>
          </van-cell>
          <van-cell title="果园ID" :value="currentItem.orchardId ?? '-'" />
          <van-cell title="采收日期" :value="currentItem.harvestDate ?? '-'" />
          <van-cell title="总重量(kg)">
            <template #value>
              <span class="nums-tabular">{{ currentItem.totalWeight ?? '-' }}</span>
            </template>
          </van-cell>
          <van-cell title="甲级(kg)">
            <template #value>
              <span class="nums-tabular">{{ currentItem.gradeA ?? '-' }}</span>
            </template>
          </van-cell>
          <van-cell title="乙级(kg)">
            <template #value>
              <span class="nums-tabular">{{ currentItem.gradeB ?? '-' }}</span>
            </template>
          </van-cell>
          <van-cell title="丙级(kg)">
            <template #value>
              <span class="nums-tabular">{{ currentItem.gradeC ?? '-' }}</span>
            </template>
          </van-cell>
          <van-cell title="溯源码" :value="currentItem.traceCode || '未生成'" />
          <van-cell title="备注" :value="currentItem.remark || '-'" />
          <van-cell title="创建时间" :value="currentItem.createTime || '-'" />
          <van-cell title="更新时间" :value="currentItem.updateTime || '-'" />
        </van-cell-group>

        <!-- Actions -->
        <div class="status-actions" v-if="currentItem">
          <p class="status-title">操作</p>
          <div class="status-btn-group">
            <van-button
              v-if="currentItem.status === 'DRAFT'"
              type="success"
              size="small"
              plain
              :loading="confirming"
              @click="handleConfirm(currentItem)"
            >
              确认采收
            </van-button>
            <van-button
              v-if="currentItem.status === 'DRAFT'"
              type="primary"
              size="small"
              plain
              @click="handleEdit(currentItem)"
            >
              编辑
            </van-button>
            <van-button
              v-if="currentItem.status === 'DRAFT'"
              type="danger"
              size="small"
              plain
              @click="handleDelete(currentItem)"
            >
              删除
            </van-button>
            <van-button
              v-if="currentItem.traceCode"
              type="primary"
              size="small"
              plain
              @click="copyTraceCode(currentItem.traceCode)"
            >
              复制溯源码
            </van-button>
          </div>
        </div>
      </template>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { plantingApi } from '@/api/planting.js'
import { showToast, showConfirmDialog } from 'vant'

// ---- List state ----
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

// ---- Query / filter ----
const query = reactive({ status: '', page: 1, size: 10 })

// ---- Form drawer state ----
const showFormDrawer = ref(false)
const showDatePicker = ref(false)
const submitting = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const datePickerValue = ref([])

const emptyForm = () => ({
  orchardId: '',
  harvestDate: new Date().toISOString().slice(0, 10),
  totalWeight: '',
  gradeA: '',
  gradeB: '',
  gradeC: '',
  remark: '',
})
const form = reactive(emptyForm())

// ---- Detail drawer state ----
const showDetail = ref(false)
const currentItem = ref(null)
const confirming = ref(false)

// ---- Status helpers ----
const STATUS_MAP = {
  DRAFT: { label: '草稿', tagType: 'default' },
  CONFIRMED: { label: '已确认', tagType: 'primary' },
  IN_STORAGE: { label: '已入库', tagType: 'success' },
}

function statusLabel(status) {
  return STATUS_MAP[status]?.label || status || '-'
}

function statusTagType(status) {
  return STATUS_MAP[status]?.tagType || 'default'
}

// ---- List loading ----
async function loadList() {
  loading.value = true
  try {
    const res = await plantingApi.getHarvestBatches({ ...query, page: 1 })
    const records = res?.records || res || []
    list.value = records
    finished.value = records.length >= (res?.total || records.length)
    query.page = 1
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '加载失败' })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function loadMore() {
  if (finished.value) return
  query.page += 1
  try {
    const res = await plantingApi.getHarvestBatches({ ...query })
    const records = res?.records || []
    list.value = [...list.value, ...records]
    if (list.value.length >= (res?.total || 0) || records.length < query.size) {
      finished.value = true
    }
  } catch {
    // silently fail on infinite scroll, user can pull-refresh
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

// ---- Create / Edit drawer ----
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
    harvestDate: item.harvestDate ?? '',
    totalWeight: item.totalWeight ?? '',
    gradeA: item.gradeA ?? '',
    gradeB: item.gradeB ?? '',
    gradeC: item.gradeC ?? '',
    remark: item.remark ?? '',
  })
  initDatePicker(form.harvestDate)
  showFormDrawer.value = true
}

function initDatePicker(dateStr) {
  if (dateStr) {
    datePickerValue.value = dateStr.split('-')
  } else {
    const [y, m, d] = new Date().toISOString().slice(0, 10).split('-')
    datePickerValue.value = [y, m, d]
  }
}

function onPickDate({ selectedValues }) {
  form.harvestDate = selectedValues.join('-')
  showDatePicker.value = false
}

async function handleFormSubmit() {
  submitting.value = true
  try {
    if (isEditing.value) {
      await plantingApi.updateHarvestBatch(editingId.value, { ...form })
      showToast({ type: 'success', message: '修改成功' })
    } else {
      await plantingApi.createHarvestBatch({ ...form })
      showToast({ type: 'success', message: '批次已创建' })
    }
    showFormDrawer.value = false
    loadList()
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '操作失败' })
  } finally {
    submitting.value = false
  }
}

// ---- Detail drawer ----
async function openDetailDrawer(item) {
  // Refresh detail from server for latest data
  try {
    const detail = await plantingApi.getHarvestBatch(item.id)
    currentItem.value = typeof detail === 'object' && detail !== null
      ? { ...item, ...detail }
      : item
  } catch {
    // Fallback to list item data
    currentItem.value = item
  }
  showDetail.value = true
}

// ---- Confirm harvest ----
async function handleConfirm(item) {
  try {
    await showConfirmDialog({
      title: '确认采收',
      message: '确认后将生成溯源码，不可撤回。是否继续？',
    })
    confirming.value = true
    const result = await plantingApi.confirmHarvestBatch(item.id)
    showToast({ type: 'success', message: '采收已确认，溯源码已生成' })
    // Refresh detail to show the new traceCode
    if (result && typeof result === 'object') {
      currentItem.value = { ...currentItem.value, ...result }
    } else {
      const refreshed = await plantingApi.getHarvestBatch(item.id)
      currentItem.value = { ...currentItem.value, ...(refreshed || {}) }
    }
    loadList()
  } catch (e) {
    if (e !== 'cancel') {
      showToast({ type: 'fail', message: e?.message || '确认失败' })
    }
  } finally {
    confirming.value = false
  }
}

// ---- Delete ----
async function handleDelete(item) {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确认删除批次 ${item.batchNo || `#${item.id}`}？删除后不可恢复。`,
    })
    await plantingApi.deleteHarvestBatch(item.id)
    showToast({ type: 'success', message: '删除成功' })
    showDetail.value = false
    loadList()
  } catch {
    // user cancelled
  }
}

// ---- Copy trace code ----
function copyTraceCode(code) {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(code).then(() => {
      showToast({ type: 'success', message: '溯源码已复制' })
    })
  } else {
    // Fallback for older browsers
    const textarea = document.createElement('textarea')
    textarea.value = code
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    showToast({ type: 'success', message: '溯源码已复制' })
  }
}

// ---- Export ----
async function handleExport() {
  showToast('正在导出...')
  try {
    await plantingApi.exportHarvestBatches()
  } catch {
    showToast({ type: 'fail', message: '导出失败' })
  }
}

// ---- Init ----
import { onMounted } from 'vue'
onMounted(loadList)
</script>

<style scoped>
.harvest-batch-page {
  padding-bottom: var(--space-8, 32px);
  background: var(--color-surface-base, #FAFAF8);
  min-height: 100vh;
}

/* Filter bar */
.filter-bar {
  background: var(--color-surface-raised, #fff);
  margin-bottom: 4px;
}

/* Action bar */
.action-bar {
  display: flex;
  gap: var(--space-2, 8px);
  padding: var(--space-2, 8px) var(--space-4, 16px);
}

/* Cell content */
.batch-no {
  font-weight: 500;
  font-size: var(--typography-body-size, 16px);
  color: var(--color-text-primary, #2C2E32);
}

.cell-label {
  display: flex;
  justify-content: space-between;
  margin-top: 4px;
  font-size: var(--typography-body-s-size, 14px);
  color: var(--color-text-secondary, #5E6368);
}

.grade-row {
  display: flex;
  gap: var(--space-3, 12px);
  margin-top: 4px;
  font-size: var(--typography-caption-size, 13px);
}

.grade-item {
  color: var(--color-text-tertiary, #8A9099);
}

.grade-a { color: var(--color-semantic-success, #43A047); }
.grade-b { color: var(--color-semantic-warning, #F57C00); }
.grade-c { color: var(--color-semantic-info, #1976D2); }

.trace-code {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: var(--typography-caption-size, 13px);
  color: var(--color-text-secondary, #5E6368);
  font-family: var(--font-mono, 'JetBrains Mono', monospace);
}

/* Drawer shared */
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4, 16px);
  font-size: var(--typography-h3-size, 16px);
  font-weight: 600;
  border-bottom: 1px solid var(--color-border-default, rgba(44, 46, 50, 0.08));
}

.drawer-form {
  padding-bottom: var(--space-5, 20px);
}

.drawer-actions {
  padding: var(--space-4, 16px);
}

/* Status actions */
.status-actions {
  padding: var(--space-4, 16px);
  border-top: 1px solid var(--color-border-default, rgba(44, 46, 50, 0.08));
  margin-top: var(--space-2, 8px);
}

.status-title {
  font-size: var(--typography-caption-size, 13px);
  color: var(--color-text-tertiary, #8A9099);
  margin-bottom: var(--space-3, 12px);
}

.status-btn-group {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2, 8px);
}

/* Tabular numbers for alignment */
.nums-tabular {
  font-variant-numeric: tabular-nums;
  font-family: var(--font-mono, 'JetBrains Mono', monospace);
}
</style>
