<template>
  <div class="records-page">
    <van-search
      v-model="query.batchCode"
      placeholder="搜索批次号"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <div class="filter-bar">
      <van-tabs v-model:active="query.recordType" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="入库" name="INBOUND" />
        <van-tab title="出库" name="OUTBOUND" />
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
              <span>{{ item.recordNo || '未知单号' }}</span>
              <van-tag :type="recordTypeTagType(item.recordType)" size="small" style="margin-left:6px">{{ recordTypeLabel(item.recordType) }}</van-tag>
            </template>
            <template #label>
              仓库: {{ item.warehouseName || '-' }} · 品种: {{ item.variety || '-' }}<br/>
              数量: {{ item.quantity ?? '-' }} kg · 日期: {{ item.recordDate || '-' }}
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无出入库记录" />
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
        <span>新增出入库记录</span>
        <van-icon name="cross" @click="showDrawer = false" />
      </div>
      <van-form @submit="handleSubmit" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.warehouseName"
            label="仓库名称"
            placeholder="请输入仓库名称"
            :rules="[{ required: true, message: '请填写仓库名称' }]"
          />
          <van-field label="记录类型">
            <template #input>
              <van-radio-group v-model="form.recordType" direction="horizontal">
                <van-radio name="INBOUND">入库</van-radio>
                <van-radio name="OUTBOUND">出库</van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field
            v-model="form.batchCode"
            label="批次号"
            placeholder="请输入批次号"
          />
          <van-field
            v-model="form.variety"
            label="品种"
            placeholder="请输入苹果品种"
            :rules="[{ required: true, message: '请填写品种' }]"
          />
          <van-field
            v-model="form.grade"
            label="等级"
            placeholder="如：特级、一级、二级"
          />
          <van-field
            v-model.number="form.quantity"
            label="数量(kg)"
            type="number"
            placeholder="请输入数量"
            :rules="[{ required: true, message: '请填写数量' }]"
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
            v-model="form.operator"
            label="操作人"
            placeholder="请输入操作人"
          />
          <van-field
            v-model="form.recordDate"
            label="记录日期"
            placeholder="例：2024-10-01"
          />
          <van-field
            v-model="form.traceCode"
            label="溯源码"
            placeholder="请输入溯源码"
          />
        </van-cell-group>
        <div class="drawer-actions">
          <van-button block type="primary" native-type="submit" :loading="submitting">
            确认新增
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
        <span>出入库详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="记录类型">
          <template #value>
            <van-tag :type="recordTypeTagType(currentItem.recordType)">{{ recordTypeLabel(currentItem.recordType) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="记录编号" :value="currentItem.recordNo || '-'" />
        <van-cell title="仓库名称" :value="currentItem.warehouseName || '-'" />
        <van-cell title="批次号" :value="currentItem.batchCode || '-'" />
        <van-cell title="品种" :value="currentItem.variety || '-'" />
        <van-cell title="等级" :value="currentItem.grade || '-'" />
        <van-cell title="数量(kg)" :value="currentItem.quantity ?? '-'" />
        <van-cell title="温度(°C)" :value="currentItem.temperature ?? '-'" />
        <van-cell title="湿度(%)" :value="currentItem.humidity ?? '-'" />
        <van-cell title="操作人" :value="currentItem.operator || '-'" />
        <van-cell title="记录日期" :value="currentItem.recordDate || '-'" />
        <van-cell title="溯源码" v-if="currentItem.traceCode" is-link @click="goToTrace(currentItem.traceCode)">
          <template #value>
            <van-tag type="success" size="medium">{{ currentItem.traceCode }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="溯源码" v-else value="-" />
        <van-cell title="创建时间" :value="currentItem.createTime || '-'" />
      </van-cell-group>
      <div class="drawer-actions">
        <van-button block type="danger" plain @click="handleDelete(currentItem)">删除</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { warehouseApi } from '@/api/warehouse.js'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showDrawer = ref(false)
const showDetail = ref(false)
const submitting = ref(false)
const currentItem = ref(null)

const query = reactive({ recordType: '', batchCode: '', page: 1, pageSize: 10 })
const form = reactive({ warehouseName: '', recordType: 'INBOUND', batchCode: '', variety: '', grade: '', quantity: '', temperature: '', humidity: '', operator: '', recordDate: '', traceCode: '' })

function recordTypeTagType(type) {
  const map = { INBOUND: 'success', OUTBOUND: 'warning' }
  return map[type] || 'default'
}

function recordTypeLabel(type) {
  const map = { INBOUND: '入库', OUTBOUND: '出库' }
  return map[type] || type || '-'
}

function resetForm() {
  Object.assign(form, { warehouseName: '', recordType: 'INBOUND', batchCode: '', variety: '', grade: '', quantity: '', temperature: '', humidity: '', operator: '', recordDate: '', traceCode: '' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await warehouseApi.getRecords({ ...query, page: 1 })
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
    const res = await warehouseApi.getRecords({ ...query })
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
  showDrawer.value = true
}

function openDetailDrawer(item) {
  currentItem.value = item
  showDetail.value = true
}

async function handleSubmit() {
  submitting.value = true
  try {
    await warehouseApi.createRecord(form)
    showToast({ type: 'success', message: '新增成功' })
    showDrawer.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(item) {
  try {
    await showConfirmDialog({ title: '确认删除', message: '确认删除该出入库记录？' })
    await warehouseApi.deleteRecord(item.id)
    showToast({ type: 'success', message: '删除成功' })
    showDetail.value = false
    loadList()
  } catch (e) {
    // user cancelled
  }
}

function goToTrace(traceCode) {
  showDetail.value = false
  router.push(`/trace/query?code=${traceCode}`)
}

async function handleExport() {
  showToast('正在导出...')
  try {
    await warehouseApi.exportRecords()
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.records-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 4px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-form { padding-bottom: 20px; }
.drawer-actions { padding: 16px; }
</style>
