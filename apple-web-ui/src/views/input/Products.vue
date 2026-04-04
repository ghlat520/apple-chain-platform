<template>
  <div class="products-page">
    <van-search
      v-model="query.name"
      placeholder="搜索农资产品名称"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <div class="filter-bar">
      <van-tabs v-model:active="query.type" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="化肥" name="FERTILIZER" />
        <van-tab title="农药" name="PESTICIDE" />
        <van-tab title="种子" name="SEED" />
        <van-tab title="农具" name="TOOL" />
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
              <span>{{ item.name || '未知产品' }}</span>
              <van-tag :type="typeTagType(item.type)" size="small" style="margin-left:6px">{{ typeLabel(item.type) }}</van-tag>
            </template>
            <template #label>
              厂家: {{ item.manufacturer || '-' }} · 规格: {{ item.spec || '-' }}
            </template>
            <template #value>
              <van-tag :type="statusTagType(item.status)">{{ statusLabel(item.status) }}</van-tag>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无农资产品" />
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
        <span>{{ editId ? '编辑农资产品' : '新增农资产品' }}</span>
        <van-icon name="cross" @click="showDrawer = false" />
      </div>
      <van-form @submit="handleSubmit" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.name"
            label="产品名称"
            placeholder="请输入产品名称"
            :rules="[{ required: true, message: '请填写产品名称' }]"
          />
          <van-field label="产品类型">
            <template #input>
              <van-radio-group v-model="form.type" direction="horizontal">
                <van-radio name="FERTILIZER">化肥</van-radio>
                <van-radio name="PESTICIDE">农药</van-radio>
                <van-radio name="SEED">种子</van-radio>
                <van-radio name="TOOL">农具</van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field
            v-model="form.manufacturer"
            label="生产厂家"
            placeholder="请输入生产厂家"
          />
          <van-field
            v-model="form.spec"
            label="规格"
            placeholder="请输入规格"
          />
          <van-field
            v-model="form.batchNo"
            label="批次号"
            placeholder="请输入批次号"
          />
          <van-field
            v-model="form.productionDate"
            label="生产日期"
            placeholder="例：2024-10-01"
          />
          <van-field
            v-model="form.expiryDate"
            label="有效期至"
            placeholder="例：2025-10-01"
          />
          <van-field
            v-model="form.registration"
            label="登记证号"
            placeholder="请输入登记证号"
          />
          <van-field label="状态">
            <template #input>
              <van-radio-group v-model="form.status" direction="horizontal">
                <van-radio name="ACTIVE">正常</van-radio>
                <van-radio name="DISCONTINUED">停用</van-radio>
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
        <span>产品详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="状态">
          <template #value>
            <van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="产品名称" :value="currentItem.name || '-'" />
        <van-cell title="产品类型">
          <template #value>
            <van-tag :type="typeTagType(currentItem.type)">{{ typeLabel(currentItem.type) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="生产厂家" :value="currentItem.manufacturer || '-'" />
        <van-cell title="规格" :value="currentItem.spec || '-'" />
        <van-cell title="批次号" :value="currentItem.batchNo || '-'" />
        <van-cell title="生产日期" :value="currentItem.productionDate || '-'" />
        <van-cell title="有效期至" :value="currentItem.expiryDate || '-'" />
        <van-cell title="登记证号" :value="currentItem.registration || '-'" />
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

const query = reactive({ type: '', name: '', page: 1, pageSize: 10 })
const form = reactive({ name: '', type: 'FERTILIZER', manufacturer: '', spec: '', batchNo: '', productionDate: '', expiryDate: '', registration: '', status: 'ACTIVE' })

function typeTagType(type) {
  const map = { FERTILIZER: 'primary', PESTICIDE: 'warning', SEED: 'success', TOOL: 'default' }
  return map[type] || 'default'
}

function typeLabel(type) {
  const map = { FERTILIZER: '化肥', PESTICIDE: '农药', SEED: '种子', TOOL: '农具' }
  return map[type] || type || '-'
}

function statusTagType(status) {
  const map = { ACTIVE: 'success', DISCONTINUED: 'danger' }
  return map[status] || 'default'
}

function statusLabel(status) {
  const map = { ACTIVE: '正常', DISCONTINUED: '停用' }
  return map[status] || status || '-'
}

function resetForm() {
  Object.assign(form, { name: '', type: 'FERTILIZER', manufacturer: '', spec: '', batchNo: '', productionDate: '', expiryDate: '', registration: '', status: 'ACTIVE' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await inputApi.getProducts({ ...query, page: 1 })
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
    const res = await inputApi.getProducts({ ...query })
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
    type: item.type || 'FERTILIZER',
    manufacturer: item.manufacturer || '',
    spec: item.spec || '',
    batchNo: item.batchNo || '',
    productionDate: item.productionDate || '',
    expiryDate: item.expiryDate || '',
    registration: item.registration || '',
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
      await inputApi.updateProduct(editId.value, form)
      showToast({ type: 'success', message: '修改成功' })
    } else {
      await inputApi.createProduct(form)
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
    await showConfirmDialog({ title: '确认删除', message: '确认删除该农资产品？' })
    await inputApi.deleteProduct(item.id)
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
    await inputApi.exportProducts()
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.products-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 4px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-form { padding-bottom: 20px; }
.drawer-actions { padding: 16px; }
</style>
