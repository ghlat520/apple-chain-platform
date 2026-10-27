<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">车辆管理</span>
      <van-button type="primary" size="small" icon="plus" @click="openAddSheet()">新增车辆</van-button>
    </div>

    <van-search v-model="query.keyword" placeholder="搜索车牌号/驾驶员" @search="loadList(true)" shape="round" />

    <van-dropdown-menu style="margin-bottom:12px;">
      <van-dropdown-item v-model="query.vehicleType" :options="typeOptions" @change="loadList(true)" />
      <van-dropdown-item v-model="query.status" :options="statusOptions" @change="loadList(true)" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-swipe-cell v-for="item in list" :key="item.id" style="margin-bottom:8px;">
          <van-cell-group inset>
            <van-cell is-link @click="showDetail(item)">
              <template #title>
                <span style="font-weight:600;">{{ item.plateNumber }}</span>
                <van-tag :type="typeTagMap[item.vehicleType]?.type || 'default'" style="margin-left:6px;">
                  {{ typeTagMap[item.vehicleType]?.label || item.vehicleType }}
                </van-tag>
              </template>
              <template #right-icon>
                <van-tag :type="statusTagMap[item.status]?.type || 'default'">
                  {{ statusTagMap[item.status]?.label || item.status }}
                </van-tag>
              </template>
            </van-cell>
            <van-cell title="品牌" :value="item.brand || '-'" />
            <van-cell title="载重" :value="`${item.capacity || '-'}吨`" />
            <van-cell title="驾驶员" :value="item.driverName || '-'" />
          </van-cell-group>
          <template #right>
            <van-button square type="primary" text="编辑" style="height:100%;" @click="openEditSheet(item)" />
            <van-button square type="danger" text="删除" style="height:100%;" @click="handleDelete(item.id)" />
          </template>
        </van-swipe-cell>
        <van-empty v-if="!loading && list.length === 0" description="暂无车辆数据" />
      </van-list>
    </van-pull-refresh>

    <!-- 新增/编辑弹窗 -->
    <van-action-sheet v-model:show="showFormSheet" :title="isEdit ? '编辑车辆' : '新增车辆'">
      <div style="padding:16px;">
        <van-form @submit="handleSubmit">
          <van-field v-model="form.plateNumber" label="车牌号" placeholder="如：京A12345" :rules="[{required:true}]" />
          <van-field v-model="form.vehicleType" is-link readonly label="车辆类型" placeholder="请选择" @click="showTypePicker = true" :rules="[{required:true}]" />
          <van-field v-model="form.brand" label="品牌" placeholder="如：福田欧马可" />
          <van-field v-model="form.capacity" label="载重(吨)" type="number" placeholder="请输入" />
          <van-field v-model="form.volume" label="容积(m³)" type="number" placeholder="请输入" />
          <van-field v-model="form.temperatureMin" label="最低温度(°C)" type="number" placeholder="-20" />
          <van-field v-model="form.temperatureMax" label="最高温度(°C)" type="number" placeholder="5" />
          <van-field v-model="form.driverName" label="驾驶员" placeholder="请输入姓名" />
          <van-field v-model="form.driverPhone" label="联系电话" placeholder="请输入" />
          <van-field v-model="form.remark" label="备注" type="textarea" placeholder="选填" rows="2" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">
            {{ isEdit ? '保存修改' : '提交' }}
          </van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- 类型选择器 -->
    <van-popup v-model:show="showTypePicker" round position="bottom">
      <van-picker :columns="typePickerColumns" @confirm="onTypeConfirm" @cancel="showTypePicker = false" />
    </van-popup>

    <!-- 详情弹窗 -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:65%;">
      <div style="padding:16px;" v-if="selectedVehicle">
        <h3 style="margin-bottom:12px;">{{ selectedVehicle.plateNumber }}</h3>
        <van-cell-group>
          <van-cell title="车辆编码" :value="selectedVehicle.vehicleCode || '-'" />
          <van-cell title="车辆类型" :value="typeTagMap[selectedVehicle.vehicleType]?.label || '-'" />
          <van-cell title="品牌" :value="selectedVehicle.brand || '-'" />
          <van-cell title="载重" :value="`${selectedVehicle.capacity || '-'}吨`" />
          <van-cell title="容积" :value="`${selectedVehicle.volume || '-'}m³`" />
          <van-cell title="温控范围" :value="`${selectedVehicle.temperatureMin ?? '-'}°C ~ ${selectedVehicle.temperatureMax ?? '-'}°C`" />
          <van-cell title="驾驶员" :value="selectedVehicle.driverName || '-'" />
          <van-cell title="联系电话" :value="selectedVehicle.driverPhone || '-'" />
          <van-cell title="状态" :value="statusTagMap[selectedVehicle.status]?.label || '-'" />
          <van-cell title="备注" :value="selectedVehicle.remark || '-'" />
        </van-cell-group>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { coldchainApi } from '@/api/coldchain'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showFormSheet = ref(false)
const showDetailPopup = ref(false)
const showTypePicker = ref(false)
const selectedVehicle = ref(null)
const isEdit = ref(false)
const editingId = ref(null)

const query = reactive({ keyword: '', vehicleType: '', status: '', page: 1, size: 10 })
const form = reactive({
  plateNumber: '', vehicleType: '', brand: '', capacity: '', volume: '',
  temperatureMin: '', temperatureMax: '', driverName: '', driverPhone: '', remark: ''
})

const typeTagMap = {
  REFRIGERATED: { label: '冷藏车', type: 'primary' },
  INSULATED: { label: '保温车', type: 'success' },
  NORMAL: { label: '普通车', type: 'default' }
}

const statusTagMap = {
  IDLE: { label: '空闲', type: 'success' },
  IN_TRANSIT: { label: '运输中', type: 'primary' },
  MAINTENANCE: { label: '维修中', type: 'warning' },
  RETIRED: { label: '已退役', type: 'default' }
}

const typeOptions = [
  { text: '全部类型', value: '' },
  { text: '冷藏车', value: 'REFRIGERATED' },
  { text: '保温车', value: 'INSULATED' },
  { text: '普通车', value: 'NORMAL' }
]

const statusOptions = [
  { text: '全部状态', value: '' },
  { text: '空闲', value: 'IDLE' },
  { text: '运输中', value: 'IN_TRANSIT' },
  { text: '维修中', value: 'MAINTENANCE' },
  { text: '已退役', value: 'RETIRED' }
]

const typePickerColumns = [
  { text: '冷藏车', value: 'REFRIGERATED' },
  { text: '保温车', value: 'INSULATED' },
  { text: '普通车', value: 'NORMAL' }
]

function resetForm() {
  Object.assign(form, {
    plateNumber: '', vehicleType: '', brand: '', capacity: '', volume: '',
    temperatureMin: '', temperatureMax: '', driverName: '', driverPhone: '', remark: ''
  })
}

function openAddSheet() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  showFormSheet.value = true
}

function openEditSheet(item) {
  isEdit.value = true
  editingId.value = item.id
  Object.assign(form, {
    plateNumber: item.plateNumber || '',
    vehicleType: item.vehicleType || '',
    brand: item.brand || '',
    capacity: item.capacity ?? '',
    volume: item.volume ?? '',
    temperatureMin: item.temperatureMin ?? '',
    temperatureMax: item.temperatureMax ?? '',
    driverName: item.driverName || '',
    driverPhone: item.driverPhone || '',
    remark: item.remark || ''
  })
  showFormSheet.value = true
}

function onTypeConfirm({ selectedOptions }) {
  form.vehicleType = selectedOptions[0].value
  showTypePicker.value = false
}

function showDetail(vehicle) {
  selectedVehicle.value = vehicle
  showDetailPopup.value = true
}

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await coldchainApi.vehicleList({ ...query })
    const records = res.data?.records || []
    if (reset) list.value = records
    else list.value.push(...records)
    finished.value = list.value.length >= (res.data?.total || 0)
    query.page++
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function handleSubmit() {
  if (isEdit.value) {
    await coldchainApi.vehicleUpdate(editingId.value, { ...form })
    showToast('车辆更新成功')
  } else {
    await coldchainApi.vehicleCreate({ ...form })
    showToast('车辆创建成功')
  }
  showFormSheet.value = false
  resetForm()
  loadList(true)
}

async function handleDelete(id) {
  await showConfirmDialog({ title: '确认删除', message: '确定要删除该车辆吗？' })
  await coldchainApi.vehicleDelete(id)
  showToast('车辆已删除')
  loadList(true)
}

loadList(true)
</script>
