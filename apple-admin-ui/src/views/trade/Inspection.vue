<template>
  <div class="page-container">
    <div class="page-header">
      <h2>质量检验</h2>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.orderId" placeholder="订单ID" clearable style="width:140px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:130px" @change="loadData">
        <el-option label="待检验" value="PENDING" />
        <el-option label="已检验" value="INSPECTED" />
        <el-option label="已验收" value="ACCEPTED" />
        <el-option label="争议中" value="DISPUTED" />
      </el-select>
      <el-select v-model="query.result" placeholder="检验结果" clearable style="width:130px" @change="loadData">
        <el-option label="通过" value="PASS" />
        <el-option label="不通过" value="FAIL" />
        <el-option label="有条件通过" value="CONDITIONAL" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="inspectionNo" label="检验编号" width="180" />
      <el-table-column prop="orderId" label="订单ID" width="180" class-name="nums-tabular" />
      <el-table-column prop="variety" label="品种" width="120" />
      <el-table-column prop="grade" label="等级" width="100" />
      <el-table-column prop="inspectorName" label="检验员" width="120" />
      <el-table-column prop="result" label="检验结果" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.result" :type="resultType(row.result)" size="small">{{ resultLabel(row.result) }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="inspectionDate" label="检验日期" width="140" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="info" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              link type="primary" size="small"
              @click="openInspectDialog(row)"
            >录入结果</el-button>
            <el-button
              v-if="row.status === 'INSPECTED'"
              link type="success" size="small"
              @click="handleAccept(row)"
            >验收</el-button>
            <el-button
              v-if="row.status === 'INSPECTED'"
              link type="danger" size="small"
              @click="openDisputeDialog(row)"
            >争议</el-button>
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
    <el-drawer v-model="detailVisible" title="质检详情" size="560px">
      <template v-if="detailRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="检验编号">{{ detailRow.inspectionNo }}</el-descriptions-item>
          <el-descriptions-item label="订单ID" class-name="nums-tabular">{{ detailRow.orderId ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="供货ID" class-name="nums-tabular">{{ detailRow.supplyId ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="品种">{{ detailRow.variety || '-' }}</el-descriptions-item>
          <el-descriptions-item label="等级">{{ detailRow.grade || '-' }}</el-descriptions-item>
          <el-descriptions-item label="检验员">{{ detailRow.inspectorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="检验日期">{{ detailRow.inspectionDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="检验结果">
            <el-tag v-if="detailRow.result" :type="resultType(detailRow.result)" size="small">{{ resultLabel(detailRow.result) }}</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detailRow.status)" size="small">{{ statusLabel(detailRow.status) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">质量指标</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="糖度(Brix)" class-name="nums-tabular">{{ detailRow.brixValue ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="硬度" class-name="nums-tabular">{{ detailRow.firmnessValue ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="色泽评分" class-name="nums-tabular">{{ detailRow.colorScore ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="缺陷率(%)" class-name="nums-tabular">{{ detailRow.defectRate ?? '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-descriptions :column="1" border style="margin-top:12px" v-if="detailRow.reportUrl || detailRow.remark">
          <el-descriptions-item label="报告链接" v-if="detailRow.reportUrl">
            <a :href="detailRow.reportUrl" target="_blank" style="color:var(--el-color-primary)">{{ detailRow.reportUrl }}</a>
          </el-descriptions-item>
          <el-descriptions-item label="备注" v-if="detailRow.remark">{{ detailRow.remark }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Inspect Dialog -->
    <el-dialog v-model="inspectVisible" title="录入检验结果" width="440px">
      <el-form ref="inspectFormRef" :model="inspectForm" :rules="inspectRules" label-width="100px">
        <el-form-item label="检验结果" prop="result">
          <el-select v-model="inspectForm.result" style="width:100%" placeholder="请选择检验结果">
            <el-option label="通过" value="PASS" />
            <el-option label="不通过" value="FAIL" />
            <el-option label="有条件通过" value="CONDITIONAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级" prop="grade">
          <el-select v-model="inspectForm.grade" style="width:100%">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="糖度(Brix)">
          <el-input-number v-model="inspectForm.brixValue" :min="0" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="硬度">
          <el-input-number v-model="inspectForm.firmnessValue" :min="0" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="色泽评分">
          <el-input-number v-model="inspectForm.colorScore" :min="0" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="缺陷率(%)">
          <el-input-number v-model="inspectForm.defectRate" :min="0" :max="100" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="inspectForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inspectVisible = false">取消</el-button>
        <el-button type="primary" @click="handleInspectSubmit" :loading="inspectSubmitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- Dispute Dialog -->
    <el-dialog v-model="disputeVisible" title="发起争议" width="440px">
      <el-form ref="disputeFormRef" :model="disputeForm" :rules="disputeRules" label-width="100px">
        <el-form-item label="争议原因" prop="reason">
          <el-input v-model="disputeForm.reason" type="textarea" :rows="3" placeholder="请详细描述争议原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="disputeVisible = false">取消</el-button>
        <el-button type="danger" @click="handleDisputeSubmit" :loading="disputeSubmitting">提交争议</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { inspectionApi } from '@/api/trade.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, orderId: '', status: '', result: '' })

const inspectVisible = ref(false)
const inspectSubmitting = ref(false)
const inspectFormRef = ref()
const currentInspectRow = ref(null)
const inspectForm = reactive({ result: '', grade: '', brixValue: null, firmnessValue: null, colorScore: null, defectRate: null, remark: '' })
const inspectRules = {
  result: [{ required: true, message: '请输入检验结果', trigger: 'blur' }],
  grade: [{ required: true, message: '请选择等级', trigger: 'change' }],
}

const disputeVisible = ref(false)
const disputeSubmitting = ref(false)
const disputeFormRef = ref()
const currentDisputeRow = ref(null)
const disputeForm = reactive({ reason: '' })
const disputeRules = {
  reason: [{ required: true, message: '请输入争议原因', trigger: 'blur' }],
}

function statusType(status) {
  const map = { PENDING: 'info', INSPECTED: 'primary', ACCEPTED: 'success', DISPUTED: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { PENDING: '待检验', INSPECTED: '已检验', ACCEPTED: '已验收', DISPUTED: '争议中' }
  return map[status] || status || '-'
}

function resultType(result) {
  const map = { PASS: 'success', FAIL: 'danger', CONDITIONAL: 'warning' }
  return map[result] || 'info'
}

function resultLabel(result) {
  const map = { PASS: '通过', FAIL: '不通过', CONDITIONAL: '有条件通过' }
  return map[result] || result || '-'
}

// Detail drawer
const detailVisible = ref(false)
const detailRow = ref(null)

async function viewDetail(row) {
  detailRow.value = row
  detailVisible.value = true
  try {
    const res = await inspectionApi.get(row.id)
    detailRow.value = res || row
  } catch (e) {
    // keep row data
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.orderId) params.orderId = query.orderId
    if (query.status) params.status = query.status
    if (query.result) params.result = query.result
    const res = await inspectionApi.list(params)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openInspectDialog(row) {
  currentInspectRow.value = row
  Object.assign(inspectForm, { result: '', grade: '', brixValue: null, firmnessValue: null, colorScore: null, defectRate: null, remark: '' })
  inspectVisible.value = true
}

async function handleInspectSubmit() {
  const valid = await inspectFormRef.value.validate().catch(() => false)
  if (!valid) return
  inspectSubmitting.value = true
  try {
    await inspectionApi.inspect(currentInspectRow.value.id, inspectForm)
    ElMessage.success('检验结果已录入')
    inspectVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    inspectSubmitting.value = false
  }
}

async function handleAccept(row) {
  await ElMessageBox.confirm(`确认验收检验记录 ${row.inspectionNo}?`, '提示', { type: 'warning' })
  try {
    await inspectionApi.accept(row.id)
    ElMessage.success('验收成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

function openDisputeDialog(row) {
  currentDisputeRow.value = row
  Object.assign(disputeForm, { reason: '' })
  disputeVisible.value = true
}

async function handleDisputeSubmit() {
  const valid = await disputeFormRef.value.validate().catch(() => false)
  if (!valid) return
  disputeSubmitting.value = true
  try {
    await inspectionApi.dispute(currentDisputeRow.value.id, disputeForm)
    ElMessage.success('争议已提交')
    disputeVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    disputeSubmitting.value = false
  }
}

onMounted(loadData)
</script>
