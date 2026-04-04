<template>
  <div class="receipts-page">
    <van-search
      v-model="query.receiptNo"
      placeholder="搜索仓单编号"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="有效" name="VALID" />
        <van-tab title="质押中" name="PLEDGED" />
        <van-tab title="已转让" name="TRANSFERRED" />
        <van-tab title="已注销" name="CANCELLED" />
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
              <span>{{ item.receiptNo || '未知仓单' }}</span>
              <van-tag :type="statusTagType(item.status)" size="small" style="margin-left:6px">{{ statusLabel(item.status) }}</van-tag>
            </template>
            <template #label>
              仓库: {{ item.warehouseName || '-' }} · 果农: {{ item.farmerName || '-' }}<br/>
              {{ item.variety || '-' }} {{ item.grade || '' }} · {{ item.quantity ?? '-' }} kg · ¥{{ item.totalValue ?? '-' }}
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无仓单" />
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
        <span>{{ editId ? '编辑仓单' : '新增仓单' }}</span>
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
          <van-field
            v-model="form.farmerName"
            label="果农姓名"
            placeholder="请输入果农姓名"
            :rules="[{ required: true, message: '请填写果农姓名' }]"
          />
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
            v-model.number="form.unitValue"
            label="单价(元/kg)"
            type="number"
            placeholder="请输入单价"
          />
          <van-field
            v-model="form.inboundDate"
            label="入库日期"
            placeholder="例：2024-10-01"
          />
          <van-field
            v-model="form.validUntil"
            label="有效期至"
            placeholder="例：2025-04-01"
          />
          <van-field
            v-model="form.traceCode"
            label="溯源码"
            placeholder="请输入溯源码"
          />
          <van-field label="状态">
            <template #input>
              <van-radio-group v-model="form.status" direction="horizontal">
                <van-radio name="VALID">有效</van-radio>
                <van-radio name="PLEDGED">质押中</van-radio>
                <van-radio name="TRANSFERRED">已转让</van-radio>
                <van-radio name="CANCELLED">已注销</van-radio>
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
        <span>仓单详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="状态">
          <template #value>
            <van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="仓单编号" :value="currentItem.receiptNo || '-'" />
        <van-cell title="仓库名称" :value="currentItem.warehouseName || '-'" />
        <van-cell title="果农姓名" :value="currentItem.farmerName || '-'" />
        <van-cell title="批次号" :value="currentItem.batchCode || '-'" />
        <van-cell title="品种" :value="currentItem.variety || '-'" />
        <van-cell title="等级" :value="currentItem.grade || '-'" />
        <van-cell title="数量(kg)" :value="currentItem.quantity ?? '-'" />
        <van-cell title="单价(元/kg)" :value="currentItem.unitValue ?? '-'" />
        <van-cell title="总价值(元)" :value="currentItem.totalValue ?? '-'" />
        <van-cell title="入库日期" :value="currentItem.inboundDate || '-'" />
        <van-cell title="有效期至" :value="currentItem.validUntil || '-'" />
        <van-cell title="溯源码" v-if="currentItem.traceCode" is-link @click="goToTrace(currentItem.traceCode)">
          <template #value>
            <van-tag type="success" size="medium">{{ currentItem.traceCode }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="溯源码" v-else value="-" />
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
const editId = ref(null)
const currentItem = ref(null)

const query = reactive({ status: '', receiptNo: '', page: 1, pageSize: 10 })
const form = reactive({ warehouseName: '', farmerName: '', batchCode: '', variety: '', grade: '', quantity: '', unitValue: '', inboundDate: '', validUntil: '', traceCode: '', status: 'VALID' })

function statusTagType(status) {
  const map = { VALID: 'success', PLEDGED: 'primary', TRANSFERRED: 'warning', CANCELLED: 'danger' }
  return map[status] || 'default'
}

function statusLabel(status) {
  const map = { VALID: '有效', PLEDGED: '质押中', TRANSFERRED: '已转让', CANCELLED: '已注销' }
  return map[status] || status || '-'
}

function resetForm() {
  Object.assign(form, { warehouseName: '', farmerName: '', batchCode: '', variety: '', grade: '', quantity: '', unitValue: '', inboundDate: '', validUntil: '', traceCode: '', status: 'VALID' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await warehouseApi.getReceipts({ ...query, page: 1 })
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
    const res = await warehouseApi.getReceipts({ ...query })
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
    warehouseName: item.warehouseName || '',
    farmerName: item.farmerName || '',
    batchCode: item.batchCode || '',
    variety: item.variety || '',
    grade: item.grade || '',
    quantity: item.quantity ?? '',
    unitValue: item.unitValue ?? '',
    inboundDate: item.inboundDate || '',
    validUntil: item.validUntil || '',
    traceCode: item.traceCode || '',
    status: item.status || 'VALID'
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
      await warehouseApi.updateReceipt(editId.value, form)
      showToast({ type: 'success', message: '修改成功' })
    } else {
      await warehouseApi.createReceipt(form)
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
    await showConfirmDialog({ title: '确认删除', message: '确认删除该仓单？' })
    await warehouseApi.deleteReceipt(item.id)
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
    await warehouseApi.exportReceipts()
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.receipts-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 4px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-form { padding-bottom: 20px; }
.drawer-actions { padding: 16px; }
</style>
