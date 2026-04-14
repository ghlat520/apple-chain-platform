<template>
  <div class="page-container">
    <div class="page-header">
      <h2>车辆管理</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增车辆</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索车牌/司机" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.vehicleType" placeholder="车辆类型" clearable style="width:140px" @change="loadData">
        <el-option label="冷藏车" value="REFRIGERATED" />
        <el-option label="保温车" value="INSULATED" />
        <el-option label="普通车" value="NORMAL" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width:140px" @change="loadData">
        <el-option label="空闲" value="IDLE" />
        <el-option label="运输中" value="IN_TRANSIT" />
        <el-option label="维护中" value="MAINTENANCE" />
        <el-option label="已报废" value="RETIRED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="vehicleCode" label="车辆编码" width="150" class-name="nums-tabular" />
      <el-table-column prop="plateNumber" label="车牌号" width="140" />
      <el-table-column label="车辆类型" width="110">
        <template #default="{ row }">{{ vehicleTypeLabel(row.vehicleType) }}</template>
      </el-table-column>
      <el-table-column prop="brand" label="品牌" width="100" />
      <el-table-column prop="capacity" label="载重(吨)" width="110" class-name="nums-tabular" />
      <el-table-column prop="volume" label="容积(m³)" width="110" class-name="nums-tabular" />
      <el-table-column prop="driverName" label="司机姓名" width="110" />
      <el-table-column prop="driverPhone" label="司机电话" width="140" class-name="nums-tabular" />
      <el-table-column label="温度范围(°C)" width="150" class-name="nums-tabular">
        <template #default="{ row }">{{ row.temperatureMin }} ~ {{ row.temperatureMax }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="openStatusUpdate(row)">状态</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑车辆' : '新增车辆'" width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="车辆编码">
          <el-input v-model="form.vehicleCode" placeholder="系统自动生成或手动输入" />
        </el-form-item>
        <el-form-item label="车牌号" prop="plateNumber">
          <el-input v-model="form.plateNumber" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="车辆类型" prop="vehicleType">
          <el-select v-model="form.vehicleType" style="width:100%">
            <el-option label="冷藏车" value="REFRIGERATED" />
            <el-option label="保温车" value="INSULATED" />
            <el-option label="普通车" value="NORMAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="form.brand" placeholder="如：中集、冰熊" />
        </el-form-item>
        <el-form-item label="载重(吨)" prop="capacity">
          <el-input-number v-model="form.capacity" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="容积(m³)">
          <el-input-number v-model="form.volume" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="司机姓名" prop="driverName">
          <el-input v-model="form.driverName" />
        </el-form-item>
        <el-form-item label="司机电话" prop="driverPhone">
          <el-input v-model="form.driverPhone" />
        </el-form-item>
        <el-form-item label="最低温度(°C)">
          <el-input-number v-model="form.temperatureMin" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="最高温度(°C)">
          <el-input-number v-model="form.temperatureMax" :precision="1" style="width:100%" />
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

    <!-- Status Update Dialog -->
    <el-dialog v-model="statusVisible" title="更新车辆状态" width="380px">
      <el-form label-width="80px">
        <el-form-item label="状态">
          <el-select v-model="statusForm.status" style="width:100%">
            <el-option label="空闲" value="IDLE" />
            <el-option label="运输中" value="IN_TRANSIT" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="已报废" value="RETIRED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStatusUpdate" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { vehicleApi } from '@/api/coldchain.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', vehicleType: '', status: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  vehicleCode: '',
  plateNumber: '',
  vehicleType: 'REFRIGERATED',
  brand: '',
  capacity: 0,
  volume: 0,
  driverName: '',
  driverPhone: '',
  temperatureMin: -20,
  temperatureMax: 5,
  remark: '',
})
const formRules = {
  plateNumber: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
  vehicleType: [{ required: true, message: '请选择车辆类型', trigger: 'change' }],
  driverName: [{ required: true, message: '请输入司机姓名', trigger: 'blur' }],
  driverPhone: [{ required: true, message: '请输入司机电话', trigger: 'blur' }],
}

const statusVisible = ref(false)
const statusForm = reactive({ id: null, status: '' })

function vehicleTypeLabel(type) {
  const map = { REFRIGERATED: '冷藏车', INSULATED: '保温车', NORMAL: '普通车' }
  return map[type] || type
}

function statusType(code) {
  const map = { IDLE: 'success', IN_TRANSIT: 'primary', MAINTENANCE: 'warning', RETIRED: 'info' }
  return map[code] || 'info'
}

function statusLabel(code) {
  const map = { IDLE: '空闲', IN_TRANSIT: '运输中', MAINTENANCE: '维护中', RETIRED: '已报废' }
  return map[code] || code
}

async function loadData() {
  loading.value = true
  try {
    const res = await vehicleApi.list(query)
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
  Object.assign(form, { id: null, vehicleCode: '', plateNumber: '', vehicleType: 'REFRIGERATED', brand: '', capacity: 0, volume: 0, driverName: '', driverPhone: '', temperatureMin: -20, temperatureMax: 5, remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    vehicleCode: row.vehicleCode || '',
    plateNumber: row.plateNumber,
    vehicleType: row.vehicleType,
    brand: row.brand || '',
    capacity: row.capacity,
    volume: row.volume || 0,
    driverName: row.driverName,
    driverPhone: row.driverPhone,
    temperatureMin: row.temperatureMin,
    temperatureMax: row.temperatureMax,
    remark: row.remark || '',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await vehicleApi.update(form.id, form)
    } else {
      await vehicleApi.create(form)
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

function openStatusUpdate(row) {
  statusForm.id = row.id
  statusForm.status = row.status
  statusVisible.value = true
}

async function handleStatusUpdate() {
  submitting.value = true
  try {
    await vehicleApi.updateStatus(statusForm.id, statusForm.status)
    ElMessage.success('状态更新成功')
    statusVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除车辆 ${row.plateNumber}?`, '提示', { type: 'warning' })
  try {
    await vehicleApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
