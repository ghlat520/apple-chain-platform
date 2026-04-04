<template>
  <div class="purchases-page">
    <van-search
      v-model="query.purchaseNo"
      placeholder="搜索采购单号"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="待收货" name="PENDING" />
        <van-tab title="已收货" name="RECEIVED" />
        <van-tab title="已取消" name="CANCELLED" />
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
              <span>{{ item.purchaseNo || '未知单号' }}</span>
              <van-tag :type="statusTagType(item.status)" size="small" style="margin-left:6px">{{ statusLabel(item.status) }}</van-tag>
            </template>
            <template #label>
              产品: {{ item.productName || '-' }} · 供应商: {{ item.supplierName || '-' }}<br/>
              数量: {{ item.quantity || '-' }} {{ item.unit || '' }} · 金额: ¥{{ item.totalAmount || '-' }}
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无采购记录" />
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
        <span>{{ editId ? '编辑采购单' : '新增采购单' }}</span>
        <van-icon name="cross" @click="showDrawer = false" />
      </div>
      <van-form @submit="handleSubmit" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.productName"
            label="产品名称"
            placeholder="请输入产品名称"
            :rules="[{ required: true, message: '请填写产品名称' }]"
          />
          <van-field
            v-model="form.supplierName"
            label="供应商"
            placeholder="请输入供应商名称"
            :rules="[{ required: true, message: '请填写供应商' }]"
          />
          <van-field
            v-model.number="form.quantity"
            label="数量"
            type="number"
            placeholder="请输入数量"
            :rules="[{ required: true, message: '请填写数量' }]"
          />
          <van-field
            v-model="form.unit"
            label="单位"
            placeholder="如：kg、瓶、袋"
          />
          <van-field
            v-model.number="form.unitPrice"
            label="单价(元)"
            type="number"
            placeholder="请输入单价"
          />
          <van-field
            v-model.number="form.totalAmount"
            label="总金额(元)"
            type="number"
            placeholder="请输入总金额"
          />
          <van-field
            v-model="form.purchaseDate"
            label="采购日期"
            placeholder="例：2024-10-01"
          />
          <van-field label="状态">
            <template #input>
              <van-radio-group v-model="form.status" direction="horizontal">
                <van-radio name="PENDING">待收货</van-radio>
                <van-radio name="RECEIVED">已收货</van-radio>
                <van-radio name="CANCELLED">已取消</van-radio>
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
        <span>采购详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="状态">
          <template #value>
            <van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="采购单号" :value="currentItem.purchaseNo || '-'" />
        <van-cell title="产品名称" :value="currentItem.productName || '-'" />
        <van-cell title="供应商" :value="currentItem.supplierName || '-'" />
        <van-cell title="数量" :value="(currentItem.quantity ?? '-') + ' ' + (currentItem.unit || '')" />
        <van-cell title="单价(元)" :value="currentItem.unitPrice ?? '-'" />
        <van-cell title="总金额(元)" :value="currentItem.totalAmount ?? '-'" />
        <van-cell title="采购日期" :value="currentItem.purchaseDate || '-'" />
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
import { inputApi } from '@/api/input.js'
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

const query = reactive({ status: '', purchaseNo: '', page: 1, pageSize: 10 })
const form = reactive({ productName: '', supplierName: '', quantity: '', unit: '', unitPrice: '', totalAmount: '', purchaseDate: '', status: 'PENDING' })

function statusTagType(status) {
  const map = { PENDING: 'warning', RECEIVED: 'success', CANCELLED: 'danger' }
  return map[status] || 'default'
}

function statusLabel(status) {
  const map = { PENDING: '待收货', RECEIVED: '已收货', CANCELLED: '已取消' }
  return map[status] || status || '-'
}

function resetForm() {
  Object.assign(form, { productName: '', supplierName: '', quantity: '', unit: '', unitPrice: '', totalAmount: '', purchaseDate: '', status: 'PENDING' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await inputApi.getPurchases({ ...query, page: 1 })
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
    const res = await inputApi.getPurchases({ ...query })
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
    productName: item.productName || '',
    supplierName: item.supplierName || '',
    quantity: item.quantity ?? '',
    unit: item.unit || '',
    unitPrice: item.unitPrice ?? '',
    totalAmount: item.totalAmount ?? '',
    purchaseDate: item.purchaseDate || '',
    status: item.status || 'PENDING'
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
      await inputApi.updatePurchase(editId.value, form)
      showToast({ type: 'success', message: '修改成功' })
    } else {
      await inputApi.createPurchase(form)
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
    await showConfirmDialog({ title: '确认删除', message: '确认删除该采购记录？' })
    await inputApi.deletePurchase(item.id)
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
    await inputApi.exportPurchases()
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.purchases-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 4px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-form { padding-bottom: 20px; }
.drawer-actions { padding: 16px; }
</style>
