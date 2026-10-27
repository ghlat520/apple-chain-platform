<template>
  <div class="page-container">
    <div class="page-header">
      <h2>仓库管理</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增仓库</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索名称/地址" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.type" placeholder="类型" clearable style="width:120px" @change="loadData">
        <el-option label="普通仓库" value="NORMAL" />
        <el-option label="冷库" value="COLD" />
        <el-option label="气调库" value="ATMOSPHERE" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width:120px" @change="loadData">
        <el-option label="启用" value="ACTIVE" />
        <el-option label="维护中" value="MAINTENANCE" />
        <el-option label="已满" value="FULL" />
        <el-option label="已关闭" value="CLOSED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="name" label="仓库名称" width="180" />
      <el-table-column prop="warehouseCode" label="仓库编码" width="160" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ typeLabel(row.type) }}</template>
      </el-table-column>
      <el-table-column prop="location" label="地址" min-width="200" />
      <el-table-column prop="capacity" label="容量(吨)" width="110" class-name="nums-tabular" />
      <el-table-column prop="usedCapacity" label="已用(吨)" width="110" class-name="nums-tabular" />
      <el-table-column prop="manager" label="负责人" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="viewDetail(row)">
              详情
              <el-badge v-if="alertCounts[row.id]" :value="alertCounts[row.id]" :max="99" style="margin-left:2px" />
            </el-button>
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

    <!-- Detail Drawer -->
    <el-drawer v-model="drawerVisible" title="仓库详情" size="600px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="仓库名称">{{ currentRow.name }}</el-descriptions-item>
          <el-descriptions-item label="仓库编码">{{ currentRow.warehouseCode }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ typeLabel(currentRow.type) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(currentRow.status)" size="small">{{ statusLabel(currentRow.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="容量(吨)" class-name="nums-tabular">{{ currentRow.capacity }}</el-descriptions-item>
          <el-descriptions-item label="已用(吨)" class-name="nums-tabular">{{ currentRow.usedCapacity ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ currentRow.manager || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentRow.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ currentRow.location || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRow.temperature != null" label="温度" class-name="nums-tabular">{{ currentRow.temperature }}&#8451;</el-descriptions-item>
          <el-descriptions-item v-if="currentRow.humidity != null" label="湿度" class-name="nums-tabular">{{ currentRow.humidity }}%</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ currentRow.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">{{ currentRow.updateTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="drawerAlerts.length" style="margin-top:20px">
          <h4 style="margin:0 0 10px">
            预警信息
            <el-badge :value="drawerAlerts.length" :max="99" style="margin-left:6px" />
          </h4>
          <el-table :data="drawerAlerts" stripe border size="small">
            <el-table-column prop="alertType" label="预警类型" width="120" />
            <el-table-column prop="alertMessage" label="信息" />
            <el-table-column prop="alertTime" label="时间" width="160" />
          </el-table>
        </div>
      </template>
    </el-drawer>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑仓库' : '新增仓库'" width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="仓库名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="仓库编码">
          <el-input v-model="form.warehouseCode" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width:100%">
            <el-option label="普通仓库" value="NORMAL" />
            <el-option label="冷库" value="COLD" />
            <el-option label="气调库" value="ATMOSPHERE" />
          </el-select>
        </el-form-item>
        <el-form-item label="容量(吨)" prop="capacity">
          <el-input-number v-model="form.capacity" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.location" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.manager" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
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
    <el-dialog v-model="statusDialogVisible" title="变更仓库状态" width="380px">
      <el-form label-width="80px">
        <el-form-item label="新状态">
          <el-select v-model="newStatus" style="width:100%">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="已满" value="FULL" />
            <el-option label="已关闭" value="CLOSED" />
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
import { warehouseApi } from '@/api/warehouse.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', type: '', status: '' })
const alertCounts = ref({})

const drawerVisible = ref(false)
const currentRow = ref(null)
const drawerAlerts = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  name: '',
  warehouseCode: '',
  type: 'NORMAL',
  capacity: 0,
  location: '',
  manager: '',
  phone: '',
  remark: ''
})
const formRules = {
  name: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }]
}

const statusDialogVisible = ref(false)
const newStatus = ref('ACTIVE')
const statusTarget = ref(null)

function typeLabel(type) {
  const map = { NORMAL: '普通仓库', COLD: '冷库', ATMOSPHERE: '气调库' }
  return map[type] || type
}

function statusType(status) {
  const map = { ACTIVE: 'success', MAINTENANCE: 'warning', FULL: 'danger', CLOSED: 'info' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { ACTIVE: '启用', MAINTENANCE: '维护中', FULL: '已满', CLOSED: '已关闭' }
  return map[status] || status
}

async function loadData() {
  loading.value = true
  try {
    const res = await warehouseApi.list(query)
    const raw = res?.records || res?.list || res
    tableData.value = Array.isArray(raw) ? raw : []
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
  drawerAlerts.value = []
  try {
    const res = await warehouseApi.alerts()
    drawerAlerts.value = res?.records || res?.list || res || []
  } catch (e) {
    drawerAlerts.value = []
  }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    warehouseCode: '',
    type: 'NORMAL',
    capacity: 0,
    location: '',
    manager: '',
    phone: '',
    remark: ''
  })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    warehouseCode: row.warehouseCode || '',
    name: row.name,
    type: row.type || 'NORMAL',
    capacity: row.capacity,
    location: row.location || '',
    manager: row.manager || '',
    phone: row.phone || '',
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
      await warehouseApi.update(form.id, form)
    } else {
      await warehouseApi.create(form)
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
    await warehouseApi.updateStatus(statusTarget.value.id, { status: newStatus.value })
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
  await ElMessageBox.confirm(`确认删除仓库 ${row.name}?`, '提示', { type: 'warning' })
  try {
    await warehouseApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
