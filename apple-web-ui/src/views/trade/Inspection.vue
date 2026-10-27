<template>
  <!-- 4 系统态: loading / error / empty / success -->
  <div class="inspection-page">

    <!-- 搜索栏 -->
    <div class="search-bar">
      <van-search
        v-model="searchKeyword"
        placeholder="输入质检单号 / 品种搜索"
        @search="handleSearch"
        @clear="handleSearch"
        shape="round"
      />
    </div>

    <!-- 状态筛选 Tabs -->
    <div class="filter-bar">
      <van-tabs v-model:active="activeTab" @change="handleTabChange" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="待检验" name="PENDING" />
        <van-tab title="已检验" name="INSPECTED" />
        <van-tab title="已验收" name="ACCEPTED" />
        <van-tab title="有争议" name="DISPUTED" />
      </van-tabs>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <van-button
        type="primary"
        size="small"
        icon="plus"
        @click="openAddDrawer"
      >
        新增质检单
      </van-button>
      <van-button
        type="default"
        size="small"
        icon="down"
        @click="handleExport"
        :loading="exporting"
      >
        导出 CSV
      </van-button>
    </div>

    <!-- 加载态 -->
    <div v-if="pageState === 'loading'" class="state-wrapper">
      <van-loading size="32px" color="var(--color-brand-primary)" vertical>加载中…</van-loading>
    </div>

    <!-- 错误态 -->
    <div v-else-if="pageState === 'error'" class="state-wrapper">
      <van-empty image="error" description="加载失败，请重试">
        <van-button type="primary" size="small" @click="loadList">重新加载</van-button>
      </van-empty>
    </div>

    <!-- 列表 (empty / success) -->
    <template v-else>
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="loadMore"
        >
          <van-cell-group
            inset
            v-for="item in list"
            :key="item.id"
            class="card-group"
          >
            <!-- 主行 -->
            <van-cell is-link @click="openDetailDrawer(item)">
              <template #title>
                <span class="card-title nums-tabular">
                  {{ item.inspectionNo || `#${item.id}` }}
                </span>
              </template>
              <template #label>
                <span>
                  品种：{{ item.variety || '-' }}
                  &nbsp;·&nbsp;
                  等级：
                  <van-tag
                    :type="gradeTagType(resolveCode(item.grade))"
                    size="mini"
                    plain
                  >{{ resolveCode(item.grade) || '-' }}</van-tag>
                </span>
              </template>
              <template #value>
                <van-tag :type="statusTagType(resolveCode(item.status))">
                  {{ resolveDesc(item.status) }}
                </van-tag>
              </template>
            </van-cell>

            <!-- 副行 -->
            <van-cell class="card-sub">
              <template #title>
                <span class="meta-text">
                  检验员：{{ item.inspectorName || '-' }}
                  &nbsp;·&nbsp;
                  {{ item.inspectionDate || '-' }}
                </span>
              </template>
              <template #value>
                <van-tag
                  v-if="item.result"
                  :type="resultTagType(resolveCode(item.result))"
                  plain
                  size="mini"
                >
                  {{ resolveDesc(item.result) }}
                </van-tag>
              </template>
            </van-cell>
          </van-cell-group>

          <!-- 空态 -->
          <van-empty
            v-if="!loading && list.length === 0"
            image="search"
            description="暂无质检记录"
          />
        </van-list>
      </van-pull-refresh>
    </template>

    <!-- ==================== 新增质检单 Popup ==================== -->
    <van-popup
      v-model:show="showAddDrawer"
      position="bottom"
      round
      :style="{ maxHeight: '92vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>新增质检单</span>
        <van-icon
          name="cross"
          class="drawer-close"
          role="button"
          tabindex="0"
          @click="showAddDrawer = false"
          @keydown.enter="showAddDrawer = false"
        />
      </div>
      <van-form @submit="handleCreate" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model.number="createForm.orderId"
            label="关联订单 ID"
            type="digit"
            placeholder="请输入关联订单 ID"
            :rules="[{ required: true, message: '请填写关联订单 ID' }]"
            input-align="right"
          />
          <van-field
            v-model.number="createForm.supplyId"
            label="关联供货 ID"
            type="digit"
            placeholder="请输入关联供货 ID"
            input-align="right"
          />
          <van-field
            v-model="createForm.variety"
            label="品种"
            placeholder="如：红富士"
            :rules="[{ required: true, message: '请填写品种' }]"
          />
          <!-- 等级选择器 -->
          <van-field
            v-model="createForm.grade"
            is-link
            readonly
            label="等级"
            placeholder="请选择等级（A / B / C）"
            @click="showGradePicker = true"
          />
          <van-field
            v-model="createForm.inspectionDate"
            label="检验日期"
            placeholder="yyyy-MM-dd，默认当天"
            input-align="right"
          />
        </van-cell-group>
        <div class="drawer-actions">
          <van-button
            block
            type="primary"
            native-type="submit"
            :loading="submitting"
          >
            确认创建
          </van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- 等级 Picker -->
    <van-popup v-model:show="showGradePicker" position="bottom" round>
      <van-picker
        :columns="gradeColumns"
        @confirm="onGradeConfirm"
        @cancel="showGradePicker = false"
      />
    </van-popup>

    <!-- ==================== 详情 Popup ==================== -->
    <van-popup
      v-model:show="showDetail"
      position="bottom"
      round
      :style="{ maxHeight: '92vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>质检详情</span>
        <van-icon
          name="cross"
          class="drawer-close"
          role="button"
          tabindex="0"
          @click="closeDetail"
          @keydown.enter="closeDetail"
        />
      </div>

      <template v-if="currentItem">
        <!-- 状态概览 -->
        <van-cell-group inset class="detail-section">
          <van-cell title="质检单号">
            <template #value>
              <span class="nums-tabular">{{ currentItem.inspectionNo || `#${currentItem.id}` }}</span>
            </template>
          </van-cell>
          <van-cell title="状态">
            <template #value>
              <van-tag :type="statusTagType(resolveCode(currentItem.status))">
                {{ resolveDesc(currentItem.status) }}
              </van-tag>
            </template>
          </van-cell>
          <van-cell title="检验结果" v-if="currentItem.result">
            <template #value>
              <van-tag :type="resultTagType(resolveCode(currentItem.result))" plain>
                {{ resolveDesc(currentItem.result) }}
              </van-tag>
            </template>
          </van-cell>
        </van-cell-group>

        <!-- 基本信息 -->
        <van-cell-group inset class="detail-section" title="基本信息">
          <van-cell title="关联订单 ID">
            <template #value>
              <span class="nums-tabular">{{ currentItem.orderId ?? '-' }}</span>
            </template>
          </van-cell>
          <van-cell title="供货 ID">
            <template #value>
              <span class="nums-tabular">{{ currentItem.supplyId ?? '-' }}</span>
            </template>
          </van-cell>
          <van-cell title="品种" :value="currentItem.variety || '-'" />
          <van-cell title="等级">
            <template #value>
              <van-tag
                v-if="resolveCode(currentItem.grade)"
                :type="gradeTagType(resolveCode(currentItem.grade))"
                plain
              >{{ resolveCode(currentItem.grade) }}</van-tag>
              <span v-else>-</span>
            </template>
          </van-cell>
          <van-cell title="检验员" :value="currentItem.inspectorName || '-'" />
          <van-cell title="检验日期" :value="currentItem.inspectionDate || '-'" />
        </van-cell-group>

        <!-- 检验指标（INSPECTED/ACCEPTED/DISPUTED 才显示） -->
        <van-cell-group
          inset
          class="detail-section"
          title="检验指标"
          v-if="resolveCode(currentItem.status) !== 'PENDING'"
        >
          <van-cell title="糖度 (Brix)">
            <template #value>
              <span class="nums-tabular">
                {{ currentItem.brixValue != null ? `${currentItem.brixValue}` : '-' }}
              </span>
              <van-tag
                v-if="currentItem.brixValue != null"
                :type="currentItem.brixValue >= 12 ? 'success' : 'danger'"
                size="mini"
                plain
                class="threshold-tag"
              >
                {{ currentItem.brixValue >= 12 ? '达标' : '低于阈值' }}
              </van-tag>
            </template>
          </van-cell>
          <van-cell title="硬度">
            <template #value>
              <span class="nums-tabular">
                {{ currentItem.firmnessValue != null ? `${currentItem.firmnessValue}` : '-' }}
              </span>
              <van-tag
                v-if="currentItem.firmnessValue != null"
                :type="currentItem.firmnessValue >= 6 ? 'success' : 'danger'"
                size="mini"
                plain
                class="threshold-tag"
              >
                {{ currentItem.firmnessValue >= 6 ? '达标' : '低于阈值' }}
              </van-tag>
            </template>
          </van-cell>
          <van-cell title="色泽评分">
            <template #value>
              <span class="nums-tabular">
                {{ currentItem.colorScore != null ? `${currentItem.colorScore}` : '-' }}
              </span>
              <van-tag
                v-if="currentItem.colorScore != null"
                :type="currentItem.colorScore >= 70 ? 'success' : 'danger'"
                size="mini"
                plain
                class="threshold-tag"
              >
                {{ currentItem.colorScore >= 70 ? '达标' : '低于阈值' }}
              </van-tag>
            </template>
          </van-cell>
          <van-cell title="缺陷率 (%)">
            <template #value>
              <span class="nums-tabular">
                {{ currentItem.defectRate != null ? `${currentItem.defectRate}%` : '-' }}
              </span>
              <van-tag
                v-if="currentItem.defectRate != null"
                :type="currentItem.defectRate <= 5 ? 'success' : 'danger'"
                size="mini"
                plain
                class="threshold-tag"
              >
                {{ currentItem.defectRate <= 5 ? '达标' : '超标' }}
              </van-tag>
            </template>
          </van-cell>
        </van-cell-group>

        <!-- 其他信息 -->
        <van-cell-group inset class="detail-section" title="其他信息">
          <van-cell title="检验报告" v-if="currentItem.reportUrl">
            <template #value>
              <a :href="currentItem.reportUrl" target="_blank" class="report-link">查看报告</a>
            </template>
          </van-cell>
          <van-cell title="备注" :value="currentItem.remark || '-'" />
          <van-cell title="创建时间" :value="currentItem.createdAt || '-'" />
          <van-cell title="更新时间" :value="currentItem.updatedAt || '-'" />
        </van-cell-group>

        <!-- 状态操作 -->
        <div class="status-actions">
          <p class="status-label">状态操作</p>
          <div class="status-btn-group">
            <van-button
              v-if="resolveCode(currentItem.status) === 'PENDING'"
              type="primary"
              size="small"
              @click="showInspectForm = true"
            >
              录入检验结果
            </van-button>
            <van-button
              v-if="resolveCode(currentItem.status) === 'INSPECTED'"
              type="success"
              size="small"
              :loading="transitioning"
              @click="handleAccept"
            >
              验收通过
            </van-button>
            <van-button
              v-if="resolveCode(currentItem.status) === 'INSPECTED'"
              plain
              size="small"
              class="btn-dispute"
              @click="showDisputeDialog = true"
            >
              发起争议
            </van-button>
          </div>
        </div>

        <!-- 录入检验结果内联表单 -->
        <van-cell-group
          v-if="showInspectForm"
          inset
          class="detail-section inspect-form-section"
          title="录入检验结果"
        >
          <van-form @submit="handleInspect">
            <van-field
              v-model="inspectForm.brixValueStr"
              label="糖度 (Brix)"
              type="decimal"
              placeholder="阈值 ≥ 12"
              :rules="[
                { required: true, message: '请填写糖度' },
                { validator: v => parseFloat(v) > 0, message: '请输入有效数值' }
              ]"
              input-align="right"
            />
            <van-field
              v-model="inspectForm.firmnessValueStr"
              label="硬度"
              type="decimal"
              placeholder="阈值 ≥ 6"
              :rules="[
                { required: true, message: '请填写硬度' },
                { validator: v => parseFloat(v) > 0, message: '请输入有效数值' }
              ]"
              input-align="right"
            />
            <van-field
              v-model="inspectForm.colorScoreStr"
              label="色泽评分"
              type="decimal"
              placeholder="阈值 ≥ 70"
              input-align="right"
            />
            <van-field
              v-model="inspectForm.defectRateStr"
              label="缺陷率 (%)"
              type="decimal"
              placeholder="阈值 ≤ 5"
              input-align="right"
            />
            <van-field
              v-model="inspectForm.inspectorName"
              label="检验员"
              placeholder="请输入检验员姓名"
              :rules="[{ required: true, message: '请填写检验员姓名' }]"
            />
            <van-field
              v-model="inspectForm.reportUrl"
              label="报告链接"
              placeholder="选填"
            />
            <div class="drawer-actions">
              <van-button
                block
                type="primary"
                native-type="submit"
                :loading="submitting"
              >
                提交检验结果
              </van-button>
              <van-button
                block
                plain
                class="btn-cancel"
                @click="showInspectForm = false"
              >
                取消
              </van-button>
            </div>
          </van-form>
        </van-cell-group>
      </template>
    </van-popup>

    <!-- ==================== 争议 Dialog ==================== -->
    <van-dialog
      v-model:show="showDisputeDialog"
      title="发起争议"
      show-cancel-button
      confirm-button-text="提交"
      cancel-button-text="取消"
      :before-close="handleDisputeClose"
    >
      <div class="dispute-body">
        <van-field
          v-model="disputeReason"
          type="textarea"
          rows="3"
          autosize
          placeholder="请输入争议原因（必填）"
        />
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { tradeApi } from '@/api/trade.js'
import { showToast, showConfirmDialog } from 'vant'

// ==================== 状态 ====================
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const pageState = ref('loading')   // 'loading' | 'error' | 'success'
const exporting = ref(false)

const activeTab = ref('')
const searchKeyword = ref('')
const query = reactive({ page: 1, size: 10, status: '', keyword: '' })

// ==================== Drawer 控制 ====================
const showAddDrawer = ref(false)
const showDetail = ref(false)
const showInspectForm = ref(false)
const showGradePicker = ref(false)
const showDisputeDialog = ref(false)
const submitting = ref(false)
const transitioning = ref(false)
const currentItem = ref(null)
const disputeReason = ref('')

// ==================== 表单 ====================
const createForm = reactive({
  orderId: '',
  supplyId: '',
  variety: '',
  grade: '',
  inspectionDate: '',
})

/**
 * 检验结果录入表单
 * 契约 QualityInspectionInspectRequest：服务端自动判定 result，不需要前端传
 * 数值用字符串绑定避免 v-model.number 的 NaN 问题
 */
const inspectForm = reactive({
  brixValueStr: '',
  firmnessValueStr: '',
  colorScoreStr: '',
  defectRateStr: '',
  inspectorName: '',
  reportUrl: '',
})

const gradeColumns = [
  { text: 'A — 优等品', value: 'A' },
  { text: 'B — 一等品', value: 'B' },
  { text: 'C — 二等品', value: 'C' },
]

// ==================== Enum helpers ====================
/**
 * 统一处理 EnumValue<string> 或 string 类型
 * 契约规定 status/result/grade 返回 { code, desc }
 */
function resolveCode(val) {
  if (!val) return ''
  return typeof val === 'object' ? (val.code ?? '') : val
}

function resolveDesc(val) {
  if (!val) return '-'
  return typeof val === 'object' ? (val.desc ?? val.code ?? '-') : val
}

// ---- 状态 ----
const STATUS_TAG = {
  PENDING: 'warning',
  INSPECTED: 'primary',
  ACCEPTED: 'success',
  DISPUTED: 'danger',
}

function statusTagType(code) {
  return STATUS_TAG[code] || 'default'
}

// ---- 结果 ----
const RESULT_TAG = {
  PASS: 'success',
  FAIL: 'danger',
  CONDITIONAL: 'warning',
}

function resultTagType(code) {
  return RESULT_TAG[code] || 'default'
}

// ---- 等级 ----
const GRADE_TAG = {
  A: 'success',   // 绿
  B: 'primary',   // 蓝
  C: 'warning',   // 橙
}

function gradeTagType(code) {
  return GRADE_TAG[code] || 'default'
}

// ==================== 列表加载 ====================
async function loadList() {
  pageState.value = 'loading'
  loading.value = true
  try {
    const params = {
      page: 1,
      size: query.size,
      ...(query.status ? { status: query.status } : {}),
    }
    const res = await tradeApi.getInspections(params)
    const data = res?.data ?? res
    const records = data?.records ?? data ?? []
    list.value = records
    finished.value = records.length >= (data?.total ?? records.length)
    query.page = 1
    pageState.value = 'success'
  } catch {
    pageState.value = 'error'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function loadMore() {
  if (finished.value) return
  query.page += 1
  try {
    const params = {
      page: query.page,
      size: query.size,
      ...(query.status ? { status: query.status } : {}),
    }
    const res = await tradeApi.getInspections(params)
    const data = res?.data ?? res
    const records = data?.records ?? []
    list.value = [...list.value, ...records]
    if (list.value.length >= (data?.total ?? 0) || records.length < query.size) {
      finished.value = true
    }
  } finally {
    loading.value = false
  }
}

function onRefresh() {
  finished.value = false
  list.value = []
  loadList()
}

function handleSearch() {
  finished.value = false
  list.value = []
  loadList()
}

function handleTabChange(name) {
  activeTab.value = name
  query.status = name
  finished.value = false
  list.value = []
  loadList()
}

// ==================== 导出 CSV ====================
async function handleExport() {
  exporting.value = true
  try {
    await tradeApi.exportInspections()
    showToast({ type: 'success', message: '导出成功' })
  } catch {
    showToast({ type: 'fail', message: '导出失败，请重试' })
  } finally {
    exporting.value = false
  }
}

// ==================== 新增 ====================
function openAddDrawer() {
  Object.assign(createForm, {
    orderId: '',
    supplyId: '',
    variety: '',
    grade: '',
    inspectionDate: '',
  })
  showAddDrawer.value = true
}

function onGradeConfirm({ selectedValues }) {
  createForm.grade = selectedValues[0]
  showGradePicker.value = false
}

async function handleCreate() {
  submitting.value = true
  try {
    const payload = {
      orderId: Number(createForm.orderId),
      variety: createForm.variety,
      ...(createForm.supplyId ? { supplyId: Number(createForm.supplyId) } : {}),
      ...(createForm.grade ? { grade: createForm.grade } : {}),
      ...(createForm.inspectionDate ? { inspectionDate: createForm.inspectionDate } : {}),
    }
    await tradeApi.createInspection(payload)
    showToast({ type: 'success', message: '质检单已创建' })
    showAddDrawer.value = false
    onRefresh()
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '创建失败，请重试' })
  } finally {
    submitting.value = false
  }
}

// ==================== 详情 ====================
function openDetailDrawer(item) {
  currentItem.value = item
  showInspectForm.value = false
  showDetail.value = true
}

function closeDetail() {
  showDetail.value = false
  showInspectForm.value = false
}

// ==================== 录入检验结果 ====================
function resetInspectForm() {
  Object.assign(inspectForm, {
    brixValueStr: '',
    firmnessValueStr: '',
    colorScoreStr: '',
    defectRateStr: '',
    inspectorName: '',
    reportUrl: '',
  })
}

async function handleInspect() {
  submitting.value = true
  try {
    const payload = {
      brixValue: parseFloat(inspectForm.brixValueStr),
      firmnessValue: parseFloat(inspectForm.firmnessValueStr),
      inspectorName: inspectForm.inspectorName,
    }
    if (inspectForm.colorScoreStr) payload.colorScore = parseFloat(inspectForm.colorScoreStr)
    if (inspectForm.defectRateStr) payload.defectRate = parseFloat(inspectForm.defectRateStr)
    if (inspectForm.reportUrl) payload.reportUrl = inspectForm.reportUrl

    await tradeApi.inspectResult(currentItem.value.id, payload)
    showToast({ type: 'success', message: '检验结果已提交' })
    showInspectForm.value = false
    showDetail.value = false
    resetInspectForm()
    onRefresh()
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '提交失败，请重试' })
  } finally {
    submitting.value = false
  }
}

// ==================== 验收 ====================
async function handleAccept() {
  try {
    await showConfirmDialog({
      title: '验收确认',
      message: '确认该质检单验收通过？',
      confirmButtonText: '确认验收',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  transitioning.value = true
  try {
    await tradeApi.acceptInspection(currentItem.value.id)
    showToast({ type: 'success', message: '已验收通过' })
    closeDetail()
    onRefresh()
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '操作失败，请重试' })
  } finally {
    transitioning.value = false
  }
}

// ==================== 争议 ====================
async function handleDisputeClose(action) {
  if (action === 'cancel') return true
  if (!disputeReason.value.trim()) {
    showToast({ type: 'fail', message: '请填写争议原因' })
    return false
  }
  try {
    await tradeApi.disputeInspection(currentItem.value.id, disputeReason.value.trim())
    showToast({ type: 'success', message: '争议已发起' })
    disputeReason.value = ''
    closeDetail()
    onRefresh()
    return true
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '操作失败，请重试' })
    return false
  }
}

onMounted(loadList)
</script>

<style scoped>
/* ========== 页面容器 ========== */
.inspection-page {
  padding-bottom: var(--space-8);
  background: var(--color-surface-base);
  min-height: 100vh;
}

/* ========== 搜索栏 ========== */
.search-bar {
  background: var(--color-surface-raised);
  padding: var(--space-2) var(--space-2);
}

/* ========== Tab 筛选 ========== */
.filter-bar {
  background: var(--color-surface-raised);
  border-bottom: 1px solid var(--color-border-default);
  margin-bottom: var(--space-1);
}

/* ========== 操作栏 ========== */
.action-bar {
  display: flex;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  background: var(--color-surface-raised);
  margin-bottom: var(--space-2);
}

/* ========== 系统态 wrapper ========== */
.state-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 40vh;
}

/* ========== 列表卡片 ========== */
.card-group {
  margin-bottom: var(--space-2);
}

.card-title {
  font-size: var(--font-size-body);
  font-weight: 500;
  color: var(--color-text-primary);
}

/* 副行（元数据）— 次要信息 caption 级别 */
.card-sub {
  padding-top: 0;
}

.meta-text {
  font-size: var(--font-size-caption);
  color: var(--color-text-tertiary);
  line-height: 1.4;
}

/* ========== Drawer 通用 ========== */
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  font-size: var(--font-size-body);
  font-weight: 600;
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border-default);
  position: sticky;
  top: 0;
  background: var(--color-surface-raised);
  z-index: 1;
}

.drawer-close {
  color: var(--color-text-secondary);
  font-size: var(--font-size-h2);
  cursor: pointer;
  padding: var(--space-1);
  border-radius: var(--radius-base);
  transition: color var(--motion-fast), background var(--motion-fast);
}

.drawer-close:hover {
  color: var(--color-text-primary);
  background: var(--color-surface-sunken);
}

.drawer-close:active {
  color: var(--color-brand-active);
}

.drawer-form {
  padding-bottom: var(--space-8);
}

.drawer-actions {
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

/* ========== 详情页 ========== */
.detail-section {
  margin-bottom: var(--space-2);
}

/* 指标阈值小标签 */
.threshold-tag {
  margin-left: var(--space-1);
  vertical-align: middle;
}

/* ========== 状态操作区 ========== */
.status-actions {
  padding: var(--space-4);
  border-top: 1px solid var(--color-border-default);
  margin-bottom: var(--space-2);
}

.status-label {
  font-size: var(--font-size-caption);
  color: var(--color-text-secondary);
  margin: 0 0 var(--space-2) 0;
}

.status-btn-group {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

/* 争议按钮用语义错误色 */
.btn-dispute {
  border-color: var(--color-error);
  color: var(--color-error);
}

.btn-dispute:hover {
  background: var(--color-surface-sunken);
}

.btn-dispute:active {
  background: var(--color-brand-light);
}

/* ========== 录入检验表单 ========== */
.inspect-form-section {
  margin-top: var(--space-2);
}

.btn-cancel {
  margin-top: var(--space-1);
}

/* ========== 报告链接 ========== */
.report-link {
  color: var(--color-brand-primary);
  text-decoration: none;
  transition: color var(--motion-fast);
}

.report-link:hover {
  color: var(--color-brand-hover);
  text-decoration: underline;
}

.report-link:active {
  color: var(--color-brand-active);
}

/* ========== 争议 Dialog body ========== */
.dispute-body {
  padding: var(--space-4);
}

/* ========== 数字等宽（配合全局 .nums-tabular） ========== */
.nums-tabular {
  font-family: var(--font-mono);
  font-variant-numeric: tabular-nums;
  font-feature-settings: "tnum";
}

/* ========== 无障碍：焦点环（全局已在 vant-theme.css 设定，此处补充组件内） ========== */
a:focus-visible,
.drawer-close:focus-visible {
  outline: var(--outline-width) solid var(--color-brand-primary);
  outline-offset: 2px;
  border-radius: var(--radius-sm);
}

/* ========== 减弱动画支持 ========== */
@media (prefers-reduced-motion: reduce) {
  .drawer-close,
  .report-link,
  .btn-dispute {
    transition: none;
  }
}
</style>
