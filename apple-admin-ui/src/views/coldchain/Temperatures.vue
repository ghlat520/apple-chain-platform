<template>
  <div class="page-container">
    <div class="page-header">
      <h2>温度记录</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增记录</el-button>
    </div>
    <div class="filter-bar">
      <el-input v-model="query.taskId" placeholder="输入任务ID" clearable style="width:160px" @clear="loadData" />
      <el-select v-model="query.isAlarm" placeholder="报警状态" clearable style="width:140px" @change="loadData">
        <el-option label="正常" :value="0" />
        <el-option label="报警" :value="1" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">分页查询</el-button>
      <el-button @click="loadByTask" icon="List">按任务查全部</el-button>
    </div>

    <el-table
      :data="tableData"
      stripe
      v-loading="loading"
      border
      :row-class-name="alarmRowClass"
    >
      <el-table-column prop="taskId" label="任务ID" width="100" class-name="nums-tabular" />
      <el-table-column prop="vehicleId" label="车辆ID" width="100" class-name="nums-tabular" />
      <el-table-column prop="recordTime" label="记录时间" width="180" />
      <el-table-column prop="temperature" label="温度(°C)" width="120" class-name="nums-tabular" />
      <el-table-column prop="humidity" label="湿度(%)" width="110" class-name="nums-tabular" />
      <el-table-column prop="location" label="位置" width="160" />
      <el-table-column label="是否报警" width="110">
        <template #default="{ row }">
          <el-tag :type="row.isAlarm === 1 ? 'danger' : 'success'" size="small">
            {{ row.isAlarm === 1 ? '报警' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="alarmMsg" label="报警信息" min-width="160" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
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
      :page-size="pageQuery.size"
      :current-page="pageQuery.page"
      @current-change="(p) => { pageQuery.page = p; loadData() }"
    />

    <!-- Create Dialog -->
    <el-dialog v-model="dialogVisible" title="新增温度记录" width="480px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="任务ID" prop="taskId">
          <el-input-number v-model="form.taskId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="车辆ID">
          <el-input-number v-model="form.vehicleId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="温度(°C)" prop="temperature">
          <el-input-number v-model="form.temperature" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="湿度(%)">
          <el-input-number v-model="form.humidity" :min="0" :max="100" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="记录时间" prop="recordTime">
          <el-date-picker v-model="form.recordTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="是否报警">
          <el-select v-model="form.isAlarm" style="width:100%">
            <el-option label="正常" :value="0" />
            <el-option label="报警" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.isAlarm === 1" label="报警信息">
          <el-input v-model="form.alarmMsg" type="textarea" :rows="2" />
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
import { temperatureApi } from '@/api/coldchain.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ taskId: '', isAlarm: '' })
const pageQuery = reactive({ page: 1, size: 20 })

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  taskId: null,
  vehicleId: null,
  temperature: 0,
  humidity: 50,
  location: '',
  recordTime: '',
  isAlarm: 0,
  alarmMsg: '',
})
const formRules = {
  taskId: [{ required: true, message: '请输入任务ID', trigger: 'blur' }],
  temperature: [{ required: true, message: '请输入温度', trigger: 'blur' }],
  recordTime: [{ required: true, message: '请选择记录时间', trigger: 'change' }],
}

function alarmRowClass({ row }) {
  return row.isAlarm === 1 ? 'alarm-row' : ''
}

async function loadData() {
  loading.value = true
  try {
    const params = { ...pageQuery }
    if (query.taskId) params.taskId = query.taskId
    if (query.isAlarm !== '' && query.isAlarm !== null) params.isAlarm = query.isAlarm
    const res = await temperatureApi.list(params)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadByTask() {
  if (!query.taskId) {
    ElMessage.warning('请先输入任务ID')
    return
  }
  loading.value = true
  try {
    const res = await temperatureApi.byTask({ taskId: query.taskId })
    // byTask returns List not PageResult - handle plain array
    const raw = Array.isArray(res) ? res : (res?.records || res?.list || res || [])
    tableData.value = Array.isArray(raw) ? raw : []
    total.value = tableData.value.length
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { taskId: query.taskId ? Number(query.taskId) : null, vehicleId: null, temperature: 0, humidity: 50, location: '', recordTime: '', isAlarm: 0, alarmMsg: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await temperatureApi.create(form)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该温度记录?', '提示', { type: 'warning' })
  try {
    await temperatureApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>

<style scoped>
:deep(.alarm-row) {
  background-color: var(--el-color-danger-light-9, #fef0f0);
}
:deep(.alarm-row:hover > td) {
  background-color: var(--el-color-danger-light-8, #fde2e2) !important;
}
</style>
