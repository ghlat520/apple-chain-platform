<template>
  <div class="page-container">
    <div class="page-header">
      <h2>贷款管理</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增贷款</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索贷款编号/借款人" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.loanType" placeholder="贷款类型" clearable style="width:150px" @change="loadData">
        <el-option label="质押贷款" value="PLEDGE" />
        <el-option label="应收账款融资" value="RECEIVABLE" />
        <el-option label="信用贷款" value="CREDIT" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width:130px" @change="loadData">
        <el-option label="待审批" value="PENDING" />
        <el-option label="已审批" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
        <el-option label="已放款" value="DISBURSED" />
        <el-option label="已还款" value="REPAID" />
        <el-option label="已逾期" value="OVERDUE" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="loanCode" label="贷款编号" width="180" />
      <el-table-column prop="borrowerName" label="借款人" width="140" />
      <el-table-column label="贷款类型" width="120">
        <template #default="{ row }">{{ loanTypeLabel(row.loanType) }}</template>
      </el-table-column>
      <el-table-column prop="amount" label="金额(元)" width="120" class-name="nums-tabular" />
      <el-table-column prop="interestRate" label="年利率(%)" width="110" class-name="nums-tabular">
        <template #default="{ row }">{{ row.interestRate != null ? (row.interestRate * 100).toFixed(2) + '%' : '-' }}</template>
      </el-table-column>
      <el-table-column prop="termMonths" label="期限(月)" width="100" class-name="nums-tabular" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <template v-if="rowStatus(row) === 'PENDING'">
              <el-button link type="success" size="small" @click="handleApprove(row)">审批</el-button>
              <el-button link type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
            <el-button v-if="rowStatus(row) === 'APPROVED'" link type="success" size="small" @click="handleDisburse(row)">放款</el-button>
            <el-button v-if="rowStatus(row) === 'DISBURSED'" link type="warning" size="small" @click="openRepay(row)">还款</el-button>
            <el-button v-if="rowStatus(row) === 'DISBURSED'" link type="info" size="small" @click="handleOverdue(row)">标记逾期</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
    <el-drawer v-model="drawerVisible" title="贷款详情" size="600px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="贷款编号">{{ currentRow.loanCode }}</el-descriptions-item>
          <el-descriptions-item label="借款人">{{ currentRow.borrowerName }}</el-descriptions-item>
          <el-descriptions-item label="贷款类型">{{ loanTypeLabel(currentRow.loanType) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(currentRow.status)" size="small">{{ statusLabel(currentRow.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="贷款金额(元)" class-name="nums-tabular">{{ currentRow.amount }}</el-descriptions-item>
          <el-descriptions-item label="利率" class-name="nums-tabular">{{ currentRow.interestRate != null ? (currentRow.interestRate * 100).toFixed(2) + '%' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="期限(月)" class-name="nums-tabular">{{ currentRow.termMonths }}</el-descriptions-item>
          <el-descriptions-item label="已还金额(元)" class-name="nums-tabular">{{ currentRow.repaidAmount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请日期">{{ currentRow.applyDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批日期">{{ currentRow.approveDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="放款日期">{{ currentRow.disburseDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="到期日期">{{ currentRow.dueDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentRow.updateTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑贷款' : '新增贷款'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="借款人" prop="borrowerName">
          <el-input v-model="form.borrowerName" />
        </el-form-item>
        <el-form-item label="贷款类型" prop="loanType">
          <el-select v-model="form.loanType" style="width:100%">
            <el-option label="质押贷款" value="PLEDGE" />
            <el-option label="应收账款融资" value="RECEIVABLE" />
            <el-option label="信用贷款" value="CREDIT" />
          </el-select>
        </el-form-item>
        <el-form-item label="贷款金额(元)" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="年利率" prop="interestRate">
          <el-input-number v-model="form.interestRate" :min="0" :max="1" :step="0.01" :precision="4" style="width:100%" />
          <div style="font-size:12px;color:#909399">请输入小数形式，如 0.05 表示 5%</div>
        </el-form-item>
        <el-form-item label="期限(月)" prop="termMonths">
          <el-input-number v-model="form.termMonths" :min="1" :precision="0" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- Repay Dialog -->
    <el-dialog v-model="repayVisible" title="还款" width="400px">
      <el-form ref="repayFormRef" :model="repayForm" :rules="repayRules" label-width="100px">
        <el-form-item label="还款金额(元)" prop="amount">
          <el-input-number v-model="repayForm.amount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="repayVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRepay" :loading="submitting">确认还款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { loanApi } from '@/api/finance.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', loanType: '', status: '' })

const drawerVisible = ref(false)
const currentRow = ref(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ id: null, borrowerName: '', loanType: 'PLEDGE', amount: null, interestRate: null, termMonths: null })
const formRules = {
  borrowerName: [{ required: true, message: '请输入借款人', trigger: 'blur' }],
  loanType: [{ required: true, message: '请选择贷款类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入贷款金额', trigger: 'blur' }],
  interestRate: [{ required: true, message: '请输入利率', trigger: 'blur' }],
  termMonths: [{ required: true, message: '请输入期限', trigger: 'blur' }],
}

const repayVisible = ref(false)
const repayFormRef = ref()
const repayForm = reactive({ id: null, amount: null })
const repayRules = {
  amount: [{ required: true, message: '请输入还款金额', trigger: 'blur' }],
}

function loanTypeLabel(type) {
  const raw = type?.code || type
  return { PLEDGE: '质押贷款', RECEIVABLE: '应收账款融资', CREDIT: '信用贷款' }[raw] || raw
}

function statusLabel(status) {
  const raw = status?.code || status
  return { PENDING: '待审批', APPROVED: '已审批', REJECTED: '已拒绝', DISBURSED: '已放款', REPAID: '已还款', OVERDUE: '已逾期' }[raw] || raw
}

function rowStatus(row) {
  return row.status?.code || row.status
}

function statusType(status) {
  const code = status?.code || status
  return { PENDING: 'info', APPROVED: 'primary', REJECTED: 'danger', DISBURSED: 'success', REPAID: 'warning', OVERDUE: 'danger' }[code] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await loanApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function viewDetail(row) {
  currentRow.value = row
  drawerVisible.value = true
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: null, borrowerName: '', loanType: 'PLEDGE', amount: null, interestRate: null, termMonths: null })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    borrowerName: row.borrowerName,
    loanType: row.loanType?.code || row.loanType,
    amount: row.amount,
    interestRate: row.interestRate,
    termMonths: row.termMonths,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await loanApi.update(form.id, form)
    } else {
      await loanApi.create(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleApprove(row) {
  await ElMessageBox.confirm(`确认审批通过贷款 ${row.loanCode}?`, '提示', { type: 'warning' })
  try {
    await loanApi.approve(row.id)
    ElMessage.success('审批成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleReject(row) {
  await ElMessageBox.confirm(`确认拒绝贷款 ${row.loanCode}?`, '警告', { type: 'warning' })
  try {
    await loanApi.reject(row.id)
    ElMessage.success('已拒绝')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleDisburse(row) {
  await ElMessageBox.confirm(`确认对贷款 ${row.loanCode} 进行放款?`, '提示', { type: 'warning' })
  try {
    await loanApi.disburse(row.id)
    ElMessage.success('放款成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

function openRepay(row) {
  Object.assign(repayForm, { id: row.id, amount: null })
  repayVisible.value = true
}

async function handleRepay() {
  const valid = await repayFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await loanApi.repay(repayForm.id, repayForm.amount)
    ElMessage.success('还款成功')
    repayVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleOverdue(row) {
  await ElMessageBox.confirm(`确认将贷款 ${row.loanCode} 标记为逾期?`, '警告', { type: 'warning' })
  try {
    await loanApi.markOverdue(row.id)
    ElMessage.success('已标记逾期')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除贷款 ${row.loanCode}?`, '提示', { type: 'warning' })
  try {
    await loanApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
