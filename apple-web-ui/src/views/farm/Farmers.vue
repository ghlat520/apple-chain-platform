<template>
  <div class="farmers-page">
    <van-search
      v-model="query.keyword"
      placeholder="搜索农户姓名/电话"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <div class="filter-bar">
      <van-tabs v-model:active="query.status" @change="handleSearch" scrollable>
        <van-tab title="全部" name="" />
        <van-tab title="活跃" name="ACTIVE" />
        <van-tab title="停用" name="INACTIVE" />
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
              <span>{{ item.name || '未知农户' }}</span>
              <van-tag :type="statusTagType(item.status)" size="small" style="margin-left:6px">{{ statusLabel(item.status) }}</van-tag>
            </template>
            <template #label>
              电话: {{ item.phone || '-' }} · 面积: {{ item.registeredArea ? item.registeredArea + '亩' : '-' }}
            </template>
            <template #value>
              <span style="font-size:12px;color:#969799">{{ item.farmerCode || '' }}</span>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!loading && list.length === 0" description="暂无农户信息" />
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
        <span>{{ editId ? '编辑农户' : '新增农户' }}</span>
        <van-icon name="cross" @click="showDrawer = false" />
      </div>
      <van-form @submit="handleSubmit" class="drawer-form">
        <van-cell-group inset>
          <van-field
            v-model="form.name"
            label="姓名"
            placeholder="请输入农户姓名"
            :rules="[{ required: true, message: '请填写姓名' }]"
          />
          <van-field
            v-model="form.phone"
            label="联系电话"
            placeholder="请输入手机号"
            type="tel"
            :rules="[{ required: true, message: '请填写联系电话' }]"
          />
          <van-field
            v-model="form.idCard"
            label="身份证号"
            placeholder="请输入身份证号"
          />
          <van-field
            v-model="form.address"
            label="地址"
            placeholder="请输入地址"
          />
          <van-field
            v-model="form.registeredArea"
            label="种植面积(亩)"
            placeholder="请输入种植面积"
            type="number"
          />
          <van-field label="状态">
            <template #input>
              <van-radio-group v-model="form.status" direction="horizontal">
                <van-radio name="ACTIVE">活跃</van-radio>
                <van-radio name="INACTIVE">停用</van-radio>
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
        <span>农户详情</span>
        <van-icon name="cross" @click="showDetail = false" />
      </div>
      <van-cell-group inset v-if="currentItem">
        <van-cell title="状态">
          <template #value>
            <van-tag :type="statusTagType(currentItem.status)">{{ statusLabel(currentItem.status) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="农户编号" :value="currentItem.farmerCode || '-'" />
        <van-cell title="姓名" :value="currentItem.name || '-'" />
        <van-cell title="联系电话" :value="currentItem.phone || '-'" />
        <van-cell title="身份证号" :value="currentItem.idCard || '-'" />
        <van-cell title="地址" :value="currentItem.address || '-'" />
        <van-cell title="种植面积" :value="currentItem.registeredArea ? currentItem.registeredArea + '亩' : '-'" />
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
import { farmApi } from '@/api/farm.js'
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

const query = reactive({ status: '', keyword: '', page: 1, size: 10 })
const form = reactive({ name: '', phone: '', idCard: '', address: '', registeredArea: '', status: 'ACTIVE' })

function statusTagType(status) {
  const map = { ACTIVE: 'success', INACTIVE: 'danger' }
  return map[status] || 'default'
}

function statusLabel(status) {
  const map = { ACTIVE: '活跃', INACTIVE: '停用' }
  return map[status] || status || '-'
}

function resetForm() {
  Object.assign(form, { name: '', phone: '', idCard: '', address: '', registeredArea: '', status: 'ACTIVE' })
}

async function loadList() {
  loading.value = true
  try {
    const res = await farmApi.getFarmers({ ...query, page: 1 })
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
    const res = await farmApi.getFarmers({ ...query })
    const records = res?.records || []
    list.value = [...list.value, ...records]
    if (list.value.length >= (res?.total || 0) || records.length < query.size) {
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
    phone: item.phone || '',
    idCard: item.idCard || '',
    address: item.address || '',
    registeredArea: item.registeredArea || '',
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
      await farmApi.updateFarmer(editId.value, form)
      showToast({ type: 'success', message: '修改成功' })
    } else {
      await farmApi.createFarmer(form)
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
    await showConfirmDialog({ title: '确认删除', message: '确认删除该农户？' })
    await farmApi.deleteFarmer(item.id)
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
    await farmApi.exportFarmers()
  } catch (e) {
    showToast('导出失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.farmers-page { padding-bottom: 20px; }
.filter-bar { background: #fff; margin-bottom: 4px; }
.action-bar { display: flex; gap: 8px; padding: 8px 16px; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; padding: 16px; font-size: 16px; font-weight: 600; border-bottom: 1px solid #ebedf0; }
.drawer-form { padding-bottom: 20px; }
.drawer-actions { padding: 16px; }
</style>
