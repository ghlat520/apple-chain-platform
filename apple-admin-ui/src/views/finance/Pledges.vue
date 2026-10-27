<template>
  <div class="page-container">
    <div class="page-header">
      <h2>质押管理</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增质押</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索质押编号/出质人" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:130px" @change="loadData">
        <el-option label="待激活" value="PENDING" />
        <el-option label="已激活" value="ACTIVE" />
        <el-option label="已释放" value="RELEASED" />
        <el-option label="已违约" value="DEFAULTED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="pledgeCode" label="质押编号" width="180" />
      <el-table-column prop="pledgorName" label="出质人" width="140" />
      <el-table-column prop="pledgeeName" label="质权人" width="140" />
      <el-table-column prop="commodity" label="质押物" min-width="180" />
      <el-table-column prop="quantity" label="数量" width="100" class-name="nums-tabular">
        <template #default="{ row }">{{ row.quantity }} {{ row.unit }}</template>
      </el-table-column>
      <el-table-column prop="appraisedValue" label="评估价值(元)" width="130" class-name="nums-tabular" />
      <el-table-column prop="loanAmount" label="贷款金额(元)" width="130" class-name="nums-tabular" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="pledgeStatusType(row.status)" size="small">{{ pledgeStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="rowStatus(row) === 'PENDING'" link type="success" size="small" @click="handleActivate(row)">激活</el-button>
            <el-button v-if="rowStatus(row) === 'ACTIVE'" link type="warning" size="small" @click="handleRelease(row)">释放</el-button>
            <el-button v-if="rowStatus(row) === 'ACTIVE'" link type="danger" size="small" @click="handleDefault(row)">违约</el-button>
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
    <el-drawer v-model="drawerVisible" title="质押详情" size="560px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="质押编号">{{ currentRow.pledgeCode }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="pledgeStatusType(currentRow.status)" size="small">{{ pledgeStatusLabel(currentRow.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="出质人">{{ currentRow.pledgorName }}</el-descriptions-item>
          <el-descriptions-item label="质权人">{{ currentRow.pledgeeName }}</el-descriptions-item>
          <el-descriptions-item label="质押物" :span="2">{{ currentRow.commodity }}</el-descriptions-item>
          <el-descriptions-item label="数量" class-name="nums-tabular">{{ currentRow.quantity }} {{ currentRow.unit }}</el-descriptions-item>
          <el-descriptions-item label="评估价值(元)" class-name="nums-tabular">{{ currentRow.appraisedValue }}</el-descriptions-item>
          <el-descriptions-item label="质押率">{{ currentRow.pledgeRate != null ? (currentRow.pledgeRate * 100).toFixed(0) + '%' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="贷款金额(元)" class-name="nums-tabular">{{ currentRow.loanAmount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="仓单编号">{{ currentRow.receiptCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRow.startDate" label="起始日期">{{ currentRow.startDate }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRow.endDate" label="到期日期">{{ currentRow.endDate }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑质押' : '新增质押'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="出质人" prop="pledgorName">
          <el-input v-model="form.pledgorName" />
        </el-form-item>
        <el-form-item label="质权人" prop="pledgeeName">
          <el-input v-model="form.pledgeeName" />
        </el-form-item>
        <el-form-item label="质押物" prop="commodity">
          <el-input v-model="form.commodity" />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" style="width:120px" />
        </el-form-item>
        <el-form-item label="评估价值(元)" prop="appraisedValue">
          <el-input-number v-model="form.appraisedValue" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="质押率(%)">
          <el-input-number v-model="form.pledgeRate" :min="0" :max="1" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="贷款金额(元)">
          <el-input-number v-model="form.loanAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="仓单编号">
          <el-input v-model="form.receiptCode" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pledgeApi } from '@/api/finance.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', status: '' })

const drawerVisible = ref(false)
const currentRow = ref(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ id: null, pledgorName: '', pledgeeName: '', commodity: '', quantity: 0, unit: 'kg', appraisedValue: null, pledgeRate: null, loanAmount: null, receiptCode: '' })
const formRules = {
  pledgorName: [{ required: true, message: '请输入出质人', trigger: 'blur' }],
  pledgeeName: [{ required: true, message: '请输入质权人', trigger: 'blur' }],
  commodity: [{ required: true, message: '请输入质押物', trigger: 'blur' }],
  appraisedValue: [{ required: true, message: '请输入评估价值', trigger: 'blur' }],
}

function rowStatus(row) {
  return row.status?.code || row.status
}

function pledgeStatusType(status) {
  const code = status?.code || status
  return { PENDING: 'info', ACTIVE: 'success', RELEASED: 'primary', DEFAULTED: 'danger' }[code] || 'info'
}

function pledgeStatusLabel(status) {
  const code = status?.code || status
  return { PENDING: '待激活', ACTIVE: '已激活', RELEASED: '已释放', DEFAULTED: '已违约' }[code] || status
}

async function loadData() {
  loading.value = true
  try {
    const res = await pledgeApi.list(query)
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
  Object.assign(form, { id: null, pledgorName: '', pledgeeName: '', commodity: '', quantity: 0, unit: 'kg', appraisedValue: null, pledgeRate: null, loanAmount: null, receiptCode: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    pledgorName: row.pledgorName || '',
    pledgeeName: row.pledgeeName || '',
    commodity: row.commodity || '',
    quantity: row.quantity,
    unit: row.unit || 'kg',
    appraisedValue: row.appraisedValue,
    pledgeRate: row.pledgeRate,
    loanAmount: row.loanAmount,
    receiptCode: row.receiptCode || '',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await pledgeApi.update(form.id, form)
    } else {
      await pledgeApi.create(form)
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

async function handleActivate(row) {
  await ElMessageBox.confirm(`确认激活质押 ${row.pledgeCode}?`, '提示', { type: 'warning' })
  try {
    await pledgeApi.activate(row.id)
    ElMessage.success('激活成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleRelease(row) {
  await ElMessageBox.confirm(`确认释放质押 ${row.pledgeCode}?`, '提示', { type: 'warning' })
  try {
    await pledgeApi.release(row.id)
    ElMessage.success('释放成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleDefault(row) {
  await ElMessageBox.confirm(`确认将质押 ${row.pledgeCode} 标记为违约?`, '警告', { type: 'warning', confirmButtonText: '确认违约', confirmButtonClass: 'el-button--danger' })
  try {
    await pledgeApi.defaultPledge(row.id)
    ElMessage.success('已标记违约')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除质押 ${row.pledgeCode}?`, '提示', { type: 'warning' })
  try {
    await pledgeApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
