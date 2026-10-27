<template>
  <div class="page-container">
    <div class="page-header">
      <h2>仓单管理</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增仓单</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索仓单号/货品名" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:130px" @change="loadData">
        <el-option label="有效" value="VALID" />
        <el-option label="已质押" value="PLEDGED" />
        <el-option label="已转让" value="TRANSFERRED" />
        <el-option label="已注销" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="receiptNo" label="仓单号" width="160" />
      <el-table-column prop="warehouseName" label="仓库名称" width="160" />
      <el-table-column prop="farmerName" label="货主" width="120" />
      <el-table-column prop="batchCode" label="批次编码" width="140" />
      <el-table-column prop="variety" label="品种" width="100" />
      <el-table-column prop="grade" label="等级" width="80" />
      <el-table-column prop="quantity" label="数量(kg)" width="110" class-name="nums-tabular" />
      <el-table-column prop="unitValue" label="单价(元)" width="100" class-name="nums-tabular" />
      <el-table-column prop="totalValue" label="总价值(元)" width="120" class-name="nums-tabular" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="inboundDate" label="入库日期" width="120" />
      <el-table-column prop="validUntil" label="有效期至" width="120" />
      <el-table-column prop="traceCode" label="溯源码" width="140" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleStatusChange(row)">变更状态</el-button>
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

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑仓单' : '新增仓单'" width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="仓单号" prop="receiptNo">
          <el-input v-model="form.receiptNo" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="仓库ID" prop="warehouseId">
          <el-input-number v-model="form.warehouseId" :min="1" style="width:100%" placeholder="关联仓库ID" />
        </el-form-item>
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="form.warehouseName" />
        </el-form-item>
        <el-form-item label="货主ID">
          <el-input-number v-model="form.farmerId" :min="1" style="width:100%" placeholder="货主ID(可选)" />
        </el-form-item>
        <el-form-item label="货主" prop="farmerName">
          <el-input v-model="form.farmerName" />
        </el-form-item>
        <el-form-item label="品种" prop="variety">
          <el-input v-model="form.variety" />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.grade" style="width:100%">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次编码">
          <el-input v-model="form.batchCode" placeholder="种植批次编码" />
        </el-form-item>
        <el-form-item label="数量(kg)" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="单价(元)">
          <el-input-number v-model="form.unitValue" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="入库日期">
          <el-date-picker v-model="form.inboundDate" type="date" placeholder="入库日期" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="到期日期">
          <el-date-picker v-model="form.validUntil" type="date" placeholder="选择到期日" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="溯源码">
          <el-input v-model="form.traceCode" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- Status Change Dialog -->
    <el-dialog v-model="statusDialogVisible" title="变更仓单状态" width="380px">
      <el-form label-width="80px">
        <el-form-item label="新状态">
          <el-select v-model="newStatus" style="width:100%">
            <el-option label="有效" value="VALID" />
            <el-option label="已质押" value="PLEDGED" />
            <el-option label="已转让" value="TRANSFERRED" />
            <el-option label="已注销" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStatusChange" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { receiptApi } from '@/api/warehouse.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', status: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  receiptNo: '',
  warehouseId: null,
  warehouseName: '',
  farmerId: null,
  farmerName: '',
  batchCode: '',
  variety: '',
  grade: 'A',
  quantity: 0,
  unitValue: 0,
  inboundDate: '',
  validUntil: '',
  traceCode: '',
  remark: ''
})
const formRules = {
  receiptNo: [{ required: true, message: '请输入仓单号', trigger: 'blur' }],
  warehouseName: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
  farmerName: [{ required: true, message: '请输入货主', trigger: 'blur' }],
  variety: [{ required: true, message: '请输入品种', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
}

const statusDialogVisible = ref(false)
const newStatus = ref('VALID')
const statusTarget = ref(null)

function statusType(status) {
  const map = { VALID: 'success', PLEDGED: 'warning', TRANSFERRED: 'primary', CANCELLED: 'info' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { VALID: '有效', PLEDGED: '已质押', TRANSFERRED: '已转让', CANCELLED: '已注销' }
  return map[status] || status
}

async function loadData() {
  loading.value = true
  try {
    const res = await receiptApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    receiptNo: '',
    warehouseId: null,
    warehouseName: '',
    farmerId: null,
    farmerName: '',
    batchCode: '',
    variety: '',
    grade: 'A',
    quantity: 0,
    unitValue: 0,
    inboundDate: '',
    validUntil: '',
    traceCode: '',
    remark: ''
  })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    receiptNo: row.receiptNo,
    warehouseId: row.warehouseId,
    warehouseName: row.warehouseName,
    farmerId: row.farmerId,
    farmerName: row.farmerName,
    batchCode: row.batchCode || '',
    variety: row.variety,
    grade: row.grade,
    quantity: row.quantity,
    unitValue: row.unitValue,
    inboundDate: row.inboundDate || '',
    validUntil: row.validUntil || '',
    traceCode: row.traceCode || '',
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await receiptApi.update(form.id, form)
    } else {
      await receiptApi.create(form)
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

function handleStatusChange(row) {
  statusTarget.value = row
  newStatus.value = row.status
  statusDialogVisible.value = true
}

async function confirmStatusChange() {
  submitting.value = true
  try {
    await receiptApi.updateStatus(statusTarget.value.id, { status: newStatus.value })
    ElMessage.success('状态更新成功')
    statusDialogVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除仓单 ${row.receiptNo}?`, '提示', { type: 'warning' })
  try {
    await receiptApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
