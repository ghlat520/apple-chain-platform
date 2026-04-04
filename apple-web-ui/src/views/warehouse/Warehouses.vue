<template>
  <div class="warehouses-page">
    <van-search
      v-model="query.name"
      placeholder="搜索仓库名称"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <div class="filter-bar">
      <van-tabs v-model:active="query.type" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="普通" name="NORMAL" />
        <van-tab title="冷库" name="COLD" />
        <van-tab title="气调" name="ATMOSPHERE" />
      </van-tabs>
    </div>

    <div class="action-bar">
      <van-button type="primary" size="small" icon="plus" @click="openAddDrawer">新增</van-button>
      <van-button type="default" size="small" icon="down" @click="handleExport">导出CSV</van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-cell-group inset style="margin-bottom:8px" v-for="item in list" :key="item.id">
          <van-cell is-link @click="openDetailDrawer(item)">
            <template #title>
              <span>{{ item.name || '未知仓库' }}</span>
              <van-tag :type="typeTagType(item.type)" size="small" style="margin-left:6px">{{ typeLabel(item.type) }}</van-tag>
            </template>
            <template #label>
              位置: {{ item.location || '-' }} · 容量: {{ item.usedCapacity ?? '-' }}/{{ item.capacity ?? '-' }}
            </template>
            <template #value>
              <van-tag :type="statusTagType(item.status)">{{ statusLabel(item.status) }}</van-tag>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无仓库" />
      </van-list>
    </van-pull-refresh>

    <!-- Add/Edit Drawer -->
    <van-popup
      v-model:show="showDrawer"
      position="bottom"
      round
      :style="{ maxHeight: '90vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>{{ editId ? '编辑仓库' : '新增仓库' }}</span>
        <van-icon name="cross" @click="showDrawer = false" />
      </div>
      <van-form @submit="handleSubmit" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.name"
            label="仓库名称"
            placeholder="请输入仓库名称"
            :rules="[{ required: true, message: '请填写仓库名称' }]"
          />
          <van-field label="仓库类型">
            <template #input>
              <van-radio-group v-model="form.type" direction="horizontal">
                <van-radio name="NORMAL">普通</van-radio>
                <van-radio name="COLD">冷库</van-radio>
                <van-radio name="ATMOSPHERE">气调</van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field
            v-model="form.location"
            label="位置"
            placeholder="请输入仓库位置"
          />
          <van-field
            v-model.number="form.capacity"
            label="容量"
            type="number"
            placeholder="请输入总容量"
          />
          <van-field
            v-model="form.temperature"
            label="温度(°C)"
            placeholder="请输入温度"
          />
          <van-field
            v-model="form.humidity"
            label="湿度(%)"
            placeholder="请输入湿度"
          />
          <van-field
            v-model="form.manager"
            label="管理员"
            placeholder="请输入管理员姓名"
          />
          <van-field
            v-model="form.phone"
            label="联系电话"
            type="tel"
            placeholder="请输入联系电话"
          />
          <van-field label="状态">
            <template #input>
              <van-radio-group v-model="form.status" direction="horizontal">
                <van-radio name="ACTIVE">正常</van-radio>
                <van-radio name="MAINTENANCE">维护</van-radio>
                <van-radio name="FULL">已满</van-radio>
                <van-radio name="CLOSED">关闭</van-radio>
              </van-radio-group>
            </template>
          </van-field>
        </van-cell-group>
        <div class="drawer-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">
            {{ editId ? '保存修改' : '确认新增' }}
          </van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- Detail Drawer -->
    <van-popup
      v-model:show="showDetail"
      position="bottom"
      round
      :style="{ maxHeight: '90vh', overflowY: 'auto' }"
    >
      <div class="drawer-header">
        <span>仓库详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="状态">
          <template #value>
            <van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="仓库名称" :value="currentItem.name || '-'" />
        <van-cell title="仓库类型">
          <template #value>
            <van-tag :type="typeTagType(currentItem.type)">{{ typeLabel(currentItem.type) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="位置" :value="currentItem.location || '-'" />
        <van-cell title="容量" :value="(currentItem.usedCapacity ?? '-') + ' / ' + (currentItem.capacity ?? '-')" />
        <van-cell title="温度(°C)" :value="currentItem.temperature ?? '-'" />
        <van-cell title="湿度(%)" :value="currentItem.humidity ?? '-'" />
        <van-cell title="管理员" :value="currentItem.manager || '-'" />
        <van-cell title="联系电话" :value="currentItem.phone || '-'" />
        <van-cell title="创建时间" :value="currentItem.createTime || '-'" />
      </van-cell-group>
      <div class="drawer-actions">
        <van-button block type="primary" plain @click="openEditDrawer(currentItem)">编辑</van-button>
        <van-button block type="danger" plain @click="handleDelete(currentItem)" style="margin-top:8px">删除</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { warehouseApi } from '@/api/warehouse.js'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showDrawer = ref(false)
const showDetail = ref(false)
const submitting = ref(false)
const editId = ref(null)
const currentItem = ref(null)

const query = reactive({ type: '', name: '', page: 1, pageSize: 10 })
const form = reactive({ name: '', type: 'NORMAL', location: '', capacity: '', temperature: '', humidity: '', manager: '', phone: '', status: 'ACTIVE' })

function typeTagType(type) {
  const map = { NORMAL: 'default', COLD: 'primary', ATMOSPHERE: 'success' }
  return map[type] || 'default'
}

function typeLabel(type) {
  const map = { NORMAL: '普通', COLD: '冷库', ATMOSPHERE: '气调' }
  return map[type] || type || '-'
}

function statusTagType(status) {
  const map = { ACTIVE: 'success', MAINTENANCE: 'warning', FULL: 'primary', CLOSED: 'danger' }
  return map[status] || 'default'
}

function statusLabel(status) {
  const map = { ACTIVE: '正常', MAINTENANCE: '维护中', FULL: '已满', CLOSED: '已关闭' }
  return map[status] || status || '-'
}

function resetForm() {
  Object.assign(form, { name: '', type: 'NORMAL', location: '', capacity: '', temperature: '', humidity: '', manager: '', phone: '', status: 'ACTIVE' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await warehouseApi.getWarehouses({ ...query, page: 1 })
    const records = res?.records || res || []
    list.value = records
    finished.value = records.length >= (res?.total || records.length)
    query.page = 1
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function loadMore() {
  if (finished.value) return
  query.page += 1
  try {
    const res = await warehouseApi.getWarehouses({ ...query })
    const records = res?.records || []
    list.value = [...list.value, ...records]
    if (list.value.length >= (res?.total || 0) || records.length < query.pageSize) {
      finished.value = true
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  finished.value = false
  list.value = []
  loadList()
}

function openAddDrawer() {
  resetForm()
  editId.value = null
  showDrawer.value = true
}

function openEditDrawer(item) {
  Object.assign(form, {
    name: item.name || '',
    type: item.type || 'NORMAL',
    location: item.location || '',
    capacity: item.capacity ?? '',
    temperature: item.temperature ?? '',
    humidity: item.humidity ?? '',
    manager: item.manager || '',
    phone: item.phone || '',
    status: item.status || 'ACTIVE'
  })
  editId.value = item.id
  showDetail.value = false
  showDrawer.value = true
}

function openDetailDrawer(item) {
  currentItem.value = item
  showDetail.value = true
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (editId.value) {
      await warehouseApi.updateWarehouse(editId.value, form)
      showToast({ type: 'success', message: '修改成功' })
    } else {
      await warehouseApi.createWarehouse(form)
      showToast({ type: 'success', message: '新增成功' })
    }
    showDrawer.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(item) {
  try {
    await showConfirmDialog({ title: '确认删除', message: '确认删除该仓库？' })
    await warehouseApi.deleteWarehouse(item.id)
    showToast({ type: 'success', message: '删除成功' })
    showDetail.value = false
    loadList()
  } catch (e) {
    // user cancelled
  }
}

async function handleExport() {
  showToast('正在导出...')
  try {
    await warehouseApi.exportWarehouses()
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.warehouses-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 4px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-form { padding-bottom: 20px; }
.drawer-actions { padding: 16px; }
</style>
