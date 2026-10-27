<template>
  <div class="page-container">
    <div class="page-header">
      <h2>异常追溯</h2>
      <el-button type="primary" @click="openReport" icon="Plus">上报异常</el-button>
    </div>
    <div class="filter-bar">
      <el-select v-model="query.anomalyType" placeholder="异常类型" clearable style="width:180px" @change="loadData">
        <el-option label="农药残留超标" value="PESTICIDE_EXCESS" />
        <el-option label="温度违规" value="TEMP_VIOLATION" />
        <el-option label="质量不合格" value="QUALITY_FAIL" />
        <el-option label="其他" value="OTHER" />
      </el-select>
      <el-select v-model="query.status" placeholder="处理状态" clearable style="width:150px" @change="loadData">
        <el-option label="待处理" value="OPEN" />
        <el-option label="调查中" value="INVESTIGATING" />
        <el-option label="已解决" value="RESOLVED" />
      </el-select>
      <el-select v-model="query.severity" placeholder="严重程度" clearable style="width:140px" @change="loadData">
        <el-option label="高" value="HIGH" />
        <el-option label="中" value="MEDIUM" />
        <el-option label="低" value="LOW" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="anomalyType" label="异常类型" width="150">
        <template #default="{ row }">{{ anomalyTypeLabel(row.anomalyType) }}</template>
      </el-table-column>
      <el-table-column prop="traceCode" label="溯源码" width="180" />
      <el-table-column prop="severity" label="严重程度" width="110">
        <template #default="{ row }">
          <el-tag :type="severityType(row.severity)" size="small">{{ severityLabel(row.severity) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="异常描述" />
      <el-table-column prop="status" label="处理状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="上报时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 'OPEN'"
              link
              type="warning"
              size="small"
              @click="handleInvestigate(row)"
            >开始调查</el-button>
            <el-button
              v-if="row.status === 'INVESTIGATING'"
              link
              type="success"
              size="small"
              @click="openResolve(row)"
            >解决</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      style="margin-top:16px;justify-content:flex-end"
      background
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.size"
      :current-page="query.page"
      @current-change="(p) => { query.page = p; loadData() }"
    />

    <!-- Detail Drawer -->
    <el-drawer v-model="drawerVisible" title="异常详情" size="560px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="异常类型">{{ anomalyTypeLabel(currentRow.anomalyType) }}</el-descriptions-item>
          <el-descriptions-item label="溯源码">{{ currentRow.traceCode }}</el-descriptions-item>
          <el-descriptions-item label="批次ID" class-name="nums-tabular">{{ currentRow.batchId ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="严重程度">
            <el-tag :type="severityType(currentRow.severity)" size="small">{{ severityLabel(currentRow.severity) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="statusType(currentRow.status)" size="small">{{ statusLabel(currentRow.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="上报时间">{{ currentRow.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="解决时间">{{ currentRow.resolvedTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="异常描述" :span="2">{{ currentRow.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="根因分析" :span="2">{{ currentRow.rootCauseAnalysis || '-' }}</el-descriptions-item>
          <el-descriptions-item label="解决人">{{ currentRow.resolvedBy || '-' }}</el-descriptions-item>
          <el-descriptions-item label="影响批次">{{ currentRow.affectedBatchCount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="影响果品">{{ currentRow.affectedFruitCount ?? '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Report Dialog -->
    <el-dialog v-model="reportDialogVisible" title="上报异常" width="500px">
      <el-form ref="reportFormRef" :model="reportForm" :rules="reportRules" label-width="100px">
        <el-form-item label="溯源码" prop="traceCode">
          <el-input v-model="reportForm.traceCode" placeholder="请输入溯源码" />
        </el-form-item>
        <el-form-item label="批次ID">
          <el-input-number v-model="reportForm.batchId" :min="1" style="width:100%" placeholder="关联批次ID（可选）" />
        </el-form-item>
        <el-form-item label="异常类型" prop="anomalyType">
          <el-select v-model="reportForm.anomalyType" style="width:100%">
            <el-option label="农药残留超标" value="PESTICIDE_EXCESS" />
            <el-option label="温度违规" value="TEMP_VIOLATION" />
            <el-option label="质量不合格" value="QUALITY_FAIL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度" prop="severity">
          <el-select v-model="reportForm.severity" style="width:100%">
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="异常描述" prop="description">
          <el-input v-model="reportForm.description" type="textarea" :rows="4" placeholder="请详细描述异常情况" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="reportForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReport" :loading="reportSubmitting">提交上报</el-button>
      </template>
    </el-dialog>

    <!-- Resolve Dialog -->
    <el-dialog v-model="resolveDialogVisible" title="解决异常" width="480px">
      <el-form ref="resolveFormRef" :model="resolveForm" :rules="resolveRules" label-width="100px">
        <el-form-item label="根因分析" prop="rootCause">
          <el-input
            v-model="resolveForm.rootCause"
            type="textarea"
            :rows="5"
            placeholder="请输入根因分析"
          />
        </el-form-item>
        <el-form-item label="解决人" prop="resolvedBy">
          <el-input v-model="resolveForm.resolvedBy" placeholder="请输入解决人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResolve" :loading="resolveSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { anomalyApi } from '@/api/trace.js'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, anomalyType: '', status: '', severity: '' })

// Detail drawer
const drawerVisible = ref(false)
const currentRow = ref(null)

// Report dialog
const reportDialogVisible = ref(false)
const reportSubmitting = ref(false)
const reportFormRef = ref()
const reportForm = reactive({ traceCode: '', batchId: null, anomalyType: '', severity: 'MEDIUM', description: '', remark: '' })
const reportRules = {
  traceCode: [{ required: true, message: '请输入溯源码', trigger: 'blur' }],
  anomalyType: [{ required: true, message: '请选择异常类型', trigger: 'change' }],
  severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }],
  description: [{ required: true, message: '请描述异常情况', trigger: 'blur' }],
}

// Resolve dialog
const resolveDialogVisible = ref(false)
const resolveSubmitting = ref(false)
const resolveFormRef = ref()
const resolveForm = reactive({ id: null, rootCause: '', resolvedBy: '' })
const resolveRules = {
  rootCause: [{ required: true, message: '请输入根因分析', trigger: 'blur' }],
  resolvedBy: [{ required: true, message: '请输入解决人', trigger: 'blur' }],
}

function anomalyTypeLabel(type) {
  const map = { PESTICIDE_EXCESS: '农药残留超标', TEMP_VIOLATION: '温度违规', QUALITY_FAIL: '质量不合格', OTHER: '其他' }
  return map[type] || type || '-'
}

function severityLabel(severity) {
  const map = { HIGH: '高', MEDIUM: '中', LOW: '低' }
  return map[severity] || severity || '-'
}

function severityType(severity) {
  const map = { HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }
  return map[severity] || 'info'
}

function statusLabel(status) {
  const map = { OPEN: '待处理', INVESTIGATING: '调查中', RESOLVED: '已解决' }
  return map[status] || status || '-'
}

function statusType(status) {
  const map = { OPEN: 'danger', INVESTIGATING: 'warning', RESOLVED: 'success' }
  return map[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.anomalyType) params.type = query.anomalyType
    if (query.status) params.status = query.status
    if (query.severity) params.severity = query.severity
    const res = await anomalyApi.list(params)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function viewDetail(row) {
  currentRow.value = row
  drawerVisible.value = true
  try {
    const res = await anomalyApi.get(row.id)
    currentRow.value = res || row
  } catch (e) {
    // keep row data
  }
}

function openReport() {
  Object.assign(reportForm, { traceCode: '', batchId: null, anomalyType: '', severity: 'MEDIUM', description: '', remark: '' })
  reportDialogVisible.value = true
}

async function handleReport() {
  const valid = await reportFormRef.value.validate().catch(() => false)
  if (!valid) return
  reportSubmitting.value = true
  try {
    await anomalyApi.report(reportForm)
    ElMessage.success('异常上报成功')
    reportDialogVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    reportSubmitting.value = false
  }
}

async function handleInvestigate(row) {
  try {
    await anomalyApi.handle(row.id, { action: 'investigate' })
    ElMessage.success('已开始调查')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

function openResolve(row) {
  Object.assign(resolveForm, { id: row.id, rootCause: '', resolvedBy: '' })
  resolveDialogVisible.value = true
}

async function handleResolve() {
  const valid = await resolveFormRef.value.validate().catch(() => false)
  if (!valid) return
  resolveSubmitting.value = true
  try {
    await anomalyApi.handle(resolveForm.id, { rootCause: resolveForm.rootCause, resolvedBy: resolveForm.resolvedBy })
    ElMessage.success('解决成功')
    resolveDialogVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    resolveSubmitting.value = false
  }
}

onMounted(loadData)
</script>
