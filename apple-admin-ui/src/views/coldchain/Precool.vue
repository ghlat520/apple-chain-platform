<template>
  <div class="page-container">
    <div class="page-header">
      <h2>预冷任务</h2>
      <el-button type="primary" @click="openCreate" icon="Plus">新增预冷任务</el-button>
    </div>
    <div class="filter-bar">
      <el-input-number v-model="query.vehicleId" placeholder="车辆ID" clearable style="width:160px" :min="1" :controls="false" @change="loadData" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:140px" @change="loadData">
        <el-option label="待启动" value="PENDING" />
        <el-option label="预冷中" value="COOLING" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="失败" value="FAILED" />
      </el-select>
      <el-button type="primary" @click="loadData" icon="Search">查询</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" border>
      <el-table-column prop="taskNo" label="预冷单号" width="180" class-name="nums-tabular" />
      <el-table-column prop="vehicleId" label="车辆ID" width="100" class-name="nums-tabular" />
      <el-table-column prop="batchCode" label="批次编码" width="160" class-name="nums-tabular" />
      <el-table-column prop="startTemp" label="起始温度(°C)" width="140" class-name="nums-tabular" />
      <el-table-column prop="targetTemp" label="目标温度(°C)" width="140" class-name="nums-tabular" />
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180" />
      <el-table-column prop="duration" label="时长(分钟)" width="120" class-name="nums-tabular" />
      <el-table-column prop="operator" label="操作人" width="100" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button
              v-if="row.status === 'PENDING'"
              link type="primary" size="small"
              @click="handleStart(row)"
            >启动</el-button>
            <el-button
              v-if="row.status === 'COOLING'"
              link type="success" size="small"
              @click="handleComplete(row)"
            >完成</el-button>
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

    <!-- Create Dialog -->
    <el-dialog v-model="dialogVisible" title="新增预冷任务" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="车辆ID" prop="vehicleId">
          <el-input-number v-model="form.vehicleId" :min="1" style="width:100%" placeholder="关联车辆ID" />
        </el-form-item>
        <el-form-item label="批次编码">
          <el-input v-model="form.batchCode" placeholder="种植批次编码(可选)" />
        </el-form-item>
        <el-form-item label="起始温度(°C)">
          <el-input-number v-model="form.startTemp" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="目标温度(°C)" prop="targetTemp">
          <el-input-number v-model="form.targetTemp" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="form.operator" />
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
import { precoolApi } from '@/api/coldchain.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, vehicleId: null, status: '' })

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  vehicleId: null,
  batchCode: '',
  startTemp: null,
  targetTemp: 2,
  operator: '',
  remark: '',
})
const formRules = {
  vehicleId: [{ required: true, message: '请输入车辆ID', trigger: 'blur' }],
  targetTemp: [{ required: true, message: '请输入目标温度', trigger: 'blur' }],
}

function statusType(code) {
  const map = { PENDING: 'info', COOLING: 'warning', COMPLETED: 'success', FAILED: 'danger' }
  return map[code] || 'info'
}

function statusLabel(code) {
  const map = { PENDING: '待启动', COOLING: '预冷中', COMPLETED: '已完成', FAILED: '失败' }
  return map[code] || code
}

async function loadData() {
  loading.value = true
  try {
    const res = await precoolApi.list(query)
    tableData.value = res?.records || res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { vehicleId: null, batchCode: '', startTemp: null, targetTemp: 2, operator: '', remark: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await precoolApi.create(form)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleStart(row) {
  await ElMessageBox.confirm("确认启动预冷任务 " + row.taskNo + "?", '提示', { type: 'warning' })
  try {
    await precoolApi.start(row.id)
    ElMessage.success('预冷任务已启动')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

async function handleComplete(row) {
  await ElMessageBox.confirm("确认完成预冷任务 " + row.taskNo + "?", '提示', { type: 'warning' })
  try {
    await precoolApi.complete(row.id)
    ElMessage.success('预冷任务已完成')
    loadData()
  } catch (e) {
    // error shown by interceptor
  }
}

onMounted(loadData)
</script>
