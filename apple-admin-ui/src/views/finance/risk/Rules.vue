<template>
  <div class="page-container">
    <div class="page-header">
      <h2>风控预警 · 规则配置</h2>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增规则</el-button>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        class="risk-input-md"
        placeholder="搜索规则名称 / 类型"
        clearable
        @clear="loadData"
        @keyup.enter="loadData"
      />
      <el-select v-model="query.severity" class="risk-input-sm" placeholder="严重度" clearable @change="loadData">
        <el-option v-for="s in SEVERITY_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-select v-model="query.enabled" class="risk-input-sm" placeholder="启用状态" clearable @change="loadData">
        <el-option label="已启用" :value="true" />
        <el-option label="已停用" :value="false" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
    </div>

    <!-- empty / loading / error / success 四态 -->
    <el-table :data="tableData" stripe v-loading="loading" border>
      <template #empty>
        <div class="risk-empty">{{ loadError ? '加载失败，请重试' : '暂无风控规则，点击右上角"新增规则"开始配置。' }}</div>
      </template>
      <el-table-column prop="name" label="规则名称" min-width="180" />
      <el-table-column prop="ruleType" label="规则类型" width="160" />
      <el-table-column label="阈值" width="120" class-name="nums-tabular">
        <template #default="{ row }">{{ formatThreshold(row.threshold) }}</template>
      </el-table-column>
      <el-table-column label="严重度" width="100">
        <template #default="{ row }">
          <el-tag :type="severityTagType(row.severity)" size="small" effect="light">
            {{ severityLabel(row.severity) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="启用" width="90">
        <template #default="{ row }">
          <el-switch
            :model-value="!!row.enabled"
            :loading="row._toggling"
            @change="(val) => handleToggle(row, val)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </div>
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

    <!-- Create / Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑风控规则' : '新增风控规则'"
      class="risk-dialog"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="96px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="form.name" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="form.ruleType" filterable allow-create class="risk-form-full">
            <el-option v-for="t in RULE_TYPE_OPTIONS" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值" prop="threshold">
          <el-input-number
            v-model="form.threshold"
            :min="0"
            :precision="4"
            controls-position="right"
            class="risk-form-full"
          />
        </el-form-item>
        <el-form-item label="严重度" prop="severity">
          <el-select v-model="form.severity" class="risk-form-full">
            <el-option v-for="s in SEVERITY_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { riskRuleApi } from '@/api/risk.js'
import { ElMessage, ElMessageBox } from 'element-plus'

// ── 后端契约（从 enums/RiskSeverity.java 1:1 复制） ──────────────────
// LOW(1, 低) / MEDIUM(2, 中) / HIGH(3, 高) / CRITICAL(4, 严重)
const SEVERITY_OPTIONS = [
  { value: 'LOW', label: '低' },
  { value: 'MEDIUM', label: '中' },
  { value: 'HIGH', label: '高' },
  { value: 'CRITICAL', label: '严重' },
]

// 从后端 V28__risk_rule_event.sql / RiskRule entity 注释提取
const RULE_TYPE_OPTIONS = [
  { value: 'OVERDUE_DAYS', label: '逾期天数 (天)' },
  { value: 'PRICE_DROP_PCT', label: '价格跌幅 (%)' },
  { value: 'TEMPERATURE_DEVIATION', label: '冷链温度偏离 (°C)' },
  { value: 'QUALITY_FAIL_RATE', label: '质检不合格率 (%)' },
  { value: 'CONCENTRATION_RATIO', label: '客户集中度 (%)' },
  { value: 'FRAUD_SCORE', label: '欺诈评分 (分)' },
  { value: 'PLEDGE_RATIO', label: '质押率 (%)' },
  { value: 'CREDIT_SCORE_BELOW', label: '信用评分低于 (分)' },
]

const loading = ref(false)
const loadError = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '', severity: '', enabled: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  name: '',
  ruleType: 'OVERDUE_DAYS',
  threshold: 30,
  severity: 'MEDIUM',
  enabled: true,
})
const formRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  threshold: [{ required: true, message: '请输入阈值', trigger: 'blur' }],
  severity: [{ required: true, message: '请选择严重度', trigger: 'change' }],
}

// ── helpers (兼容 enum 后端可能返回 {code,desc} 或 name 字符串) ─────
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

function formatThreshold(v) {
  if (v == null || v === '') return '-'
  const num = Number(v)
  if (Number.isNaN(num)) return String(v)
  return Number.isInteger(num) ? String(num) : num.toFixed(2)
}

function buildParams() {
  const p = { page: query.page, size: query.size }
  if (query.keyword) p.keyword = query.keyword
  if (query.severity) p.severity = query.severity
  if (query.enabled !== '' && query.enabled !== null && query.enabled !== undefined) {
    p.enabled = query.enabled
  }
  return p
}

async function loadData() {
  loading.value = true
  loadError.value = false
  try {
    const res = await riskRuleApi.list(buildParams())
    tableData.value = (res?.records || []).map((r) => ({ ...r, _toggling: false }))
    total.value = res?.total || 0
  } catch (e) {
    loadError.value = true
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    ruleType: 'OVERDUE_DAYS',
    threshold: 30,
    severity: 'MEDIUM',
    enabled: true,
  })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    ruleType: row.ruleType,
    threshold: row.threshold,
    severity: severityName(row.severity) || 'MEDIUM',
    enabled: !!row.enabled,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = {
      name: form.name,
      ruleType: form.ruleType,
      threshold: form.threshold,
      severity: form.severity,
      enabled: form.enabled,
    }
    if (isEdit.value) {
      await riskRuleApi.update(form.id, payload)
    } else {
      await riskRuleApi.create(payload)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // interceptor surfaces error
  } finally {
    submitting.value = false
  }
}

async function handleToggle(row, next) {
  if (row._toggling) return
  row._toggling = true
  try {
    await riskRuleApi.setEnabled(row.id, next)
    row.enabled = next
    ElMessage.success(next ? '已启用' : '已停用')
  } catch (e) {
    // revert visually by reloading
    loadData()
  } finally {
    row._toggling = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除规则 "${row.name}" 吗？`, '提示', { type: 'warning' })
  } catch (_) {
    return
  }
  try {
    await riskRuleApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // interceptor surfaces error
  }
}

onMounted(loadData)
</script>

<style scoped>
/* 所有间距 / 圆角 / 焦点环走 CSS 变量，禁止硬编码颜色与 ≥2px 数值 */
.risk-input-md { width: 16rem; }
.risk-input-sm { width: 9rem; }
.risk-form-full { width: 100%; }
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
/* :focus-visible 可见焦点环 (D008 合规) */
.risk-input-md :deep(.el-input__wrapper):focus-visible,
.risk-input-sm :deep(.el-select__wrapper):focus-visible {
  box-shadow: var(--shadow-focus);
}
</style>
