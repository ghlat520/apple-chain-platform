<template>
  <div class="page-container">
    <div class="page-header">
      <h2>物流查询</h2>
    </div>
    <div class="filter-bar">
      <el-input
        v-model="taskId"
        placeholder="请输入任务ID"
        clearable
        style="width:240px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch" icon="Search" :loading="loading">查询</el-button>
    </div>

    <div v-if="loading" style="padding:40px 0;text-align:center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="result">
      <!-- Task Info -->
      <el-card style="margin-top:16px" shadow="never">
        <template #header><span>运输任务信息</span></template>
        <el-descriptions :column="2" border v-if="result.task">
          <el-descriptions-item label="任务编号">{{ result.task.taskCode }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="taskStatusType(result.task.status)" size="small">{{ taskStatusLabel(result.task.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="出发地">{{ result.task.origin }}</el-descriptions-item>
          <el-descriptions-item label="目的地">{{ result.task.destination }}</el-descriptions-item>
          <el-descriptions-item label="货物描述">{{ result.task.cargoDesc || '-' }}</el-descriptions-item>
          <el-descriptions-item label="货物重量" class-name="nums-tabular">{{ result.task.cargoWeight ? result.task.cargoWeight + ' 吨' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="要求温度" class-name="nums-tabular">{{ result.task.requiredTemp != null ? result.task.requiredTemp + '°C' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="计划发车">{{ result.task.planDepart || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际发车">{{ result.task.actualDepart || '-' }}</el-descriptions-item>
          <el-descriptions-item label="计划到达">{{ result.task.planArrive || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际到达">{{ result.task.actualArrive || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- Vehicle Info -->
      <el-card style="margin-top:16px" shadow="never" v-if="result.vehicle">
        <template #header><span>车辆信息</span></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="车牌号">{{ result.vehicle.plateNumber }}</el-descriptions-item>
          <el-descriptions-item label="车辆类型">{{ vehicleTypeLabel(result.vehicle.vehicleType) }}</el-descriptions-item>
          <el-descriptions-item label="品牌">{{ result.vehicle.brand || '-' }}</el-descriptions-item>
          <el-descriptions-item label="司机">{{ result.vehicle.driverName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="司机电话" class-name="nums-tabular">{{ result.vehicle.driverPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="温度范围" class-name="nums-tabular">{{ result.vehicle.temperatureMin }}°C ~ {{ result.vehicle.temperatureMax }}°C</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- Temperature Records Timeline -->
      <el-card style="margin-top:16px" shadow="never">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span>温度记录 ({{ (result.tempRecords || []).length }} 条)</span>
            <el-tag v-if="result.alarmCount > 0" type="danger" size="small">报警 {{ result.alarmCount }} 次</el-tag>
          </div>
        </template>
        <el-timeline v-if="result.tempRecords && result.tempRecords.length > 0">
          <el-timeline-item
            v-for="(rec, index) in result.tempRecords"
            :key="index"
            :timestamp="rec.recordTime"
            :type="rec.isAlarm === 1 ? 'danger' : 'primary'"
            placement="top"
          >
            <div>
              <span class="nums-tabular" style="font-weight:600">{{ rec.temperature }}°C</span>
              <span v-if="rec.humidity != null" style="margin-left:12px;color:var(--el-text-color-secondary)" class="nums-tabular">湿度 {{ rec.humidity }}%</span>
              <span v-if="rec.location" style="margin-left:12px;color:var(--el-text-color-secondary)">{{ rec.location }}</span>
            </div>
            <div v-if="rec.alarmMsg" style="color:var(--el-color-danger);font-size:13px;margin-top:2px">{{ rec.alarmMsg }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无温度记录" :image-size="60" />
      </el-card>

      <!-- Delivery Info -->
      <el-card style="margin-top:16px" shadow="never" v-if="result.delivery">
        <template #header><span>配送信息</span></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="配送编码">{{ result.delivery.deliveryCode }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="deliveryStatusType(result.delivery.status)" size="small">{{ deliveryStatusLabel(result.delivery.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ result.delivery.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话" class-name="nums-tabular">{{ result.delivery.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ result.delivery.receiverAddr }}</el-descriptions-item>
          <el-descriptions-item label="配送时间">{{ result.delivery.deliveryTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="签收时间">{{ result.delivery.signTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="质量检查">{{ qualityLabel(result.delivery.qualityCheck) }}</el-descriptions-item>
          <el-descriptions-item label="质量备注">{{ result.delivery.qualityRemark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </template>

    <el-empty v-else-if="searched && !result" description="暂无物流信息" style="margin-top:40px" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { logisticsApi } from '@/api/coldchain.js'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const taskId = ref('')
const loading = ref(false)
const result = ref(null)
const searched = ref(false)

function taskStatusType(code) {
  const map = { PENDING: 'info', IN_TRANSIT: 'primary', DELIVERED: 'success', CANCELLED: 'danger' }
  return map[code] || 'info'
}

function taskStatusLabel(code) {
  const map = { PENDING: '待发车', IN_TRANSIT: '运输中', DELIVERED: '已送达', CANCELLED: '已取消' }
  return map[code] || code
}

function vehicleTypeLabel(type) {
  const map = { REFRIGERATED: '冷藏车', INSULATED: '保温车', NORMAL: '普通车' }
  return map[type] || type
}

function deliveryStatusType(code) {
  const map = { PENDING: 'info', DELIVERING: 'primary', SIGNED: 'success', REJECTED: 'danger' }
  return map[code] || 'info'
}

function deliveryStatusLabel(code) {
  const map = { PENDING: '待配送', DELIVERING: '配送中', SIGNED: '已签收', REJECTED: '已拒收' }
  return map[code] || code
}

function qualityLabel(code) {
  const map = { PENDING: '待检', PASSED: '合格', REJECTED: '不合格' }
  return map[code] || code || '-'
}

async function handleSearch() {
  if (!taskId.value) {
    ElMessage.warning('请输入任务ID')
    return
  }
  loading.value = true
  searched.value = false
  result.value = null
  try {
    const res = await logisticsApi.full(taskId.value)
    result.value = res || null
    searched.value = true
  } catch (e) {
    searched.value = true
    // error shown by interceptor
  } finally {
    loading.value = false
  }
}
</script>
