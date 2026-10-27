<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">仓储管理</span>
      <div style="display:flex;gap:8px;">
        <van-button plain type="warning" size="small" icon="warning-o" @click="goAlerts">预警</van-button>
        <van-button type="primary" size="small" icon="plus" @click="showAddSheet = true">新增仓库</van-button>
      </div>
    </div>

    <van-dropdown-menu>
      <van-dropdown-item v-model="query.type" :options="typeOptions" @change="loadList(true)" />
    </van-dropdown-menu>

    <van-search v-model="query.keyword" placeholder="搜索仓库名称/位置" @search="loadList(true)" shape="round" />

    <van-pull-refresh v-model="refreshing" @refresh="() => loadList(true)">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadList">
        <van-swipe-cell v-for="item in list" :key="item.id" style="margin-bottom:8px;">
          <van-cell-group inset>
            <van-cell is-link @click="showDetail(item)">
              <template #title>
                <span style="font-weight:600;">{{ item.name }}</span>
                <van-tag :type="typeTagColor(item.type)" style="margin-left:6px;">
                  {{ typeLabel(item.type) }}
                </van-tag>
                <van-tag :type="item.status === 'ACTIVE' ? 'success' : 'default'" style="margin-left:4px;">
                  {{ statusLabel(item.status) }}
                </van-tag>
              </template>
              <template #label>
                {{ item.location }} · 容量: {{ item.capacity }}吨 · 已用: {{ item.usedCapacity }}吨
              </template>
            </van-cell>
            <van-cell title="温度范围" :value="`${item.tempMin}°C ~ ${item.tempMax}°C`" />
            <van-cell title="当前温度" :value="`${item.currentTemp || '-'}°C`" />
          </van-cell-group>
          <template #right>
            <van-button
              v-if="item.status === 'ACTIVE'"
              square type="warning" text="维护" style="height:100%;"
              @click="changeStatus(item.id, 'MAINTENANCE')"
            />
            <van-button
              v-if="item.status === 'MAINTENANCE'"
              square type="danger" text="关闭" style="height:100%;"
              @click="changeStatus(item.id, 'CLOSED')"
            />
            <van-button
              v-if="item.status === 'CLOSED' || item.status === 'MAINTENANCE'"
              square type="success" text="启用" style="height:100%;"
              @click="changeStatus(item.id, 'ACTIVE')"
            />
          </template>
        </van-swipe-cell>
        <van-empty v-if="!loading && list.length === 0" description="暂无仓库数据" />
      </van-list>
    </van-pull-refresh>

    <!-- 演示链路导航 -->
    <div style="padding:16px;" v-if="list.length > 0 && !showAddSheet && !showDetailPopup && !showAlertPopup">
      <van-button
        block
        type="primary"
        color="#07c160"
        icon="guide-o"
        @click="router.push('/coldchain/transports')"
      >
        下一步：冷链运输
      </van-button>
    </div>

    <!-- 新增仓库 -->
    <van-action-sheet v-model:show="showAddSheet" title="新增仓库">
      <div style="padding:16px;">
        <van-form @submit="handleCreate">
          <van-field v-model="addForm.name" label="仓库名称" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.code" label="仓库编码" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.location" label="位置" placeholder="请输入" :rules="[{required:true}]" />
          <van-field
            v-model="addForm.type"
            is-link readonly label="仓库类型" placeholder="请选择"
            @click="showTypePicker = true"
            :rules="[{required:true}]"
          />
          <van-field v-model="addForm.capacity" label="总容量(吨)" type="number" placeholder="请输入" :rules="[{required:true}]" />
          <van-field v-model="addForm.tempMin" label="最低温度(°C)" type="number" placeholder="-2" />
          <van-field v-model="addForm.tempMax" label="最高温度(°C)" type="number" placeholder="4" />
          <van-field v-model="addForm.managerName" label="管理员" placeholder="请输入" />
          <van-field v-model="addForm.managerPhone" label="管理员电话" placeholder="请输入" />
          <van-button block type="primary" native-type="submit" style="margin-top:16px;" color="#07c160">提交</van-button>
        </van-form>
      </div>
    </van-action-sheet>

    <!-- 类型选择器 -->
    <van-popup v-model:show="showTypePicker" round position="bottom">
      <van-picker
        :columns="typePickerColumns"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>

    <!-- 仓库详情弹窗 -->
    <van-popup v-model:show="showDetailPopup" round position="bottom" style="height:70%;">
      <div style="padding:16px;" v-if="selectedWarehouse">
        <h3 style="margin-bottom:12px;">{{ selectedWarehouse.name }}</h3>
        <van-cell-group>
          <van-cell title="仓库编码" :value="selectedWarehouse.code || '-'" />
          <van-cell title="仓库类型" :value="typeLabel(selectedWarehouse.type)" />
          <van-cell title="位置" :value="selectedWarehouse.location" />
          <van-cell title="总容量" :value="`${selectedWarehouse.capacity} 吨`" />
          <van-cell title="已使用" :value="`${selectedWarehouse.usedCapacity} 吨`" />
          <van-cell title="空余容量" :value="`${selectedWarehouse.capacity - selectedWarehouse.usedCapacity} 吨`" />
          <van-cell title="温度范围" :value="`${selectedWarehouse.tempMin}°C ~ ${selectedWarehouse.tempMax}°C`" />
          <van-cell title="当前温度" :value="`${selectedWarehouse.currentTemp || '-'}°C`" />
          <van-cell title="管理员" :value="selectedWarehouse.managerName || '-'" />
          <van-cell title="管理员电话" :value="selectedWarehouse.managerPhone || '-'" />
          <van-cell title="状态" :value="statusLabel(selectedWarehouse.status)" />
        </van-cell-group>
      </div>
    </van-popup>

    <!-- 预警列表弹窗 -->
    <van-popup v-model:show="showAlertPopup" round position="bottom" style="height:60%;">
      <div style="padding:16px;">
        <h3 style="margin-bottom:12px;">仓库预警</h3>
        <van-empty v-if="alertList.length === 0" description="暂无预警信息" />
        <van-cell-group v-for="alert in alertList" :key="alert.id" inset style="margin-bottom:8px;">
          <van-cell :title="alert.warehouseName" :label="alert.message" icon="warning-o">
            <template #right-icon>
              <van-tag type="danger">{{ alert.level || 'WARN' }}</van-tag>
            </template>
          </van-cell>
        </van-cell-group>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { warehouseApi } from '@/api/warehouse'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAddSheet = ref(false)
const showDetailPopup = ref(false)
const showAlertPopup = ref(false)
const showTypePicker = ref(false)
const selectedWarehouse = ref(null)
const alertList = ref([])

const query = reactive({ keyword: '', type: '', page: 1, size: 10 })
const addForm = reactive({
  name: '', code: '', location: '', type: '', capacity: '',
  tempMin: '', tempMax: '', managerName: '', managerPhone: ''
})

const typeMap = {
  NORMAL: { label: '普通仓', color: 'default' },
  COLD: { label: '冷藏仓', color: 'primary' },
  ATMOSPHERE: { label: '气调仓', color: 'success' }
}

const statusMap = {
  ACTIVE: '运营中',
  MAINTENANCE: '维护中',
  CLOSED: '已关闭'
}

const typeOptions = [
  { text: '全部类型', value: '' },
  { text: '普通仓', value: 'NORMAL' },
  { text: '冷藏仓', value: 'COLD' },
  { text: '气调仓', value: 'ATMOSPHERE' }
]

const typePickerColumns = [
  { text: '普通仓', value: 'NORMAL' },
  { text: '冷藏仓', value: 'COLD' },
  { text: '气调仓', value: 'ATMOSPHERE' }
]

function typeLabel(t) { return typeMap[t]?.label || t }
function typeTagColor(t) { return typeMap[t]?.color || 'default' }
function statusLabel(s) { return statusMap[s] || s }

function onTypeConfirm({ selectedOptions }) {
  addForm.type = selectedOptions[0]?.value || ''
  showTypePicker.value = false
}

async function loadList(reset = false) {
  if (reset) { query.page = 1; list.value = []; finished.value = false }
  loading.value = true
  try {
    const res = await warehouseApi.list({ ...query })
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

function showDetail(warehouse) {
  selectedWarehouse.value = warehouse
  showDetailPopup.value = true
}

async function changeStatus(id, status) {
  try {
    await showConfirmDialog({ title: '确认', message: `确定将仓库状态变更为"${statusLabel(status)}"吗？` })
    await warehouseApi.changeStatus(id, { status })
    showToast('状态变更成功')
    loadList(true)
  } catch {
    // user cancelled or API error
  }
}

async function goAlerts() {
  showAlertPopup.value = true
  try {
    const res = await warehouseApi.alerts()
    alertList.value = res.data || []
  } catch {
    alertList.value = []
  }
}

async function handleCreate() {
  await warehouseApi.create(addForm)
  showToast('仓库创建成功')
  showAddSheet.value = false
  Object.assign(addForm, {
    name: '', code: '', location: '', type: '', capacity: '',
    tempMin: '', tempMax: '', managerName: '', managerPhone: ''
  })
  loadList(true)
}

loadList(true)
</script>
