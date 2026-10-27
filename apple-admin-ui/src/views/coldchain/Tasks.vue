<template>
  <div class="page-container">
    <div class="page-header">
      <h2>运输任务</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增任务</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索任务编号/目的地" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:140px" @change="loadData">
        <el-option label="待发车" value="PENDING" />
        <el-option label="运输中" value="IN_TRANSIT" />
        <el-option label="已送达" value="DELIVERED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="taskCode" label="任务编号" width="180" class-name="nums-tabular" />
      <el-table-column prop="vehicleId" label="车辆ID" width="100" class-name="nums-tabular" />
      <el-table-column prop="orderId" label="订单ID" width="100" class-name="nums-tabular" />
      <el-table-column prop="origin" label="出发地" width="140" />
      <el-table-column prop="destination" label="目的地" width="140" />
      <el-table-column prop="cargoDesc" label="货物描述" width="140" />
      <el-table-column prop="cargoWeight" label="货物重量(吨)" width="120" class-name="nums-tabular" />
      <el-table-column prop="requiredTemp" label="要求温度(°C)" width="120" class-name="nums-tabular" />
      <el-table-column prop="planDepart" label="计划发车" width="180" />
      <el-table-column prop="actualDepart" label="实际发车" width="180" />
      <el-table-column prop="planArrive" label="计划到达" width="180" />
      <el-table-column prop="actualArrive" label="实际到达" width="180" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button
              v-if="row.status === 'PENDING'"
              link type="primary" size="small"
              @click="handleDepart(row)"
            >发车</el-button>
            <el-button
              v-if="row.status === 'IN_TRANSIT'"
              link type="success" size="small"
              @click="handleDeliver(row)"
            >确认送达</el-button>
            <el-button
              v-if="['PENDING', 'IN_TRANSIT'].includes(row.status)"
              link type="warning" size="small"
              @click="handleCancel(row)"
            >取消</el-button>
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

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑任务' : '新增任务'" width="560px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="车辆ID" prop="vehicleId">
          <el-input-number v-model="form.vehicleId" :min="1" style="width:100%" placeholder="关联车辆ID" />
        </el-form-item>
        <el-form-item label="订单ID">
          <el-input-number v-model="form.orderId" :min="0" style="width:100%" placeholder="关联订单ID(可选)" />
        </el-form-item>
        <el-form-item label="出发地" prop="origin">
          <el-input v-model="form.origin" />
        </el-form-item>
        <el-form-item label="目的地" prop="destination">
          <el-input v-model="form.destination" />
        </el-form-item>
        <el-form-item label="货物描述">
          <el-input v-model="form.cargoDesc" />
        </el-form-item>
        <el-form-item label="货物重量(吨)">
          <el-input-number v-model="form.cargoWeight" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="要求温度(°C)">
          <el-input-number v-model="form.requiredTemp" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="计划发车时间" prop="planDepart">
          <el-date-picker v-model="form.planDepart" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="计划到达时间">
          <el-date-picker v-model="form.planArrive" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
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
import { taskApi } from '@/api/coldchain.js'
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
  vehicleId: null,
  orderId: null,
  origin: '',
  destination: '',
  cargoDesc: '',
  cargoWeight: 0,
  requiredTemp: 2,
  planDepart: '',
  planArrive: '',
  remark: '',
})
const formRules = {
  vehicleId: [{ required: true, message: '请输入车辆ID', trigger: 'blur' }],
  origin: [{ required: true, message: '请输入出发地', trigger: 'blur' }],
  destination: [{ required: true, message: '请输入目的地', trigger: 'blur' }],
  planDepart: [{ required: true, message: '请选择计划发车时间', trigger: 'change' }],
}

function statusType(code) {
  const map = { PENDING: 'info', IN_TRANSIT: 'primary', DELIVERED: 'success', CANCELLED: 'danger' }
  return map[code] || 'info'
}

function statusLabel(code) {
  const map = { PENDING: '待发车', IN_TRANSIT: '运输中', DELIVERED: '已送达', CANCELLED: '已取消' }
  return map[code] || code
}

async function loadData() {
  loading.value = true
  try {
    const res = await taskApi.list(query)
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
  Object.assign(form, { id: null, vehicleId: null, orderId: null, origin: '', destination: '', cargoDesc: '', cargoWeight: 0, requiredTemp: 2, planDepart: '', planArrive: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    vehicleId: row.vehicleId,
    orderId: row.orderId,
    origin: row.origin,
    destination: row.destination,
    cargoDesc: row.cargoDesc || '',
    cargoWeight: row.cargoWeight || 0,
    requiredTemp: row.requiredTemp || 2,
    planDepart: row.planDepart || '',
    planArrive: row.planArrive || '',
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
      await taskApi.update(form.id, form)
    } else {
      await taskApi.create(form)
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

async function handleDepart(row) {
  await ElMessageBox.confirm(`确认为任务 ${row.taskCode} 执行发车?`, '提示', { type: 'warning' })
  try {
    await taskApi.depart(row.id)
    ElMessage.success('发车成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleDeliver(row) {
  await ElMessageBox.confirm(`确认任务 ${row.taskCode} 已送达?`, '提示', { type: 'warning' })
  try {
    await taskApi.deliver(row.id)
    ElMessage.success('已确认送达')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleCancel(row) {
  await ElMessageBox.confirm(`确认取消任务 ${row.taskCode}?`, '提示', { type: 'warning' })
  try {
    await taskApi.cancel(row.id)
    ElMessage.success('任务已取消')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除任务 ${row.taskCode}?`, '提示', { type: 'warning' })
  try {
    await taskApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
