<template>
  <div class="page-container">
    <div class="page-header">
      <h2>冷链统计</h2>
    </div>

    <!-- KPI Cards -->
    <el-row :gutter="16" style="margin-bottom:24px" v-loading="summaryLoading">
      <el-col :span="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-label">总任务数</div>
          <div class="kpi-value nums-tabular">{{ summary.totalTasks ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-label">运输中</div>
          <div class="kpi-value nums-tabular" style="color:var(--el-color-primary)">{{ summary.taskByStatus?.IN_TRANSIT ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-label">车辆总数</div>
          <div class="kpi-value nums-tabular">{{ summary.totalVehicles ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="kpi-card">
          <div class="kpi-label">温度报警</div>
          <div class="kpi-value nums-tabular" style="color:#D32F2F">{{ summary.totalAlarms ?? '-' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Task Status Distribution -->
    <el-row :gutter="16" style="margin-bottom:24px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>任务状态分布</span></template>
          <div v-if="summary.taskByStatus">
            <el-tag v-for="(count, status) in summary.taskByStatus" :key="status" :type="taskStatusType(status)" style="margin:4px" size="large">
              {{ taskStatusLabel(status) }}: {{ count }}
            </el-tag>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>车辆状态分布</span></template>
          <div v-if="summary.vehicleByStatus">
            <el-tag v-for="(count, status) in summary.vehicleByStatus" :key="status" :type="vehicleStatusType(status)" style="margin:4px" size="large">
              {{ vehicleStatusLabel(status) }}: {{ count }}
            </el-tag>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Alarm Records -->
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>温度报警记录（近{{ alarmDays }}天）</span>
          <el-select v-model="alarmDays" style="width:120px" @change="loadAlarms">
            <el-option :label="'近7天'" :value="7" />
            <el-option :label="'近30天'" :value="30" />
            <el-option :label="'近90天'" :value="90" />
          </el-select>
        </div>
      </template>
      <div v-loading="alarmLoading">
        <div style="margin-bottom:12px" v-if="alarmData.totalAlarms != null">
          <span>共 <strong class="nums-tabular">{{ alarmData.totalAlarms }}</strong> 条报警</span>
          <span v-if="alarmData.alarmByTask" style="margin-left:16px;color:var(--el-text-color-secondary)">
            涉及 {{ Object.keys(alarmData.alarmByTask).length }} 个任务
          </span>
        </div>
        <el-table :data="alarmRecords" stripe border max-height="400" v-if="alarmRecords.length > 0">
          <el-table-column prop="taskId" label="任务ID" width="100" class-name="nums-tabular" />
          <el-table-column prop="vehicleId" label="车辆ID" width="100" class-name="nums-tabular" />
          <el-table-column prop="temperature" label="温度(°C)" width="120" class-name="nums-tabular" />
          <el-table-column prop="humidity" label="湿度(%)" width="100" class-name="nums-tabular" />
          <el-table-column prop="location" label="位置" width="160" />
          <el-table-column prop="alarmMsg" label="报警信息" min-width="160" />
          <el-table-column prop="recordTime" label="记录时间" width="180" />
        </el-table>
        <el-empty v-else description="无报警记录" :image-size="60" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { coldchainStatsApi } from '@/api/coldchain.js'

const summaryLoading = ref(false)
const alarmLoading = ref(false)
const summary = reactive({
  totalTasks: null,
  taskByStatus: null,
  totalVehicles: null,
  vehicleByStatus: null,
  totalAlarms: null,
})
const alarmDays = ref(30)
const alarmData = reactive({ totalAlarms: null, alarmByTask: null })
const alarmRecords = ref([])

function taskStatusType(code) {
  const map = { PENDING: 'info', IN_TRANSIT: 'primary', DELIVERED: 'success', CANCELLED: 'danger' }
  return map[code] || 'info'
}

function taskStatusLabel(code) {
  const map = { PENDING: '待发车', IN_TRANSIT: '运输中', DELIVERED: '已送达', CANCELLED: '已取消' }
  return map[code] || code
}

function vehicleStatusType(code) {
  const map = { IDLE: 'success', IN_TRANSIT: 'primary', MAINTENANCE: 'warning', RETIRED: 'info' }
  return map[code] || 'info'
}

function vehicleStatusLabel(code) {
  const map = { IDLE: '空闲', IN_TRANSIT: '运输中', MAINTENANCE: '维护中', RETIRED: '已报废' }
  return map[code] || code
}

async function loadSummary() {
  summaryLoading.value = true
  try {
    const res = await coldchainStatsApi.summary()
    if (res) {
      summary.totalTasks = res.totalTasks
      summary.taskByStatus = res.taskByStatus
      summary.totalVehicles = res.totalVehicles
      summary.vehicleByStatus = res.vehicleByStatus
      summary.totalAlarms = res.totalAlarms
    }
  } catch (e) {
    console.error(e)
  } finally {
    summaryLoading.value = false
  }
}

async function loadAlarms() {
  alarmLoading.value = true
  try {
    const res = await coldchainStatsApi.alarms({ days: alarmDays.value })
    if (res) {
      alarmData.totalAlarms = res.totalAlarms
      alarmData.alarmByTask = res.alarmByTask
      alarmRecords.value = res.records || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    alarmLoading.value = false
  }
}

onMounted(async () => {
  await loadSummary()
  await loadAlarms()
})
</script>

<style scoped>
.kpi-card {
  text-align: center;
  padding: 8px 0;
}
.kpi-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}
.kpi-value {
  font-size: 32px;
  font-weight: 600;
  line-height: 1.2;
}
</style>
