<template>
  <div class="page-container">
    <div class="page-header">
      <h2>采购管理</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增采购</el-button>
    </div>
    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索采购单号/供应商"
        clearable
        style="width:220px"
        @clear="loadData"
        @keyup.enter="loadData"
      />
      <el-select v-model="query.status" placeholder="采购状态" clearable style="width:140px" @change="loadData">
        <el-option label="待审批" value="PENDING" />
        <el-option label="已审批" value="APPROVED" />
        <el-option label="已收货" value="RECEIVED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="purchaseNo" label="采购单号" width="180" />
      <el-table-column prop="supplierName" label="供应商" min-width="140" show-overflow-tooltip />
      <el-table-column prop="productName" label="产品名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="quantity" label="数量" width="100" class-name="nums-tabular" />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="unitPrice" label="单价" width="100" class-name="nums-tabular" />
      <el-table-column prop="totalAmount" label="总金额" width="120" class-name="nums-tabular" />
      <el-table-column prop="purchaseDate" label="采购日期" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="purchaseStatusType(row.status?.code || row.status)" size="small">
            {{ row.status?.desc || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button
              link type="primary" size="small"
              @click="openEdit(row)"
              v-if="(row.status?.code || row.status) === 'PENDING'"
            >编辑</el-button>
            <el-button
              link type="success" size="small"
              @click="handleApprove(row)"
              v-if="(row.status?.code || row.status) === 'PENDING'"
            >审批</el-button>
            <el-button
              link type="primary" size="small"
              @click="handleReceive(row)"
              v-if="(row.status?.code || row.status) === 'APPROVED'"
            >收货</el-button>
            <el-button
              link type="warning" size="small"
              @click="handleCancel(row)"
              v-if="(row.status?.code || row.status) === 'PENDING'"
            >取消</el-button>
            <el-button
              link type="danger" size="small"
              @click="handleDelete(row)"
              v-if="(row.status?.code || row.status) === 'PENDING'"
            >删除</el-button>
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
    <el-drawer v-model="drawerVisible" title="采购详情" size="520px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="采购单号" :span="2">{{ currentRow.purchaseNo }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ currentRow.supplierName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ currentRow.productName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="数量" class-name="nums-tabular">{{ currentRow.quantity }}</el-descriptions-item>
          <el-descriptions-item label="单位">{{ currentRow.unit || '-' }}</el-descriptions-item>
          <el-descriptions-item label="单价" class-name="nums-tabular">{{ currentRow.unitPrice || '-' }}</el-descriptions-item>
          <el-descriptions-item label="总金额" class-name="nums-tabular">{{ currentRow.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="采购日期">{{ currentRow.purchaseDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="purchaseStatusType(currentRow.status?.code || currentRow.status)" size="small">
              {{ currentRow.status?.desc || currentRow.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRow.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑采购' : '新增采购'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="供应商ID" prop="supplierId">
          <el-input-number v-model="form.supplierId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="供应商名���">
          <el-input v-model="form.supplierName" />
        </el-form-item>
        <el-form-item label="产品ID" prop="productId">
          <el-input-number v-model="form.productId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="kg/L/袋/瓶" />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="总金额">
          <el-input-number v-model="form.totalAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="采���日期" prop="purchaseDate">
          <el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
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

    <!-- Approve Dialog removed: backend approve endpoint takes no body, just confirms directly -->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { purchaseApi } from '@/api/input.js'
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
const form = reactive({
  id: null,
  supplierId: null,
  supplierName: '',
  productId: null,
  productName: '',
  quantity: 0,
  unit: '',
  unitPrice: 0,
  totalAmount: 0,
  purchaseDate: '',
  remark: ''
})
const formRules = {
  supplierId: [{ required: true, message: '请输入供应商ID', trigger: 'blur' }],
  productId: [{ required: true, message: '请输入产品ID', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  purchaseDate: [{ required: true, message: '请选择采购日期', trigger: 'change' }]
}


function purchaseStatusType(status) {
  const map = { PENDING: 'warning', APPROVED: 'success', RECEIVED: 'primary', CANCELLED: 'info' }
  return map[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await purchaseApi.list(query)
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
  Object.assign(form, { id: null, supplierId: null, supplierName: '', productId: null, productName: '', quantity: 0, unit: '', unitPrice: 0, totalAmount: 0, purchaseDate: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    supplierId: row.supplierId,
    supplierName: row.supplierName || '',
    productId: row.productId,
    productName: row.productName || '',
    quantity: row.quantity || 0,
    unit: row.unit || '',
    unitPrice: row.unitPrice || 0,
    totalAmount: row.totalAmount || 0,
    purchaseDate: row.purchaseDate || '',
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function handleApprove(row) {
  await ElMessageBox.confirm(`确认审批通过采购单 "${row.purchaseNo}"?`, '提示', { type: 'info' })
  try {
    await purchaseApi.approve(row.id)
    ElMessage.success('审批通过')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleReceive(row) {
  await ElMessageBox.confirm(`确认对采购单 "${row.purchaseNo}" 执行收货操作?`, '提示', { type: 'info' })
  try {
    await purchaseApi.receive(row.id)
    ElMessage.success('收货成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleCancel(row) {
  await ElMessageBox.confirm(`确认取消采购单 "${row.purchaseNo}"?`, '提示', { type: 'warning' })
  try {
    await purchaseApi.cancel(row.id)
    ElMessage.success('已取消')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await purchaseApi.update(form.id, form)
    } else {
      await purchaseApi.create(form)
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

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除采购单 "${row.purchaseNo}"?`, '提示', { type: 'warning' })
  try {
    await purchaseApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
