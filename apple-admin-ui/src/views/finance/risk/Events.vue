<template>
  <div class="page-container">
    <div class="page-header">
      <h2>风控预警 · 事件看板</h2>
    </div>

    <div class="filter-bar">
      <el-select v-model="query.severity" class="risk-input-sm" placeholder="严重度" clearable @change="loadData">
        <el-option v-for="s in SEVERITY_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-select v-model="query.status" class="risk-input-sm" placeholder="状态" clearable @change="loadData">
        <el-option v-for="s in STATUS_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-select v-model="query.targetType" class="risk-input-sm" placeholder="对象类型" clearable @change="loadData">
        <el-option v-for="t in TARGET_TYPE_OPTIONS" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <!-- 严重度汇总色块 -->
    <div class="severity-summary" v-if="!loading && tableData.length > 0">
      <div
        v-for="s in SEVERITY_OPTIONS"
        :key="s.value"
        class="severity-chip"
        :class="`severity-chip--${s.value.toLowerCase()}`"
      >
        <span class="severity-chip__label">{{ s.label }}</span>
        <span class="severity-chip__count nums-tabular">{{ severityCount(s.value) }}</span>
      </div>
    </div>

    <el-table :data="tableData" stripe v-loading="loading" border>
      <template #empty>
        <div class="risk-empty">{{ loadError ? '加载失败，请重试' : '暂无风控事件' }}</div>
      </template>
      <el-table-column label="事件ID" width="120" class-name="nums-tabular">
        <template #default="{ row }">{{ row.id }}</template>
      </el-table-column>
      <el-table-column label="严重度" width="100">
        <template #default="{ row }">
          <el-tag :type="severityTagType(row.severity)" size="small" effect="light">
            {{ severityLabel(row.severity) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetType" label="对象类型" width="120" />
      <el-table-column label="对象ID" width="140" class-name="nums-tabular">
        <template #default="{ row }">{{ row.targetId }}</template>
      </el-table-column>
      <el-table-column label="触发值" width="110" class-name="nums-tabular">
        <template #default="{ row }">{{ formatNumber(row.triggerValue) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="triggerTime" label="触发时间" width="170" />
      <el-table-column prop="handleTime" label="处置时间" width="170">
        <template #default="{ row }">{{ row.handleTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="处置备注" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.handleRemark || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            size="small"
            :disabled="isTerminal(row.status)"
            @click="openHandle(row)"
          >
            {{ isTerminal(row.status) ? '已结案' : '处置' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      class="risk-pagination"
      background
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.size"
      :current-page="query.page"
      @current-change="(p) => { query.page = p; loadData() }"
    />

    <!-- Handle Dialog -->
    <el-dialog
      v-model="dialogVisible"
      title="处置风控事件"
      class="risk-dialog"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" size="small" border v-if="currentRow">
        <el-descriptions-item label="事件ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="严重度">
          <el-tag :type="severityTagType(currentRow.severity)" size="small" effect="light">
            {{ severityLabel(currentRow.severity) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="对象">{{ currentRow.targetType }} / {{ currentRow.targetId }}</el-descriptions-item>
        <el-descriptions-item label="触发值">{{ formatNumber(currentRow.triggerValue) }}</el-descriptions-item>
        <el-descriptions-item label="触发时间" :span="2">{{ currentRow.triggerTime }}</el-descriptions-item>
      </el-descriptions>

      <el-form ref="formRef" :model="form" :rules="formRules" label-width="96px" class="risk-form">
        <el-form-item label="目标状态" prop="status">
          <el-select v-model="form.status" class="risk-form-full">
            <el-option
              v-for="s in handleStatusOptions"
              :key="s.value"
              :label="s.label"
              :value="s.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处置人ID" prop="assigneeId">
          <el-input v-model="form.assigneeId" placeholder="可选：处置人用户ID" />
        </el-form-item>
        <el-form-item label="处置备注" prop="handleRemark">
          <el-input
            v-model="form.handleRemark"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请填写处置说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { riskEventApi } from '@/api/risk.js'
import { ElMessage } from 'element-plus'

// ── 后端契约（1:1 复制自 enums/RiskSeverity.java + enums/RiskEventStatus.java） ──
const SEVERITY_OPTIONS = [
  { value: 'LOW', label: '低' },
  { value: 'MEDIUM', label: '中' },
  { value: 'HIGH', label: '高' },
  { value: 'CRITICAL', label: '严重' },
]

const STATUS_OPTIONS = [
  { value: 'PENDING', label: '待处理' },
  { value: 'HANDLING', label: '处理中' },
  { value: 'RESOLVED', label: '已解决' },
  { value: 'IGNORED', label: '已忽略' },
]

// 从 V28__risk_rule_event.sql 种子 + 后端 RiskEvent.targetType 归纳
const TARGET_TYPE_OPTIONS = [
  { value: 'LOAN', label: '贷款' },
  { value: 'PLEDGE', label: '质押' },
  { value: 'CREDIT', label: '授信' },
  { value: 'ORDER', label: '订单' },
  { value: 'BATCH', label: '批次' },
]

const TERMINAL_STATUSES = new Set(['RESOLVED', 'IGNORED'])

const loading = ref(false)
const loadError = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, severity: '', status: '', targetType: '' })

const dialogVisible = ref(false)
const submitting = ref(false)
const currentRow = ref(null)
const formRef = ref()
const form = reactive({
  status: 'HANDLING',
  assigneeId: '',
  handleRemark: '',
})
const formRules = {
  status: [{ required: true, message: '请选择目标状态', trigger: 'change' }],
  handleRemark: [{ max: 500, message: '备注最长 500 字', trigger: 'blur' }],
}

// 处置可选的目标状态依赖于当前状态（后端：禁止回到 PENDING，结案后不可再处置）
const handleStatusOptions = computed(() => {
  const cur = statusName(currentRow.value?.status)
  if (cur === 'PENDING') {
    return [
      { value: 'HANDLING', label: '标记为处理中' },
      { value: 'RESOLVED', label: '直接解决' },
      { value: 'IGNORED', label: '忽略' },
    ]
  }
  if (cur === 'HANDLING') {
    return [
      { value: 'RESOLVED', label: '标记为已解决' },
      { value: 'IGNORED', label: '忽略' },
    ]
  }
  return []
})

// ── helpers（兼容后端 BaseEnum 可能返回 {code,desc} 或字符串） ──────
function severityName(sev) {
  if (sev == null) return ''
  if (typeof sev === 'object') {
    if (typeof sev.code === 'number') {
      return ['', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'][sev.code] || ''
    }
    return sev.name || ''
  }
  return String(sev)
}
function severityLabel(sev) {
  if (sev && typeof sev === 'object' && sev.desc) return sev.desc
  return { LOW: '低', MEDIUM: '中', HIGH: '高', CRITICAL: '严重' }[severityName(sev)] || severityName(sev)
}
function severityTagType(sev) {
  return { LOW: 'info', MEDIUM: 'warning', HIGH: 'danger', CRITICAL: 'danger' }[severityName(sev)] || 'info'
}

function statusName(st) {
  if (st == null) return ''
  if (typeof st === 'object') {
    if (typeof st.code === 'number') {
      return ['', 'PENDING', 'HANDLING', 'RESOLVED', 'IGNORED'][st.code] || ''
    }
    return st.name || ''
  }
  return String(st)
}
function statusLabel(st) {
  if (st && typeof st === 'object' && st.desc) return st.desc
  return { PENDING: '待处理', HANDLING: '处理中', RESOLVED: '已解决', IGNORED: '已忽略' }[statusName(st)] || statusName(st)
}
function statusTagType(st) {
  return { PENDING: 'warning', HANDLING: 'primary', RESOLVED: 'success', IGNORED: 'info' }[statusName(st)] || 'info'
}

function isTerminal(st) {
  return TERMINAL_STATUSES.has(statusName(st))
}

function formatNumber(v) {
  if (v == null || v === '') return '-'
  const num = Number(v)
  if (Number.isNaN(num)) return String(v)
  return Number.isInteger(num) ? String(num) : num.toFixed(2)
}

function severityCount(sevValue) {
  return tableData.value.filter((r) => severityName(r.severity) === sevValue).length
}

function buildParams() {
  const p = { page: query.page, size: query.size }
  if (query.severity) p.severity = query.severity
  if (query.status) p.status = query.status
  if (query.targetType) p.targetType = query.targetType
  return p
}

async function loadData() {
  loading.value = true
  loadError.value = false
  try {
    const res = await riskEventApi.list(buildParams())
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) {
    loadError.value = true
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.severity = ''
  query.status = ''
  query.targetType = ''
  query.page = 1
  loadData()
}

function openHandle(row) {
  currentRow.value = row
  const cur = statusName(row.status)
  form.status = cur === 'PENDING' ? 'HANDLING' : 'RESOLVED'
  form.assigneeId = ''
  form.handleRemark = ''
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = {
      status: form.status,
      handleRemark: form.handleRemark || null,
    }
    // assigneeId 为可选数值；空字符串不提交
    if (form.assigneeId !== '' && form.assigneeId != null) {
      const n = Number(form.assigneeId)
      if (!Number.isNaN(n)) payload.assigneeId = n
    }
    await riskEventApi.handle(currentRow.value.id, payload)
    ElMessage.success('处置成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // interceptor surfaces error
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
/* 所有间距 / 圆角走 CSS 变量；禁止硬编码颜色与 ≥2px 数值 */
.risk-input-sm { width: 9rem; }
.risk-form-full { width: 100%; }
.risk-form { margin-top: var(--space-4); }
.risk-pagination {
  margin-top: var(--space-4);
  justify-content: flex-end;
}
.risk-empty {
  padding: var(--space-6) 0;
  color: var(--color-text-tertiary);
  font-size: var(--font-size-body);
  line-height: var(--line-height-body);
}
.risk-dialog :deep(.el-dialog) {
  border-radius: var(--radius-lg);
}

/* 严重度汇总色块：走主题语义色，不硬编码 */
.severity-summary {
  display: flex;
  gap: var(--space-3);
  margin: var(--space-3) 0 var(--space-4);
  flex-wrap: wrap;
}
.severity-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  background: var(--color-surface-sunken);
  color: var(--color-text-primary);
  font-size: var(--font-size-body);
  line-height: var(--line-height-body);
}
.severity-chip__count {
  color: var(--color-text-primary);
}
.severity-chip--low { border-left: 0.25rem solid var(--color-text-tertiary); }
.severity-chip--medium { border-left: 0.25rem solid var(--color-warning); }
.severity-chip--high { border-left: 0.25rem solid var(--color-error); }
.severity-chip--critical { border-left: 0.25rem solid var(--color-brand-primary); }

/* :focus-visible 可见焦点环 */
.risk-input-sm :deep(.el-select__wrapper):focus-visible {
  box-shadow: var(--shadow-focus);
}
</style>
