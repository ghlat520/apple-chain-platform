<template>
  <div class="page-container">
    <div class="page-header">
      <h2>农资产品</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增产品</el-button>
    </div>
    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索产品名称/登记证号"
        clearable
        style="width:220px"
        @clear="loadData"
        @keyup.enter="loadData"
      />
      <el-select v-model="query.type" placeholder="产品类型" clearable style="width:140px" @change="loadData">
        <el-option label="农药" value="PESTICIDE" />
        <el-option label="化肥" value="FERTILIZER" />
        <el-option label="种子" value="SEED" />
        <el-option label="农具" value="TOOL" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="productCode" label="产品编号" width="140" />
      <el-table-column prop="name" label="产品名称" min-width="160" />
      <el-table-column label="产品类型" width="110">
        <template #default="{ row }">
          {{ row.type?.desc || row.type || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="spec" label="规格型号" width="140" show-overflow-tooltip />
      <el-table-column prop="manufacturer" label="生产厂家" min-width="160" show-overflow-tooltip />
      <el-table-column prop="batchNo" label="批号" width="140" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="productStatusType(row.status)" size="small">{{ productStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
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
    <el-drawer v-model="drawerVisible" title="产品详情" size="500px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="产品编号">{{ currentRow.productCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="产品名称" :span="2">{{ currentRow.name }}</el-descriptions-item>
          <el-descriptions-item label="产品类型">{{ currentRow.type?.desc || currentRow.type || '-' }}</el-descriptions-item>
          <el-descriptions-item label="规格型号">{{ currentRow.spec || '-' }}</el-descriptions-item>
          <el-descriptions-item label="生产厂家" :span="2">{{ currentRow.manufacturer || '-' }}</el-descriptions-item>
          <el-descriptions-item label="批号">{{ currentRow.batchNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="登记证号">{{ currentRow.registration || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="productStatusType(currentRow.status)" size="small">{{ productStatusLabel(currentRow.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="生产日期">{{ currentRow.productionDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="过期日期">{{ currentRow.expiryDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRow.createTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑产品' : '新增产品'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="产品名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="产品类型" prop="type">
          <el-select v-model="form.type" style="width:100%">
            <el-option label="农药" value="PESTICIDE" />
            <el-option label="化肥" value="FERTILIZER" />
            <el-option label="种子" value="SEED" />
            <el-option label="农具" value="TOOL" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="form.spec" />
        </el-form-item>
        <el-form-item label="生产厂家">
          <el-input v-model="form.manufacturer" />
        </el-form-item>
        <el-form-item label="批号">
          <el-input v-model="form.batchNo" />
        </el-form-item>
        <el-form-item label="登记证号">
          <el-input v-model="form.registration" />
        </el-form-item>
        <el-form-item label="生产日期">
          <el-date-picker v-model="form.productionDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="过期日期">
          <el-date-picker v-model="form.expiryDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { agriProductApi } from '@/api/input.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', type: '' })

const drawerVisible = ref(false)
const currentRow = ref(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  name: '',
  type: '',
  spec: '',
  manufacturer: '',
  batchNo: '',
  registration: '',
  productionDate: '',
  expiryDate: '',
  remark: ''
})
const formRules = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择产品类型', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await agriProductApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function productStatusType(status) {
  const map = { ACTIVE: 'success', DISCONTINUED: 'info' }
  return map[status] || 'info'
}

function productStatusLabel(status) {
  const map = { ACTIVE: '在售', DISCONTINUED: '已停产' }
  return map[status] || status || '-'
}

function viewDetail(row) {
  currentRow.value = row
  drawerVisible.value = true
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', type: '', spec: '', manufacturer: '', batchNo: '', registration: '', productionDate: '', expiryDate: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    type: row.type?.code || row.type || '',
    spec: row.spec || '',
    manufacturer: row.manufacturer || '',
    batchNo: row.batchNo || '',
    registration: row.registration || '',
    productionDate: row.productionDate || '',
    expiryDate: row.expiryDate || '',
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
      await agriProductApi.update(form.id, form)
    } else {
      await agriProductApi.create(form)
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
  await ElMessageBox.confirm(`确认删除产品 "${row.name}"?`, '提示', { type: 'warning' })
  try {
    await agriProductApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
