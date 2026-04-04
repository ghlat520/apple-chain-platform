<template>
  <div class="inventory-page">
    <van-search
      v-model="query.productName"
      placeholder="搜索产品名称"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="正常" name="NORMAL" />
        <van-tab title="偏低" name="LOW" />
        <van-tab title="缺货" name="EMPTY" />
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
              <span>{{ item.productName || '未知产品' }}</span>
              <van-tag :type="statusTagType(item.status)" size="small" style="margin-left:6px">{{ statusLabel(item.status) }}</van-tag>
            </template>
            <template #label>
              库存: {{ item.stockQuantity ?? '-' }} {{ item.unit || '' }}
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无库存记录" />
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
        <span>{{ editId ? '编辑库存' : '新增库存' }}</span>
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
            v-model.number="form.stockQuantity"
            label="库存数量"
            type="number"
            placeholder="请输入库存数量"
            :rules="[{ required: true, message: '请填写库存数量' }]"
          />
          <van-field
            v-model="form.unit"
            label="单位"
            placeholder="如：kg、瓶、袋"
          />
          <van-field
            v-model.number="form.warningLevel"
            label="预警值"
            type="number"
            placeholder="低于此值发出预警"
          />
          <van-field
            v-model="form.warehouse"
            label="所在仓库"
            placeholder="请输入仓库名称"
          />
          <van-field label="状态">
            <template #input>
              <van-radio-group v-model="form.status" direction="horizontal">
                <van-radio name="NORMAL">正常</van-radio>
                <van-radio name="LOW">偏低</van-radio>
                <van-radio name="EMPTY">缺货</van-radio>
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
        <span>库存详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="状态">
          <template #value>
            <van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="产品名称" :value="currentItem.productName || '-'" />
        <van-cell title="库存数量" :value="(currentItem.stockQuantity ?? '-') + ' ' + (currentItem.unit || '')" />
        <van-cell title="预警值" :value="currentItem.warningLevel ?? '-'" />
        <van-cell title="所在仓库" :value="currentItem.warehouse || '-'" />
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

const query = reactive({ status: '', productName: '', page: 1, pageSize: 10 })
const form = reactive({ productName: '', stockQuantity: '', unit: '', warningLevel: '', warehouse: '', status: 'NORMAL' })

function statusTagType(status) {
  const map = { NORMAL: 'success', LOW: 'warning', EMPTY: 'danger' }
  return map[status] || 'default'
}

function statusLabel(status) {
  const map = { NORMAL: '正常', LOW: '偏低', EMPTY: '缺货' }
  return map[status] || status || '-'
}

function resetForm() {
  Object.assign(form, { productName: '', stockQuantity: '', unit: '', warningLevel: '', warehouse: '', status: 'NORMAL' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await inputApi.getInventory({ ...query, page: 1 })
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
    const res = await inputApi.getInventory({ ...query })
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
    stockQuantity: item.stockQuantity ?? '',
    unit: item.unit || '',
    warningLevel: item.warningLevel ?? '',
    warehouse: item.warehouse || '',
    status: item.status || 'NORMAL'
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
      await inputApi.updateInventory(editId.value, form)
      showToast({ type: 'success', message: '修改成功' })
    } else {
      await inputApi.createInventory(form)
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
    await showConfirmDialog({ title: '确认删除', message: '确认删除该库存记录？' })
    await inputApi.deleteInventory(item.id)
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
    await inputApi.exportInventory()
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.inventory-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 4px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-form { padding-bottom: 20px; }
.drawer-actions { padding: 16px; }
</style>
